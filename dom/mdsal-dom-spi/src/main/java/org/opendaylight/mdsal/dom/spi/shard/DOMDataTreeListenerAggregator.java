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
import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregator which combines multiple disjunct {@link DOMDataTreeListener} and forwards their changes to a central
 * listener.
 *
 * @author Robert Varga
 */
@Beta
public final class DOMDataTreeListenerAggregator
        extends AbstractStateAggregator<DOMDataTreeListenerAggregator.State> {

    abstract static class State extends AbstractStateAggregator.State {

    }

    private static final class Aggregated extends State {
        final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees;
        final Collection<DOMDataTreeListeningException> failures;
        final Collection<DataTreeCandidate> changes;

        Aggregated(final Collection<DataTreeCandidate> changes,
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees,
            final Collection<DOMDataTreeListeningException> failures) {
            this.changes = Preconditions.checkNotNull(changes);
            this.subtrees = Preconditions.checkNotNull(subtrees);
            this.failures = Preconditions.checkNotNull(failures);
        }
    }

    private static final class Changes extends State {
        final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees;
        final Collection<DataTreeCandidate> changes;

        Changes(final Collection<DataTreeCandidate> changes,
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
            this.changes = Preconditions.checkNotNull(changes);
            this.subtrees = Preconditions.checkNotNull(subtrees);
        }
    }

    private static final class Failure extends State {
        final Collection<DOMDataTreeListeningException> causes;

        Failure(final Collection<DOMDataTreeListeningException> causes) {
            this.causes = Preconditions.checkNotNull(causes);
        }
    }

    private static final class StateBuilder extends AbstractStateAggregator.StateBuilder<State> {
        @GuardedBy("this")
        private final Collection<DOMDataTreeListeningException> causes = new ArrayList<>(0);
        @GuardedBy("this")
        private final Collection<DataTreeCandidate> changes = new ArrayList<>();
        @GuardedBy("this")
        private Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees = ImmutableMap.of();

        @Override
        protected void append(final State state) {
            if (state instanceof Changes) {
                final Changes changesState = (Changes) state;
                this.changes.addAll(changesState.changes);
                subtrees = ImmutableMap.copyOf(changesState.subtrees);
            } else if (state instanceof Failure) {
                causes.addAll(((Failure) state).causes);
            } else {
                throw new IllegalStateException("Unexpected state " + state);
            }
        }

        @Override
        protected synchronized void appendInitial(final State state) {
            // TODO: we could index and compress state changes here
            if (state instanceof Changes) {
                final Changes changesState = (Changes) state;
                this.changes.addAll(changesState.changes);
                subtrees = ImmutableMap.copyOf(changesState.subtrees);
            } else if (state instanceof Failure) {
                causes.addAll(((Failure) state).causes);
            } else {
                throw new IllegalStateException("Unexpected state " + state);
            }
        }

        @Override
        public synchronized Aggregated build() {
            final Aggregated ret = new Aggregated(ImmutableList.copyOf(changes), subtrees,
                ImmutableList.copyOf(causes));
            changes.clear();
            causes.clear();
            return ret;
        }
    }

    private static final class Operational extends AbstractStateAggregator.Operational<State> {
        private final DOMDataTreeListener listener;
        private boolean failed;

        Operational(final Collection<AbstractStateAggregator.StateBuilder<State>> builders,
                final DOMDataTreeListener listener) {
            super(builders);
            this.listener = Preconditions.checkNotNull(listener);
        }

        @Override
        protected void notifyListener(final Iterator<State> iterator) {
            if (failed) {
                iterator.forEachRemaining(state -> LOG.debug("Listener {} failed, ignoring state {}", state));
                return;
            }

            final Stopwatch clock = Stopwatch.createStarted();
            final List<DataTreeCandidate> changes = new ArrayList<>();
            final List<DOMDataTreeListeningException> failures = new ArrayList<>(0);
            final Builder<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees = ImmutableMap.builder();
            while (iterator.hasNext()) {
                collectState(iterator.next(), changes, subtrees, failures);
            }

            if (!changes.isEmpty()) {
                // Note: it is okay to leak changes, we must never leak mutable subtrees.
                callListener(listener, changes, subtrees.build());
            }
            if (!failures.isEmpty()) {
                failed = true;
                listener.onDataTreeFailed(failures);
            }

            LOG.trace("Listener {} notification completed in {}", listener, clock);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DOMDataTreeListenerAggregator.class);

    // Because a component listener may report a failure before we finish registering all listeners, we need a way
    // to trigger a failure report from the thread *not* performing the registration.
    private static final Executor FAILURE_NOTIFIER;

    static {
        final ThreadFactoryBuilder tfb = new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat(DOMDataTreeListenerAggregator.class.getSimpleName() + "-failure-%s");
        FAILURE_NOTIFIER = Executors.newSingleThreadExecutor(tfb.build());
    }

    private final boolean allowRxMerges;

    public DOMDataTreeListenerAggregator(final int sizeHint, final boolean allowRxMerges) {
        super(sizeHint);
        this.allowRxMerges = allowRxMerges;
    }

    public static <L extends DOMDataTreeListener, T> ListenerRegistration<L> aggregateIfNeeded(final L listener,
            final Map<T, Collection<DOMDataTreeIdentifier>> subtrees, final boolean allowRxMerges,
            final Function<T, DOMDataTreeShard> keyToShard) {
        if (subtrees.size() == 1) {
            final Entry<T, Collection<DOMDataTreeIdentifier>> entry = subtrees.entrySet().iterator()
                    .next();
            return CompatListenableDOMDataTreeShard.createIfNeeded(keyToShard.apply(entry.getKey()))
                    .registerListener(listener, entry.getValue(), allowRxMerges);
        }

        // Alright, this the real deal, we have to aggregate.
        final int size = subtrees.size();
        final DOMDataTreeListenerAggregator aggregator = new DOMDataTreeListenerAggregator(size, allowRxMerges);
        final Collection<ListenerRegistration<DOMDataTreeListener>> regs = new ArrayList<>(size);
        for (Entry<T, Collection<DOMDataTreeIdentifier>> entry : subtrees.entrySet()) {
            regs.add(CompatListenableDOMDataTreeShard.createIfNeeded(keyToShard.apply(entry.getKey()))
                .registerListener(aggregator.createListener(), entry.getValue(), allowRxMerges));
        }

        return aggregator.start(listener, regs);
    }

    public DOMDataTreeListener createListener() {
        // TODO: do not ignore allowRxMerges, but rather create a dedicated subclass or something
        final StateBuilder builder = new StateBuilder();
        addBuilder(builder);

        return new DOMDataTreeListener() {
            @Override
            public void onDataTreeFailed(final Collection<DOMDataTreeListeningException> causes) {
                receiveState(builder, new Failure(causes));
            }

            @Override
            public void onDataTreeChanged(final Collection<DataTreeCandidate> changes,
                    final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
                receiveState(builder, new Changes(changes, subtrees));
            }
        };
    }

    public <L extends DOMDataTreeListener> ListenerRegistration<L> start(final L listener,
            final Collection<ListenerRegistration<DOMDataTreeListener>> regs) {

        final Started<State> result = start(builders -> start(listener, regs, builders));
        if (result instanceof Failed) {
            return new AbstractListenerRegistration<L>(listener) {
                @Override
                protected void removeRegistration() {
                    // Listeners have already been closed, this is a no-op
                }
            };
        }

        return new AbstractListenerRegistration<L>(listener) {
            @Override
            protected void removeRegistration() {
                regs.forEach(ListenerRegistration::close);
            }
        };
    }

    static Started<State> start(final DOMDataTreeListener listener,
            final Collection<ListenerRegistration<DOMDataTreeListener>> regs,
            final Collection<AbstractStateAggregator.StateBuilder<State>> builders) {

        final List<DataTreeCandidate> changes = new ArrayList<>();
        final List<DOMDataTreeListeningException> failures = new ArrayList<>(0);
        final Builder<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees = ImmutableMap.builder();
        for (AbstractStateAggregator.StateBuilder<State> builder : builders) {
            collectState(builder.build(), changes, subtrees, failures);
        }

        if (!failures.isEmpty()) {
            regs.forEach(ListenerRegistration::close);
            FAILURE_NOTIFIER.execute(() -> listener.onDataTreeFailed(failures));
            return new Failed<>(builders);
        }
        if (!changes.isEmpty()) {
            callListener(listener, changes, subtrees.build());
        }

        return new Operational(builders, listener);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    static void callListener(final DOMDataTreeListener listener, final Collection<DataTreeCandidate> changes,
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
        try {
            listener.onDataTreeChanged(changes, subtrees);
        } catch (Exception e) {
            LOG.error("Listener {} failed to process initial changes", listener, e);
        }
    }

    static void collectState(final State state, final Collection<DataTreeCandidate> changes,
            final Builder<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees,
            final Collection<DOMDataTreeListeningException> failures) {
        Verify.verify(state instanceof Aggregated, "Unexpected state %s", state);
        final Aggregated aggregated = (Aggregated) state;

        subtrees.putAll(aggregated.subtrees);
        changes.addAll(aggregated.changes);
        failures.addAll(aggregated.failures);
    }
}
