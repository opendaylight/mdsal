/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.store.AbstractDOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An internal {@link DOMStoreTreeChangePublisher} implementation.
 */
@NonNullByDefault
abstract sealed class TreeChangePublisher extends AbstractDOMStoreTreeChangePublisher
        permits InMemoryDOMStoreTreeChangePublisher, DirectTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(TreeChangePublisher.class);

    @Override
    protected final void registrationRemoved(final Reg registration) {
        LOG.debug("Closing registration {}", registration);
        // FIXME: remove the queue for this registration and make sure we clear it
    }

    final Registration registerTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener, final DataTreeSnapshot snapshot) {
        final var ret = super.registerTreeChangeListener(treeId, listener);
        final var changes = computeChanges(snapshot, treeId);
        if (changes.isEmpty()) {
            listener.onInitialData();
        } else {
            notifyListener((Reg) ret, changes);
        }
        return ret;
    }

    private List<DataTreeCandidate> computeChanges(final DataTreeSnapshot snapshot,
            final YangInstanceIdentifier treeId) {
        final var optRoot = snapshot.readNode(YangInstanceIdentifier.of());
        if (optRoot.isEmpty()) {
            return List.of();
        }

        final var root = optRoot.orElseThrow();
        if (!(root instanceof DataContainerNode container)) {
            throw new IllegalStateException("Unexpected root node type " + root.contract());
        }

        if (container.isEmpty()) {
            // If we are listening on root of data tree we still get empty normalized node, root is always
            // present, we should filter this out separately and notify it by 'onInitialData()' once.
            // Otherwise, it is just a valid data node with empty value which also should be notified by
            // "onDataTreeChanged(List<DataTreeCandidate>)".
            return List.of();
        }

        final var candidate = DataTreeCandidates.fromNormalizedNode(YangInstanceIdentifier.of(), root);
        final var args = treeId.getPathArguments();
        final var size = args.size();
        return switch (size) {
            case 0 -> List.of(candidate);
            default -> computeChanges(new ArrayDeque<>(size), candidate.getRootNode(), args.iterator());
        };
    }

    private List<DataTreeCandidate> computeChanges(final ArrayDeque<PathArgument> stack,
            final DataTreeCandidateNode node, final Iterator<PathArgument> it) {
        throw new UnsupportedOperationException();
    }

    /**
     * Process a {@link DataTreeCandidate} tree with respect to registered {@link DOMDataTreeChangeListener}s.
     *
     * @param candidate candidate three which needs to be processed
     * @return true if at least one listener was notified or false.
     */
    final boolean publishCandidate(final DataTreeCandidate candidate) {
        return processCandidateTree(candidate);
    }

    abstract void close();
}
