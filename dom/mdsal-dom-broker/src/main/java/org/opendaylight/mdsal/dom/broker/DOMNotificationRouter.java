/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListener;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListenerRegistry;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.util.concurrent.EqualityQueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Joint implementation of {@link DOMNotificationPublishService} and {@link DOMNotificationService}. Provides
 * routing of notifications from publishers to subscribers.
 *
 *<p>
 * Internal implementation one by using a {@link QueuedNotificationManager}.
 *</p>
 */
@Component(immediate = true, configurationPid = "org.opendaylight.mdsal.dom.notification", service = {
    DOMNotificationService.class, DOMNotificationPublishService.class,
    DOMNotificationSubscriptionListenerRegistry.class
})
@Designate(ocd = DOMNotificationRouter.Config.class)
// Non-final for testing
public class DOMNotificationRouter implements AutoCloseable, DOMNotificationPublishService,
        DOMNotificationService, DOMNotificationSubscriptionListenerRegistry {
    @ObjectClassDefinition()
    public @interface Config {
        @AttributeDefinition(name = "notification-queue-depth")
        int queueDepth() default 65536;
    }

    @VisibleForTesting
    abstract static sealed class Reg<T extends DOMNotificationListener> extends AbstractListenerRegistration<T> {
        Reg(final @NonNull T listener) {
            super(listener);
        }
    }

    private final class SingleReg<T extends DOMNotificationListener> extends Reg<T> {
        SingleReg(final @NonNull T listener) {
            super(listener);
        }

        @Override
        protected void removeRegistration() {
            DOMNotificationRouter.this.removeRegistration(this);
        }
    }

    private static final class ComponentReg extends Reg<DOMNotificationListener> {
        ComponentReg(final @NonNull DOMNotificationListener listener) {
            super(listener);
        }

        @Override
        protected void removeRegistration() {
            // No-op
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DOMNotificationRouter.class);
    private static final ListenableFuture<Void> NO_LISTENERS = FluentFutures.immediateNullFluentFuture();

    private final ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
            ListenerRegistry.create();
    private final EqualityQueuedNotificationManager<AbstractListenerRegistration<? extends DOMNotificationListener>,
                DOMNotificationRouterEvent> queueNotificationManager;
    private final ScheduledThreadPoolExecutor observer;
    private final ExecutorService executor;

    private volatile Multimap<Absolute, Reg<?>> listeners = ImmutableMultimap.of();

    @Inject
    public DOMNotificationRouter(final int maxQueueCapacity) {
        observer = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("DOMNotificationRouter-observer-%d")
            .build());
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("DOMNotificationRouter-listeners-%d")
            .build());
        queueNotificationManager = new EqualityQueuedNotificationManager<>("DOMNotificationRouter", executor,
                maxQueueCapacity, DOMNotificationRouter::deliverEvents);
    }

    @Activate
    public DOMNotificationRouter(final Config config) {
        this(config.queueDepth());
        LOG.info("DOM Notification Router started");
    }

    @Deprecated(forRemoval = true)
    public static DOMNotificationRouter create(final int maxQueueCapacity) {
        return new DOMNotificationRouter(maxQueueCapacity);
    }

    @Override
    public synchronized <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(
            final T listener, final Collection<Absolute> types) {
        final var reg = new SingleReg<>(listener);

        if (!types.isEmpty()) {
            final var b = ImmutableMultimap.<Absolute, Reg<?>>builder();
            b.putAll(listeners);

            for (var t : types) {
                b.put(t, reg);
            }

            replaceListeners(b.build());
        }

        return reg;
    }

    @Override
    public synchronized Registration registerNotificationListeners(
            final Map<Absolute, DOMNotificationListener> typeToListener) {
        final var b = ImmutableMultimap.<Absolute, Reg<?>>builder();
        b.putAll(listeners);

        final var tmp = new HashMap<DOMNotificationListener, ComponentReg>();
        for (var e : typeToListener.entrySet()) {
            b.put(e.getKey(), tmp.computeIfAbsent(e.getValue(), ComponentReg::new));
        }
        replaceListeners(b.build());

        final var regs = List.copyOf(tmp.values());
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                regs.forEach(ComponentReg::close);
                removeRegistrations(regs);
            }
        };
    }

    private synchronized void removeRegistration(final SingleReg<?> reg) {
        replaceListeners(ImmutableMultimap.copyOf(Multimaps.filterValues(listeners, input -> input != reg)));
    }

    private synchronized void removeRegistrations(final List<ComponentReg> regs) {
        replaceListeners(ImmutableMultimap.copyOf(Multimaps.filterValues(listeners, input -> !regs.contains(input))));
    }

    /**
     * Swaps registered listeners and triggers notification update.
     *
     * @param newListeners is used to notify listenerTypes changed
     */
    private void replaceListeners(final Multimap<Absolute, Reg<?>> newListeners) {
        listeners = newListeners;
        notifyListenerTypesChanged(newListeners.keySet());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void notifyListenerTypesChanged(final Set<Absolute> typesAfter) {
        final List<? extends DOMNotificationSubscriptionListener> listenersAfter =
                subscriptionListeners.streamListeners().collect(ImmutableList.toImmutableList());
        executor.execute(() -> {
            for (final DOMNotificationSubscriptionListener subListener : listenersAfter) {
                try {
                    subListener.onSubscriptionChanged(typesAfter);
                } catch (final Exception e) {
                    LOG.warn("Uncaught exception during invoking listener {}", subListener, e);
                }
            }
        });
    }

    @Override
    public <L extends DOMNotificationSubscriptionListener> ListenerRegistration<L> registerSubscriptionListener(
            final L listener) {
        final Set<Absolute> initialTypes = listeners.keySet();
        executor.execute(() -> listener.onSubscriptionChanged(initialTypes));
        return subscriptionListeners.register(listener);
    }

    @VisibleForTesting
    ListenableFuture<? extends Object> publish(final DOMNotification notification,
            final Collection<Reg<?>> subscribers) {
        final List<ListenableFuture<Void>> futures = new ArrayList<>(subscribers.size());
        subscribers.forEach(subscriber -> {
            final DOMNotificationRouterEvent event = new DOMNotificationRouterEvent(notification);
            futures.add(event.future());
            queueNotificationManager.submitNotification(subscriber, event);
        });
        return Futures.transform(Futures.successfulAsList(futures), ignored -> (Void)null,
            MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
            throws InterruptedException {
        final var subscribers = listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }

        return publish(notification, subscribers);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification) {
        final var subscribers = listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }

        return publish(notification, subscribers);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        final var subscribers = listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }
        // Attempt to perform a non-blocking publish first
        final ListenableFuture<?> noBlock = publish(notification, subscribers);
        if (!DOMNotificationPublishService.REJECTED.equals(noBlock)) {
            return noBlock;
        }

        try {
            final Thread publishThread = Thread.currentThread();
            ScheduledFuture<?> timerTask = observer.schedule(publishThread::interrupt, timeout, unit);
            final ListenableFuture<?> withBlock = putNotification(notification);
            timerTask.cancel(true);
            if (observer.getQueue().size() > 50) {
                observer.purge();
            }
            return withBlock;
        } catch (InterruptedException e) {
            return DOMNotificationPublishService.REJECTED;
        }
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        observer.shutdown();
        executor.shutdown();
        LOG.info("DOM Notification Router stopped");
    }

    @VisibleForTesting
    ExecutorService executor() {
        return executor;
    }

    @VisibleForTesting
    ExecutorService observer() {
        return observer;
    }

    @VisibleForTesting
    Multimap<Absolute, ?> listeners() {
        return listeners;
    }

    @VisibleForTesting
    ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners() {
        return subscriptionListeners;
    }

    private static void deliverEvents(final AbstractListenerRegistration<? extends DOMNotificationListener> reg,
            final ImmutableList<DOMNotificationRouterEvent> events) {
        if (reg.notClosed()) {
            final DOMNotificationListener listener = reg.getInstance();
            for (DOMNotificationRouterEvent event : events) {
                event.deliverTo(listener);
            }
        } else {
            events.forEach(DOMNotificationRouterEvent::clear);
        }
    }
}
