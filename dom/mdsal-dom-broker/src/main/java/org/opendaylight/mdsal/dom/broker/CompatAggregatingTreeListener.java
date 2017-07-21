package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
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

final class CompatAggregatingTreeListener {
    private abstract static class State {

        abstract void receiveChanges(CandidateQueue queue, Collection<DataTreeCandidate> changes);
    }

    private static final class Starting extends State {
        private final Map<DOMDataTreeIdentifier, CandidateQueue> queues = new HashMap<>();
        private final boolean allowRxMerges;

        @GuardedBy("this")
        private Operational successor;

        Starting(final boolean allowRxMerges) {
            this.allowRxMerges = allowRxMerges;
        }

        CandidateQueue getQueue(final DOMDataTreeIdentifier treeId) {
            // TODO: do not ignore allowRxMerges, but rather create a dedicated subclass or something
            return queues.computeIfAbsent(treeId, id -> new CandidateQueue());
        }

        synchronized Operational start(final DOMDataTreeListener listener) {
            final Operational ret = new Operational(ImmutableMap.copyOf(queues), listener);
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
            queue.clear();
            if (lastData.isPresent()) {
                queue.add(DataTreeCandidates.fromNormalizedNode(last.getRootPath(), lastData.get()));
            }
        }
    }

    private static final class Operational extends State {
        private static final Logger LOG = LoggerFactory.getLogger(Operational.class);
        private static final AtomicIntegerFieldUpdater<Operational> REFCOUNT =
                AtomicIntegerFieldUpdater.newUpdater(Operational.class, "refcount");

        private final Map<DOMDataTreeIdentifier, CandidateQueue> queues;

        @GuardedBy("this")
        private final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees = new HashMap<>();
        @GuardedBy("this")
        private final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> publicSubtrees =
                Collections.unmodifiableMap(subtrees);

        private final DOMDataTreeListener listener;

        private volatile int refcount;

        Operational(final Map<DOMDataTreeIdentifier, CandidateQueue> queues, final DOMDataTreeListener listener) {
            this.queues = Preconditions.checkNotNull(queues);
            this.listener = Preconditions.checkNotNull(listener);
        }

        @Override
        void receiveChanges(final CandidateQueue queue, final Collection<DataTreeCandidate> changes) {
            REFCOUNT.getAndIncrement(this);
            try {
                queue.append(changes);
            } finally {
                // We now have to re-sync, as we may end up being the last thread in position to observe
                // the complete state of the queues. Since queues are updated independently to iteration,
                // notifyListener() may have missed some updates, in which case we must eventually run it.
                int current = REFCOUNT.decrementAndGet(this);
                Verify.verify(current >= 0, "Dirty over/underflow in %s", this);

                // We are looping while we observe movement in others.
                while (current == 0) {
                    current = notifyListener();
                }
            }
        }

        private synchronized int notifyListener() {
            final Stopwatch clock = Stopwatch.createStarted();

            final List<DataTreeCandidate> changes = new ArrayList<>();
            for (Entry<DOMDataTreeIdentifier, CandidateQueue> e : queues.entrySet()) {
                final List<DataTreeCandidate> candidates = e.getValue().collect();

                // Update current subtree snapshot based on last candidate node
                final DataTreeCandidateNode lastRoot = Iterables.getLast(e.getValue()).getRootNode();
                final Optional<NormalizedNode<?, ?>> optData = lastRoot.getDataAfter();
                if (optData.isPresent()) {
                    subtrees.put(e.getKey(), optData.get());
                } else {
                    subtrees.remove(e.getKey());
                }

                // Append changes
                changes.addAll(candidates);
            }

            final int size = changes.size();
            if (size != 0) {
                // Note: it is okay to leak changes, we must never leak mutable subtrees.
                listener.onDataTreeChanged(changes, publicSubtrees);
                LOG.trace("Listener {} processed {} changes in {}", listener, clock);
            } else {
                LOG.trace("Listener {} cycled {} queues in {}", listener, queues.size(), clock);
            }

            // It any of the buffers are dirty while we were collecting/executing, we need to re-execute, unless
            // someone else noted it dirtied the buffer.
            return queues.values().stream().anyMatch(CandidateQueue::isDirty) ? 1 : refcount;
        }
    }

    private volatile State state;

    CompatAggregatingTreeListener(final boolean allowRxMerges) {
        state = new Starting(allowRxMerges);
    }

    DOMDataTreeChangeListener createListener(final DOMDataTreeIdentifier treeId) {
        // Note: this is state capture, evaluated once
        final CandidateQueue queue = verifyStarting().getQueue(treeId);

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