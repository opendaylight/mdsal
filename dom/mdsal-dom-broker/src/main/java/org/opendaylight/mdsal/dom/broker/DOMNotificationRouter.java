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
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListener;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListenerRegistry;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.util.concurrent.EqualityQueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
public class DOMNotificationRouter implements AutoCloseable, DOMNotificationPublishService,
        DOMNotificationService, DOMNotificationSubscriptionListenerRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DOMNotificationRouter.class);
    private static final ListenableFuture<Void> NO_LISTENERS = FluentFutures.immediateNullFluentFuture();

    private final ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
            ListenerRegistry.create();
    private final EqualityQueuedNotificationManager<ListenerRegistration<? extends DOMNotificationListener>,
                DOMNotificationRouterEvent> queueNotificationManager;
    private final ScheduledThreadPoolExecutor observer;
    private final ExecutorService executor;

    private volatile Multimap<SchemaPath, AbstractListenerRegistration<? extends DOMNotificationListener>> listeners =
            ImmutableMultimap.of();

    @VisibleForTesting
    DOMNotificationRouter(int maxQueueCapacity) {
        observer = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("DOMNotificationRouter-observer-%d").build());
        executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("DOMNotificationRouter-listeners-%d").build());
        queueNotificationManager = new EqualityQueuedNotificationManager<>(
                "DOMNotificationRouter",
                executor,
                maxQueueCapacity,
                (key, events) -> events.forEach(DOMNotificationRouterEvent::deliverNotification));
    }

    public static DOMNotificationRouter create(int maxQueueCapacity) {
        return new DOMNotificationRouter(maxQueueCapacity);
    }

    @Override
    public synchronized <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(
            final T listener, final Collection<SchemaPath> types) {
        final AbstractListenerRegistration<T> reg = new AbstractListenerRegistration<>(listener) {
            @Override
            protected void removeRegistration() {
                synchronized (DOMNotificationRouter.this) {
                    replaceListeners(ImmutableMultimap.copyOf(Multimaps.filterValues(listeners,
                        input -> input != this)));
                }
            }
        };

        if (!types.isEmpty()) {
            final Builder<SchemaPath, AbstractListenerRegistration<? extends DOMNotificationListener>> b =
                    ImmutableMultimap.builder();
            b.putAll(listeners);

            for (final SchemaPath t : types) {
                b.put(t, reg);
            }

            replaceListeners(b.build());
        }

        return reg;
    }

    @Override
    public <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(
            final T listener, final SchemaPath... types) {
        return registerNotificationListener(listener, Arrays.asList(types));
    }

    /**
     * Swaps registered listeners and triggers notification update.
     *
     * @param newListeners is used to notify listenerTypes changed
     */
    private void replaceListeners(
            final Multimap<SchemaPath, AbstractListenerRegistration<? extends DOMNotificationListener>> newListeners) {
        listeners = newListeners;
        notifyListenerTypesChanged(newListeners.keySet());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void notifyListenerTypesChanged(final Set<SchemaPath> typesAfter) {
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
        final Set<SchemaPath> initialTypes = listeners.keySet();
        executor.execute(() -> listener.onSubscriptionChanged(initialTypes));
        return subscriptionListeners.register(listener);
    }


    @VisibleForTesting
    ListenableFuture<? extends Object> publish(DOMNotification notification,
            final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers) {
        List<Future<Void>> futures = new ArrayList<>();
        subscribers.forEach(subscriber -> {
            final DOMNotificationRouterEvent event = DOMNotificationRouterEvent.FACTORY.newInstance();
            final ListenableFuture<Void> future = event.initialize(notification, subscriber);
            futures.add(future);
            queueNotificationManager.submitNotification(subscriber, event);
        });
        return Futures.transform(
                Futures.successfulAsList((Iterable)futures),
                ignored -> (Void)null,
                MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
            throws InterruptedException {
        final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers =
                listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }

        return publish(notification, subscribers);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification) {
        final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers =
                listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }

        return publish(notification, subscribers);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers =
                listeners.get(notification.getType());
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

    @Override
    public void close() {
        observer.shutdown();
        executor.shutdown();
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
    Multimap<SchemaPath, ?> listeners() {
        return listeners;
    }

    @VisibleForTesting
    ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners() {
        return subscriptionListeners;
    }
}
