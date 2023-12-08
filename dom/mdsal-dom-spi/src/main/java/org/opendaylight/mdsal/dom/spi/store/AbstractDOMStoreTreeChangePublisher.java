/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link DOMStoreTreeChangePublisher} implementations.
 */
public abstract class AbstractDOMStoreTreeChangePublisher
    extends AbstractRegistrationTree<AbstractDOMDataTreeChangeListenerRegistration<?>>
        implements DOMStoreTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMStoreTreeChangePublisher.class);

    /**
     * Callback for subclass to notify a specified registration of a list of candidates. This method is guaranteed
     * to be only called from within {@link #processCandidateTree(DataTreeCandidate)}.
     * @param registration the registration to notify
     * @param changes the list of DataTreeCandidate changes
     */
    protected abstract void notifyListener(@NonNull AbstractDOMDataTreeChangeListenerRegistration<?> registration,
            @NonNull List<DataTreeCandidate> changes);

    /**
     * Callback notifying the subclass that the specified registration is being
     * closed and it's user no longer
     * wishes to receive notifications. This notification is invoked while
     * the {@link org.opendaylight.yangtools.concepts.ListenerRegistration#close()}
     * method is executing. Subclasses can use this callback to properly
     * remove any delayed notifications pending
     * towards the registration.
     *
     * @param registration Registration which is being closed
     */
    protected abstract void registrationRemoved(
            @NonNull AbstractDOMDataTreeChangeListenerRegistration<?> registration);

    /**
     * Process a candidate tree with respect to registered listeners.
     *
     * @param candidate candidate three which needs to be processed
     * @return true if at least one listener was notified or false.
     */
    protected final boolean processCandidateTree(final @NonNull DataTreeCandidate candidate) {
        final DataTreeCandidateNode node = candidate.getRootNode();
        if (node.modificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", candidate);
            return false;
        }

        try (var snapshot = takeSnapshot()) {
            final List<PathArgument> toLookup = ImmutableList.copyOf(candidate.getRootPath().getPathArguments());
            final ListMultimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges =
                    Multimaps.newListMultimap(new IdentityHashMap<>(), ArrayList::new);
            lookupAndNotify(toLookup, 0, snapshot.getRootNode(), candidate, listenerChanges);

            for (var entry : Multimaps.asMap(listenerChanges).entrySet()) {
                notifyListener(entry.getKey(), entry.getValue());
            }

            return !listenerChanges.isEmpty();
        }
    }

    @Override
    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L>
        registerTreeChangeListener(final YangInstanceIdentifier treeId, final L listener) {
        // Take the write lock
        takeLock();
        try {
            final Node<AbstractDOMDataTreeChangeListenerRegistration<?>> node =
                    findNodeFor(treeId.getPathArguments());
            final var reg = new AbstractDOMDataTreeChangeListenerRegistration<>(listener) {
                @Override
                protected void removeRegistration() {
                    AbstractDOMStoreTreeChangePublisher.this.removeRegistration(node, this);
                    registrationRemoved(this);
                }
            };

            addRegistration(node, reg);
            return reg;
        } finally {
            // Always release the lock
            releaseLock();
        }
    }

    private void lookupAndNotify(final List<PathArgument> args,
            final int offset, final Node<AbstractDOMDataTreeChangeListenerRegistration<?>> node,
            final DataTreeCandidate candidate,
            final Multimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges) {
        if (args.size() != offset) {
            final PathArgument arg = args.get(offset);

            final Node<AbstractDOMDataTreeChangeListenerRegistration<?>> exactChild
                = node.getExactChild(arg);
            if (exactChild != null) {
                lookupAndNotify(args, offset + 1, exactChild, candidate, listenerChanges);
            }

            for (Node<AbstractDOMDataTreeChangeListenerRegistration<?>> c :
                    node.getInexactChildren(arg)) {
                lookupAndNotify(args, offset + 1, c, candidate, listenerChanges);
            }
        } else {
            notifyNode(candidate.getRootPath(), node, candidate.getRootNode(), listenerChanges);
        }
    }

    private void notifyNode(final YangInstanceIdentifier path,
            final Node<AbstractDOMDataTreeChangeListenerRegistration<?>> regNode,
            final DataTreeCandidateNode candNode,
            final Multimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges) {
        if (candNode.modificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", path);
            return;
        }

        final var regs = regNode.getRegistrations();
        if (!regs.isEmpty()) {
            addToListenerChanges(regs, path, candNode, listenerChanges);
        }

        for (var candChild : candNode.childNodes()) {
            if (candChild.modificationType() != ModificationType.UNMODIFIED) {
                final var regChild = regNode.getExactChild(candChild.name());
                if (regChild != null) {
                    notifyNode(path.node(candChild.name()), regChild, candChild, listenerChanges);
                }

                for (var rc : regNode.getInexactChildren(candChild.name())) {
                    notifyNode(path.node(candChild.name()), rc, candChild, listenerChanges);
                }
            }
        }
    }

    private static void addToListenerChanges(
            final Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations,
            final YangInstanceIdentifier path, final DataTreeCandidateNode node,
            final Multimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges) {
        final DataTreeCandidate dataTreeCandidate = DataTreeCandidates.newDataTreeCandidate(path, node);

        for (AbstractDOMDataTreeChangeListenerRegistration<?> reg : registrations) {
            listenerChanges.put(reg, dataTreeCandidate);
        }
    }
}
