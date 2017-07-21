/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Aggregator which combines state reported by potentially multiple threads into a single state report. State is
 * received concurrently to reports and reporter threads are hijacked when there is state to be reported and no thread
 * is reporting it.
 *
 * @param <S> State type
 * @author Robert Varga
 */
@Beta
public abstract class AbstractStateAggregator<S extends AbstractStateAggregator.State> {

    /**
     * Marker interface for state as both reported up and down.
     */
    public abstract static class State implements Immutable {

    }

    /**
     * State aggregator, which receives state chunks and creates an aggregated state object via the build method.
     * Note all internal state must be protected by the the lock on the builder project itself.
     *
     * @param <S> State type
     */
    protected abstract static class StateBuilder<S extends State> implements Builder<S>, Mutable {

        protected abstract void append(S state);

        protected abstract void appendInitial(S state);
    }

    protected abstract static class Behavior<B extends Behavior<B, S>, S extends State> {

        abstract Collection<StateBuilder<S>> builders();

        abstract void receiveState(StateBuilder<S> builder, S state);
    }

    private static final class Starting<S extends State> extends Behavior<Starting<S>, S> {
        private final Collection<StateBuilder<S>> builders;
        @GuardedBy("this")
        private Started<S> successor;

        Starting(final int sizeHint) {
            builders = new ArrayList<>(sizeHint);
        }

        void add(final StateBuilder<S> builder) {
            builders.add(Preconditions.checkNotNull(builder));
        }

        @Override
        Collection<StateBuilder<S>> builders() {
            return builders;
        }

        @Override
        synchronized void receiveState(final StateBuilder<S> builder, final S state) {
            if (successor != null) {
                successor.receiveState(builder, state);
                return;
            }

            builder.appendInitial(state);
        }

        synchronized Started<S> start(final Function<Collection<StateBuilder<S>>, Started<S>> function) {
            Preconditions.checkState(successor == null, "Attempted to start an already-started aggregator");
            final Started<S> next = Verify.verifyNotNull(function.apply(ImmutableList.copyOf(builders)));
            successor = next;
            return next;
        }
    }

    protected abstract static class Started<S extends State> extends Behavior<Started<S>, S> {
        private final Collection<StateBuilder<S>> builders;

        Started(final Collection<? extends StateBuilder<S>> builders) {
            this.builders = ImmutableList.copyOf(builders);
        }

        @Override
        final Collection<StateBuilder<S>> builders() {
            return builders;
        }
    }

    protected static final class Failed<S extends State> extends Started<S> {
        protected Failed(final Collection<? extends StateBuilder<S>> builders) {
            super(builders);
        }

        @Override
        void receiveState(final StateBuilder<S> builder, final S state) {
            // Intentional no-op
        }
    }

    protected abstract static class Operational<S extends State> extends Started<S> {
        // Locking is a combination of a generation counter and a semaphore. Generation is bumped and remembered
        // on stack when new state is being appended. Processed generations are recorded separately. This can cause
        // false-positives when we loop on empty state, but that should not happen often and is harmless.
        private final AtomicBoolean semaphore = new AtomicBoolean();
        private final AtomicLong generation = new AtomicLong();

        private volatile long processed;

        protected Operational(final Collection<? extends StateBuilder<S>> builders) {
            super(builders);
        }

        protected abstract void notifyListener(Iterator<S> iterator);

        @Override
        final void receiveState(final StateBuilder<S> builder, final S state) {
            synchronized (builder) {
                // Generation has to be bumbed atomically with state delivery, otherwise tryNotifyListener could
                // observe state with after generation was bumped and before the state was appended
                final long gen = generation.incrementAndGet();
                try {
                    builder.append(state);
                } finally {
                    tryNotifyListener(gen);
                }
            }
        }

        private void tryNotifyListener(final long initGen) {
            long gen = initGen;

            // We now have to re-sync, as we may end up being the last thread in position to observe the complete state
            // of the queues. Since queues are updated independently to iteration, notifyListener() may have missed
            // some updates, in which case we must eventually run it.
            //
            // Check if this generation was processed by someone else (while we were inserting items) or if there is
            // somebody else already running this loop (which means they will re-check and spin again).
            while (gen != processed && semaphore.compareAndSet(false, true)) {
                try {
                    processed = gen;
                    notifyListener(Iterators.transform(builders().iterator(), StateBuilder::build));
                } finally {
                    semaphore.set(false);
                }

                final long nextGen = generation.get();
                if (nextGen == gen) {
                    // No modifications happened, we are done
                    return;
                }

                gen = nextGen;
            }
        }
    }

    private volatile Behavior<?, S> behavior;

    protected AbstractStateAggregator(final int sizeHint) {
        this.behavior = new Starting<>(sizeHint);
    }

    protected final void addBuilder(final StateBuilder<S> builder) {
        checkStarting().add(builder);
    }

    protected final synchronized Started<S> start(final Function<Collection<StateBuilder<S>>, Started<S>> function) {
        final Started<S> ret = checkStarting().start(function);
        behavior = ret;
        return ret;
    }

    protected final void receiveState(final StateBuilder<S> builder, final S state) {
        behavior.receiveState(builder, state);
    }

    @SuppressWarnings("unchecked")
    private Starting<S> checkStarting() {
        final Behavior<?, S> local = behavior;
        Preconditions.checkState(local instanceof Starting, "Unexpected behavior %s", local);
        return (Starting<S>) local;
    }
}
