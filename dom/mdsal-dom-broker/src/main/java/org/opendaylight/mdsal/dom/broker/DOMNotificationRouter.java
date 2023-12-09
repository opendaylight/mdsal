/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension.DemandListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ObjectRegistry;
import org.opendaylight.yangtools.util.concurrent.EqualityQueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.yang.common.Empty;
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
@Singleton
@Component(configurationPid = "org.opendaylight.mdsal.dom.notification", service = DOMNotificationRouter.class)
@Designate(ocd = DOMNotificationRouter.Config.class)
// Non-final for testing
public class DOMNotificationRouter implements AutoCloseable {
    @ObjectClassDefinition()
    public @interface Config {
        @AttributeDefinition(name = "notification-queue-depth")
        int queueDepth() default 65536;
    }

    @VisibleForTesting
    abstract static sealed class Reg extends AbstractRegistration {
        private final @NonNull DOMNotificationListener listener;

        Reg(final @NonNull DOMNotificationListener listener) {
            this.listener = requireNonNull(listener);
        }
    }

    private final class SingleReg extends Reg {
        SingleReg(final @NonNull DOMNotificationListener listener) {
            super(listener);
        }

        @Override
        protected void removeRegistration() {
            DOMNotificationRouter.this.removeRegistration(this);
        }
    }

    private static final class ComponentReg extends Reg {
        ComponentReg(final @NonNull DOMNotificationListener listener) {
            super(listener);
        }

        @Override
        protected void removeRegistration() {
            // No-op
        }
    }

    private final class PublishFacade implements DOMNotificationPublishService, DOMNotificationPublishDemandExtension {
        @Override
        public List<Extension> supportedExtensions() {
            return List.of(this);
        }

        @Override
        public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
                throws InterruptedException {
            return putNotificationImpl(notification);
        }

        @Override
        public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification) {
            final var subscribers = listeners.get(notification.getType());
            return subscribers.isEmpty() ? NO_LISTENERS : publish(notification, subscribers);
        }

        @Override
        public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification,
                final long timeout, final TimeUnit unit) throws InterruptedException {
            final var subscribers = listeners.get(notification.getType());
            if (subscribers.isEmpty()) {
                return NO_LISTENERS;
            }
            // Attempt to perform a non-blocking publish first
            final var noBlock = publish(notification, subscribers);
            if (!DOMNotificationPublishService.REJECTED.equals(noBlock)) {
                return noBlock;
            }

            try {
                final var publishThread = Thread.currentThread();
                final var timerTask = observer.schedule(publishThread::interrupt, timeout, unit);
                final var withBlock = putNotificationImpl(notification);
                timerTask.cancel(true);
                if (observer.getQueue().size() > 50) {
                    observer.purge();
                }
                return withBlock;
            } catch (InterruptedException e) {
                return DOMNotificationPublishService.REJECTED;
            }
        }

        @Override
        public Registration registerDemandListener(final DemandListener listener) {
            final var initialTypes = listeners.keySet();
            executor.execute(() -> listener.onDemandUpdated(initialTypes));
            return demandListeners.register(listener);
        }
    }

    private final class SubscribeFacade implements DOMNotificationService {
        @Override
        public Registration registerNotificationListener(final DOMNotificationListener listener,
                final Collection<Absolute> types) {
            synchronized (DOMNotificationRouter.this) {
                final var reg = new SingleReg(listener);

                if (!types.isEmpty()) {
                    final var b = ImmutableMultimap.<Absolute, Reg>builder();
                    b.putAll(listeners);

                    for (var t : types) {
                        b.put(t, reg);
                    }

                    replaceListeners(b.build());
                }

                return reg;
            }
        }

        @Override
        public synchronized Registration registerNotificationListeners(
                final Map<Absolute, DOMNotificationListener> typeToListener) {
            synchronized (DOMNotificationRouter.this) {
                final var b = ImmutableMultimap.<Absolute, Reg>builder();
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
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DOMNotificationRouter.class);
    private static final @NonNull ListenableFuture<?> NO_LISTENERS = Futures.immediateFuture(Empty.value());

    private final EqualityQueuedNotificationManager<Reg, DOMNotificationRouterEvent> queueNotificationManager;
    private final @NonNull DOMNotificationPublishService notificationPublishService = new PublishFacade();
    private final @NonNull DOMNotificationService notificationService = new SubscribeFacade();
    private final ObjectRegistry<DemandListener> demandListeners =
        ObjectRegistry.createConcurrent("notification demand listeners");
    private final ScheduledThreadPoolExecutor observer;
    private final ExecutorService executor;

    private volatile ImmutableMultimap<Absolute, Reg> listeners = ImmutableMultimap.of();

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
        LOG.info("DOM Notification Router started");
    }

    @Activate
    public DOMNotificationRouter(final Config config) {
        this(config.queueDepth());
    }

    public @NonNull DOMNotificationService notificationService() {
        return notificationService;
    }

    public @NonNull DOMNotificationPublishService notificationPublishService() {
        return notificationPublishService;
    }

    private synchronized void removeRegistration(final SingleReg reg) {
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
    private void replaceListeners(final ImmutableMultimap<Absolute, Reg> newListeners) {
        listeners = newListeners;
        notifyListenerTypesChanged(newListeners.keySet());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void notifyListenerTypesChanged(final @NonNull ImmutableSet<Absolute> typesAfter) {
        final var listenersAfter = demandListeners.streamObjects().collect(ImmutableList.toImmutableList());
        executor.execute(() -> {
            for (var listener : listenersAfter) {
                try {
                    listener.onDemandUpdated(typesAfter);
                } catch (final Exception e) {
                    LOG.warn("Uncaught exception during invoking listener {}", listener, e);
                }
            }
        });
    }

    @VisibleForTesting
    @NonNull ListenableFuture<? extends Object> putNotificationImpl(final DOMNotification notification)
            throws InterruptedException {
        final var subscribers = listeners.get(notification.getType());
        return subscribers.isEmpty() ? NO_LISTENERS : publish(notification, subscribers);
    }

    @VisibleForTesting
    @NonNull ListenableFuture<?> publish(final DOMNotification notification, final Collection<Reg> subscribers) {
        final var futures = new ArrayList<ListenableFuture<?>>(subscribers.size());
        subscribers.forEach(subscriber -> {
            final var event = new DOMNotificationRouterEvent(notification);
            futures.add(event.future());
            queueNotificationManager.submitNotification(subscriber, event);
        });
        return Futures.transform(Futures.successfulAsList(futures), ignored -> Empty.value(),
            MoreExecutors.directExecutor());
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
    ImmutableMultimap<Absolute, ?> listeners() {
        return listeners;
    }

    @VisibleForTesting
    ObjectRegistry<DemandListener> demandListeners() {
        return demandListeners;
    }

    private static void deliverEvents(final Reg reg, final ImmutableList<DOMNotificationRouterEvent> events) {
        if (reg.notClosed()) {
            final var listener = reg.listener;
            for (var event : events) {
                event.deliverTo(listener);
            }
        } else {
            events.forEach(DOMNotificationRouterEvent::clear);
        }
    }
}
