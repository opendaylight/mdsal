/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link DOMDataTreeChangePublisher} implementations.
 */
@Beta
public abstract class AbstractDOMDataTreeChangePublisher
    extends AbstractRegistrationTree<AbstractDOMDataTreeChangeListenerRegistration<?>>
        implements DOMDataTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMDataTreeChangePublisher.class);

    /**
     * Callback for subclass to notify specified registrations
     * of a candidate at a specified path. This method is guaranteed
     * to be only called from within {@link #processCandidateTree(DataTreeCandidate)}.
     * @param registrations Registrations which are affected by the candidate node
     * @param path Path of changed candidate node. Guaranteed to match the path specified by the registration
     * @param node Candidate node
     */
    protected abstract void notifyListeners(
            @Nonnull Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations,
            @Nonnull YangInstanceIdentifier path, @Nonnull DataTreeCandidateNode node);

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

        try (final RegistrationTreeSnapshot<AbstractDOMDataTreeChangeListenerRegistration<?>> snapshot
                = takeSnapshot()) {
            final List<PathArgument> toLookup
                = ImmutableList.copyOf(candidate.getRootPath().getPathArguments());
            lookupAndNotify(toLookup, 0, snapshot.getRootNode(), candidate);
        }
    }

    @Override
    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L>
        registerTreeChangeListener(final L listener, final Collection<YangInstanceIdentifier> trees) {

        // Take the write lock
        takeLock();
        try {
            final Collection<RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>>> nodes =
                    new ArrayList<>(1);
            final AbstractDOMDataTreeChangeListenerRegistration<L> reg =
                    new AbstractDOMDataTreeChangeListenerRegistration<L>(listener) {
                @Override
                protected void removeRegistration() {
                    AbstractDOMDataTreeChangePublisher.this.removeRegistrations(nodes, this);
                    registrationRemoved(this);
                }
            };

            for (YangInstanceIdentifier tree : trees) {
                final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node =
                        findNodeFor(tree.getPathArguments());
                addRegistration(node, reg);
                nodes.add(node);
            }

            return reg;
        } finally {
            // Always release the lock
            releaseLock();
        }
    }

    private void lookupAndNotify(final List<PathArgument> args,
            final int offset, final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node,
                    final DataTreeCandidate candidate) {
        if (args.size() != offset) {
            final PathArgument arg = args.get(offset);

            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> exactChild
                = node.getExactChild(arg);
            if (exactChild != null) {
                lookupAndNotify(args, offset + 1, exactChild, candidate);
            }

            for (RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> c :
                    node.getInexactChildren(arg)) {
                lookupAndNotify(args, offset + 1, c, candidate);
            }
        } else {
            notifyNode(candidate.getRootPath(), node, candidate.getRootNode());
        }
    }

    private void notifyNode(final YangInstanceIdentifier path,
            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> regNode,
            final DataTreeCandidateNode candNode) {
        if (candNode.getModificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", path);
            return;
        }

        final Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> regs = regNode.getRegistrations();
        if (!regs.isEmpty()) {
            notifyListeners(regs, path, candNode);
        }

        for (DataTreeCandidateNode candChild : candNode.getChildNodes()) {
            if (candChild.getModificationType() != ModificationType.UNMODIFIED) {
                final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> regChild =
                        regNode.getExactChild(candChild.getIdentifier());
                if (regChild != null) {
                    notifyNode(path.node(candChild.getIdentifier()), regChild, candChild);
                }

                for (RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> rc :
                    regNode.getInexactChildren(candChild.getIdentifier())) {
                    notifyNode(path.node(candChild.getIdentifier()), rc, candChild);
                }
            }
        }
    }
}
