/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A compatibility class for bridging DOMDataTreeChangeListener, which can listen on only single subtree with
 * {@link DOMDataTreeListener} interface.
 *
 * @author Robert Varga
 * @deprecated This class is scheduled for removal when we remove compatibility with dom.spi.store APIs.
 */
@Deprecated
final class DOMDataTreeChangeListenerAggregator {
    private abstract static class State {

        abstract void receiveChanges(CandidateQueue queue, Collection<DataTreeCandidate> changes);
    }

    private static final class Starting extends State {
        private final Collection<CandidateQueue> queues;
        private final boolean allowRxMerges;

        @GuardedBy("this")
        private Operational successor;

        Starting(final int sizeHint, final boolean allowRxMerges) {
            this.queues = new ArrayList<>(sizeHint);
            this.allowRxMerges = allowRxMerges;
        }

        CandidateQueue createQueue(final DOMDataTreeIdentifier treeId) {
            final CandidateQueue ret = CandidateQueue.create(treeId, allowRxMerges);
            queues.add(ret);
            return ret;
        }
        synchronized Operational start(final DOMDataTreeListener listener) {
            final Operational ret = new Operational(queues, listener);
            successor = ret;
            return ret;
        }

        @Override
        synchronized void receiveChanges(final CandidateQueue queue, final Collection<DataTreeCandidate> changes) {
            if (successor != null) {
                // Forward to operational
                successor.receiveChanges(queue, changes);
                return;
            }

            // We are still starting up, so all we need to do is squash reported changes to an initial write event
            final DataTreeCandidate last = Iterables.getLast(changes);
            final Optional<NormalizedNode<?, ?>> lastData = last.getRootNode().getDataAfter();
            if (lastData.isPresent()) {
                queue.clearAndAppend(ImmutableList.of(
                    DataTreeCandidates.fromNormalizedNode(last.getRootPath(), lastData.get())));
            } else {
                queue.clear();
            }
        }
    }

    private static final class Operational extends State {
        private static final Logger LOG = LoggerFactory.getLogger(Operational.class);
        private static final AtomicLongFieldUpdater<Operational> GENERATION =
                AtomicLongFieldUpdater.newUpdater(Operational.class, "generation");
        private static final AtomicIntegerFieldUpdater<Operational> SEMAPHORE =
                AtomicIntegerFieldUpdater.newUpdater(Operational.class, "semaphore");

        private final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees = new HashMap<>();
        private final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> publicSubtrees =
                Collections.unmodifiableMap(subtrees);
        private final ImmutableList<CandidateQueue> queues;
        private final DOMDataTreeListener listener;

        private volatile long generation;
        private volatile long processed;
        @SuppressWarnings("unused")
        private volatile int semaphore;

        Operational(final Collection<CandidateQueue> queues, final DOMDataTreeListener listener) {
            this.queues = ImmutableList.copyOf(queues);
            this.listener = Preconditions.checkNotNull(listener);
        }

        @Override
        void receiveChanges(final CandidateQueue queue, final Collection<DataTreeCandidate> changes) {
            final long gen = GENERATION.incrementAndGet(this);
            try {
                queue.append(changes);
            } finally {
                tryNotifyListener(gen);
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
            while (gen != processed && SEMAPHORE.compareAndSet(this, 0, 1)) {
                try {
                    processed = gen;
                    notifyListener();
                } finally {
                    SEMAPHORE.set(this, 0);
                }

                final long nextGen = generation;
                if (nextGen == gen) {
                    // No modifications happened, we are done
                    return;
                }

                gen = nextGen;
            }
        }

        private void notifyListener() {
            final Stopwatch clock = Stopwatch.createStarted();

            final List<DataTreeCandidate> changes = new ArrayList<>();
            for (CandidateQueue queue : queues) {
                final List<DataTreeCandidate> candidates = queue.collect();
                if (!candidates.isEmpty()) {
                    // Update current subtree snapshot based on last candidate node
                    final DataTreeCandidateNode lastRoot = candidates.get(candidates.size() - 1).getRootNode();
                    final Optional<NormalizedNode<?, ?>> optData = lastRoot.getDataAfter();
                    if (optData.isPresent()) {
                        subtrees.put(queue.getIdentifier(), optData.get());
                    } else {
                        subtrees.remove(queue.getIdentifier());
                    }

                    // Append changes
                    changes.addAll(candidates);
                }
            }

            final int size = changes.size();
            if (size != 0) {
                // Note: it is okay to leak changes, we must never leak mutable subtrees.
                listener.onDataTreeChanged(changes, publicSubtrees);
                LOG.trace("Listener {} processed {} changes in {}", listener, clock);
            } else {
                LOG.trace("Listener {} cycled {} queues in {}", listener, queues.size(), clock);
            }
        }
    }

    private volatile State state;

    DOMDataTreeChangeListenerAggregator(final int sizeHint, final boolean allowRxMerges) {
        state = new Starting(sizeHint, allowRxMerges);
    }

    DOMDataTreeChangeListener createListener(final DOMDataTreeIdentifier treeId) {
        // Note: this is state capture, evaluated once
        final CandidateQueue queue = verifyStarting().createQueue(treeId);

        return changes -> receiveChanges(queue, changes);
    }

    void receiveChanges(final CandidateQueue queue, final Collection<DataTreeCandidate> changes) {
        state.receiveChanges(queue, changes);
    }

    void start(final DOMDataTreeListener listener) {
        state = verifyStarting().start(listener);
    }

    private Starting verifyStarting() {
        final State local = state;
        Verify.verify(local instanceof Starting, "Unexpected state %s", local);
        return (Starting) local;
    }
}