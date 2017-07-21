/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
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
final class DOMDataTreeChangeListenerAggregator
        extends AbstractStateAggregator<DOMDataTreeChangeListenerAggregator.State> {

    static final class State extends AbstractStateAggregator.State implements Identifiable<DOMDataTreeIdentifier> {
        private final DOMDataTreeIdentifier identifier;
        final List<DataTreeCandidate> changes;

        State(final DOMDataTreeIdentifier identifier, final List<DataTreeCandidate> changes) {
            this.identifier = Preconditions.checkNotNull(identifier);
            this.changes = Preconditions.checkNotNull(changes);
        }

        @Override
        public DOMDataTreeIdentifier getIdentifier() {
            return identifier;
        }
    }

    private static final class StateBuilder extends AbstractStateAggregator.StateBuilder<State> {
        @GuardedBy("this")
        private final List<DataTreeCandidate> changes = new ArrayList<>();
        private final DOMDataTreeIdentifier identifier;

        StateBuilder(final DOMDataTreeIdentifier identifier) {
            this.identifier = Preconditions.checkNotNull(identifier);
        }

        @Override
        protected synchronized void append(final State state) {
            changes.addAll(state.changes);
        }

        @Override
        protected synchronized void appendInitial(final State state) {
            // We are still starting up, so all we need to do is squash reported changes to an initial write event
            final DataTreeCandidate last = Iterables.getLast(state.changes);
            changes.clear();
            final Optional<NormalizedNode<?, ?>> lastData = last.getRootNode().getDataAfter();
            if (lastData.isPresent()) {
                changes.add(DataTreeCandidates.fromNormalizedNode(last.getRootPath(), lastData.get()));
            }
        }

        @Override
        public synchronized State build() {
            final State ret = new State(identifier, ImmutableList.copyOf(changes));
            changes.clear();
            return ret;
        }
    }

    private static final class Operational extends AbstractStateAggregator.Operational<State> {
        private final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees = new HashMap<>();
        private final DOMDataTreeListener listener;

        Operational(final Collection<AbstractStateAggregator.StateBuilder<State>> builders,
                final DOMDataTreeListener listener) {
            super(builders);
            this.listener = Preconditions.checkNotNull(listener);
        }

        @Override
        protected void notifyListener(final Iterator<State> iterator) {
            final Stopwatch clock = Stopwatch.createStarted();
            final List<DataTreeCandidate> changes = new ArrayList<>();
            while (iterator.hasNext()) {
                final State state = iterator.next();
                final List<DataTreeCandidate> candidates = state.changes;
                if (!candidates.isEmpty()) {
                    // Update current subtree snapshot based on last candidate node
                    final DataTreeCandidateNode lastRoot = candidates.get(candidates.size() - 1).getRootNode();
                    final Optional<NormalizedNode<?, ?>> optData = lastRoot.getDataAfter();
                    if (optData.isPresent()) {
                        subtrees.put(state.getIdentifier(), optData.get());
                    } else {
                        subtrees.remove(state.getIdentifier());
                    }

                    // Append changes
                    changes.addAll(candidates);
                }
            }

            final int size = changes.size();
            if (size != 0) {
                // Note: it is okay to leak changes, we must never leak mutable subtrees.
                listener.onDataTreeChanged(changes, ImmutableMap.copyOf(subtrees));
                LOG.trace("Listener {} processed {} changes in {}", listener, clock);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DOMDataTreeChangeListenerAggregator.class);

    private final boolean allowRxMerges;

    DOMDataTreeChangeListenerAggregator(final int sizeHint, final boolean allowRxMerges) {
        super(sizeHint);
        this.allowRxMerges = allowRxMerges;
    }

    DOMDataTreeChangeListener createListener(final DOMDataTreeIdentifier treeId) {
        // TODO: do not ignore allowRxMerges, but rather create a dedicated subclass or something
        final StateBuilder builder = new StateBuilder(treeId);
        addBuilder(builder);

        return changes -> receiveState(builder, new State(treeId, ImmutableList.copyOf(changes)));
    }

    <L extends DOMDataTreeListener> ListenerRegistration<L> start(final L listener,
            final Collection<ListenerRegistration<?>> regs) {
        start(builders -> {
            final Operational ret = new Operational(builders, listener);
            ret.notifyListener(Iterators.transform(builders.iterator(), AbstractStateAggregator.StateBuilder::build));
            return ret;
        });

        return new AbstractListenerRegistration<L>(listener) {
            @Override
            protected void removeRegistration() {
                regs.forEach(ListenerRegistration::close);
            }
        };
    }
}
