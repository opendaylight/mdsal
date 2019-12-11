/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import org.checkerframework.checker.lock.qual.GuardedBy;

/**
 * An abstract event queue, which can quickly consume events.
 *
 * @param <T> event type
 */
@Beta
public final class EventQueue<T extends AbstractListenerEvent<?>> {
    @FunctionalInterface
    public interface EventConsumer<T> {

        boolean consume(T event);
    }

    @FunctionalInterface
    public interface EventPredicate extends Predicate<Object> {

    }

    @GuardedBy("this")
    private final Deque<T> events = new ArrayDeque<>();

    public void addEvent(final T event) {
        events.add(requireNonNull(event));
    }

    public void consumeEvents(final EventConsumer<T> predicate) {
        // We need to deal with reentrancy on events, as firing an event may cause multiple other events to be enqueued
        // which will break iteration for no good reason. Those events will be executing on this thread, so synchronized
        // is not really an option.
        //
        // To avoid that scenario we do a slight twist on the Deque:
        // - we first consume all events from the queue (under synchronized to preemnt other threads) to a local temp
        // - we drop the lock and process all events, removing them as we go
        // - we re-acquire the lock and push any remaining events back (to front of the queue)
        //
        // This involves a few efficiency shenanigans, where we invert the order of events while filling remaining
        // events.

        final List<T> satb;
        synchronized (this) {
            // Oldest event first here ...
            satb = new ArrayList<>(events);
            events.clear();
        }

        // .. becomes newest event first here ...
        final Deque<T> unconsumed = new ArrayDeque<>();
        for (T event : satb) {
            if (!predicate.consume(event)) {
                unconsumed.addFirst(event);
            }
        }

        synchronized (this) {
            // ... ends up being pushed to front with consecutively older events
            unconsumed.forEach(events::addFirst);
        }
    }
}
