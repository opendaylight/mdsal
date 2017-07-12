/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.mdsal.dom.spi.RegistrationTreeNode;
import org.opendaylight.mdsal.dom.spi.RegistrationTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link DOMStoreTreeChangePublisher} implementations.
 */
public abstract class AbstractDOMStoreTreeChangePublisher
    extends AbstractRegistrationTree<AbstractDOMDataTreeChangeListenerRegistration<?>>
        implements DOMStoreTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMStoreTreeChangePublisher.class);

    private static final Supplier<List<DataTreeCandidate>> LIST_SUPPLIER = () -> new ArrayList<>();

    /**
     * Callback for subclass to notify a specified registration of a list of candidates. This method is guaranteed
     * to be only called from within {@link #processCandidateTree(DataTreeCandidate)}.
     * @param registration the registration to notify
     * @param changes the list of DataTreeCandidate changes
     */
    protected abstract void notifyListener(@Nonnull AbstractDOMDataTreeChangeListenerRegistration<?> registration,
            @Nonnull Collection<DataTreeCandidate> changes);

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
            @Nonnull AbstractDOMDataTreeChangeListenerRegistration<?> registration);

    /**
     * Process a candidate tree with respect to registered listeners.
     *
     * @param candidate candidate three which needs to be processed
     */
    protected final void processCandidateTree(@Nonnull final DataTreeCandidate candidate) {
        final DataTreeCandidateNode node = candidate.getRootNode();
        if (node.getModificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", candidate);
            return;
        }

        try (RegistrationTreeSnapshot<AbstractDOMDataTreeChangeListenerRegistration<?>> snapshot
                = takeSnapshot()) {
            final List<PathArgument> toLookup = ImmutableList.copyOf(candidate.getRootPath().getPathArguments());
            final Multimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges =
                    Multimaps.newListMultimap(new IdentityHashMap<>(), LIST_SUPPLIER);
            lookupAndNotify(toLookup, 0, snapshot.getRootNode(), candidate, listenerChanges);

            for (Map.Entry<AbstractDOMDataTreeChangeListenerRegistration<?>, Collection<DataTreeCandidate>> entry:
                    listenerChanges.asMap().entrySet()) {
                notifyListener(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L>
        registerTreeChangeListener(final YangInstanceIdentifier treeId, final L listener) {
        // Take the write lock
        takeLock();
        try {
            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node =
                    findNodeFor(treeId.getPathArguments());
            final AbstractDOMDataTreeChangeListenerRegistration<L> reg =
                    new AbstractDOMDataTreeChangeListenerRegistration<L>(listener) {
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
            final int offset, final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node,
            final DataTreeCandidate candidate,
            final Multimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges) {
        if (args.size() != offset) {
            final PathArgument arg = args.get(offset);

            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> exactChild
                = node.getExactChild(arg);
            if (exactChild != null) {
                lookupAndNotify(args, offset + 1, exactChild, candidate, listenerChanges);
            }

            for (RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> c :
                    node.getInexactChildren(arg)) {
                lookupAndNotify(args, offset + 1, c, candidate, listenerChanges);
            }
        } else {
            notifyNode(candidate.getRootPath(), node, candidate.getRootNode(), listenerChanges);
        }
    }

    private void notifyNode(final YangInstanceIdentifier path,
            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> regNode,
            final DataTreeCandidateNode candNode,
            final Multimap<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> listenerChanges) {
        if (candNode.getModificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", path);
            return;
        }

        final Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> regs = regNode.getRegistrations();
        if (!regs.isEmpty()) {
            addToListenerChanges(regs, path, candNode, listenerChanges);
        }

        for (DataTreeCandidateNode candChild : candNode.getChildNodes()) {
            if (candChild.getModificationType() != ModificationType.UNMODIFIED) {
                final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> regChild =
                        regNode.getExactChild(candChild.getIdentifier());
                if (regChild != null) {
                    notifyNode(path.node(candChild.getIdentifier()), regChild, candChild, listenerChanges);
                }

                for (RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> rc :
                    regNode.getInexactChildren(candChild.getIdentifier())) {
                    notifyNode(path.node(candChild.getIdentifier()), rc, candChild, listenerChanges);
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
