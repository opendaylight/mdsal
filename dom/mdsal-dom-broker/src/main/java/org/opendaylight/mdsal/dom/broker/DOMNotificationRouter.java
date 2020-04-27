/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Joint implementation of {@link DOMNotificationPublishService} and {@link DOMNotificationService}. Provides
 * routing of notifications from publishers to subscribers.
 *
 *<p>
 * Internal implementation works by allocating a two-handler Disruptor. The first handler delivers notifications
 * to subscribed listeners and the second one notifies whoever may be listening on the returned future. Registration
 * state tracking is performed by a simple immutable multimap -- when a registration or unregistration occurs we
 * re-generate the entire map from scratch and set it atomically. While registrations/unregistrations synchronize
 * on this instance, notifications do not take any locks here.
 *
 *<p>
 * The fully-blocking {@link #publish(long, DOMNotification, Collection)}
 * and non-blocking {@link #offerNotification(DOMNotification)}
 * are realized using the Disruptor's native operations. The bounded-blocking {@link
 * #offerNotification(DOMNotification, long, TimeUnit)}
 * is realized by arming a background wakeup interrupt.
 */
public class DOMNotificationRouter implements AutoCloseable, DOMNotificationPublishService,
        DOMNotificationService, DOMNotificationSubscriptionListenerRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DOMNotificationRouter.class);
    private static final ListenableFuture<Void> NO_LISTENERS = FluentFutures.immediateNullFluentFuture();
    private static final WaitStrategy DEFAULT_STRATEGY = PhasedBackoffWaitStrategy.withLock(
            1L, 30L, TimeUnit.MILLISECONDS);
    private static final EventHandler<DOMNotificationRouterEvent> DISPATCH_NOTIFICATIONS =
        (event, sequence, endOfBatch) -> event.deliverNotification();
    private static final EventHandler<DOMNotificationRouterEvent> NOTIFY_FUTURE =
        (event, sequence, endOfBatch) -> event.setFuture();

    private final ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
            ListenerRegistry.create();
    private final Disruptor<DOMNotificationRouterEvent> disruptor;
    private final ScheduledThreadPoolExecutor observer;
    private final ExecutorService executor;

    private volatile Multimap<SchemaPath, AbstractListenerRegistration<? extends DOMNotificationListener>> listeners =
            ImmutableMultimap.of();

    @VisibleForTesting
    DOMNotificationRouter(final int queueDepth, final WaitStrategy strategy) {
        observer = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("DOMNotificationRouter-observer-%d").build());
        executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("DOMNotificationRouter-listeners-%d").build());
        disruptor = new Disruptor<>(DOMNotificationRouterEvent.FACTORY, queueDepth,
                new ThreadFactoryBuilder().setNameFormat("DOMNotificationRouter-disruptor-%d").build(),
                ProducerType.MULTI, strategy);
        disruptor.handleEventsWith(DISPATCH_NOTIFICATIONS);
        disruptor.after(DISPATCH_NOTIFICATIONS).handleEventsWith(NOTIFY_FUTURE);
        disruptor.start();
    }

    public static DOMNotificationRouter create(final int queueDepth) {
        return new DOMNotificationRouter(queueDepth, DEFAULT_STRATEGY);
    }

    public static DOMNotificationRouter create(final int queueDepth, final long spinTime, final long parkTime,
            final TimeUnit unit) {
        checkArgument(Long.lowestOneBit(queueDepth) == Long.highestOneBit(queueDepth),
                "Queue depth %s is not power-of-two", queueDepth);
        return new DOMNotificationRouter(queueDepth, PhasedBackoffWaitStrategy.withLock(spinTime, parkTime, unit));
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

    private ListenableFuture<Void> publish(final long seq, final DOMNotification notification,
            final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers) {
        final DOMNotificationRouterEvent event = disruptor.get(seq);
        final ListenableFuture<Void> future = event.initialize(notification, subscribers);
        disruptor.getRingBuffer().publish(seq);
        return future;
    }

    @Override
    public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
            throws InterruptedException {
        final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers =
                listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }

        final long seq = disruptor.getRingBuffer().next();
        return publish(seq, notification, subscribers);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @VisibleForTesting
    ListenableFuture<? extends Object> tryPublish(final DOMNotification notification,
            final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers) {
        final long seq;
        try {
            seq = disruptor.getRingBuffer().tryNext();
        } catch (final InsufficientCapacityException e) {
            return DOMNotificationPublishService.REJECTED;
        }

        return publish(seq, notification, subscribers);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification) {
        final Collection<AbstractListenerRegistration<? extends DOMNotificationListener>> subscribers =
                listeners.get(notification.getType());
        if (subscribers.isEmpty()) {
            return NO_LISTENERS;
        }

        return tryPublish(notification, subscribers);
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
        final ListenableFuture<?> noBlock = tryPublish(notification, subscribers);
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
        disruptor.shutdown();
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
