/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for publishers pushing DataTreeCandidate state out to various listeners.
 */
@Beta
public abstract class AbstractDataTreeCandidatePublisher<T extends ListenerRegistration<?>>
    extends AbstractRegistrationTree<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataTreeCandidatePublisher.class);

    /**
     * Callback for subclass to notify a specified registration of a list of candidates. This method is guaranteed
     * to be only called from within {@link #processCandidateTree(DataTreeCandidate)}.
     * @param registration the registration to notify
     * @param changes the list of DataTreeCandidate changes
     */
    protected abstract void notifyListener(@Nonnull T registration,
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
    protected abstract void registrationRemoved(@Nonnull T registration);

    /**
     * Callback invoked between {@link #processCandidateTree(DataTreeCandidate)} and
     * {@link #notifyListener(ListenerRegistration, Collection)} indicating that a registration path has been matched.
     * Default implementation does nothing.
     *
     * @param path Registration path
     * @param candidate {@link DataTreeCandidateNode}
     * @param registrations Registrations affected by the candidate
     */
    protected void onRegistrationMatched(final YangInstanceIdentifier path, final DataTreeCandidateNode candidate,
            final Collection<T> registrations) {
        // No-op
    }

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

        try (RegistrationTreeSnapshot<T> snapshot = takeSnapshot()) {
            final List<PathArgument> toLookup = candidate.getRootPath().getPathArguments();
            final Multimap<T, DataTreeCandidate> listenerChanges = Multimaps.newListMultimap(new IdentityHashMap<>(),
                ArrayList::new);
            lookupAndNotify(toLookup, 0, snapshot.getRootNode(), candidate, listenerChanges);

            listenerChanges.asMap().forEach(this::notifyListener);
        }
    }

    private void lookupAndNotify(final List<PathArgument> args, final int offset, final RegistrationTreeNode<T> node,
            final DataTreeCandidate candidate, final Multimap<T, DataTreeCandidate> listenerChanges) {
        if (args.size() != offset) {
            final PathArgument arg = args.get(offset);

            final RegistrationTreeNode<T> exactChild = node.getExactChild(arg);
            if (exactChild != null) {
                lookupAndNotify(args, offset + 1, exactChild, candidate, listenerChanges);
            }

            for (RegistrationTreeNode<T> c : node.getInexactChildren(arg)) {
                lookupAndNotify(args, offset + 1, c, candidate, listenerChanges);
            }
        } else {
            notifyNode(candidate.getRootPath(), node, candidate.getRootNode(), listenerChanges);
        }
    }

    private void notifyNode(final YangInstanceIdentifier path, final RegistrationTreeNode<T> regNode,
            final DataTreeCandidateNode candNode, final Multimap<T, DataTreeCandidate> listenerChanges) {
        if (candNode.getModificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", path);
            return;
        }

        final Collection<T> regs = regNode.getRegistrations();
        if (!regs.isEmpty()) {
            addToListenerChanges(regs, path, candNode, listenerChanges);
            onRegistrationMatched(path, candNode, regs);
        }

        for (DataTreeCandidateNode candChild : candNode.getChildNodes()) {
            if (candChild.getModificationType() != ModificationType.UNMODIFIED) {
                final RegistrationTreeNode<T> regChild = regNode.getExactChild(candChild.getIdentifier());
                if (regChild != null) {
                    notifyNode(path.node(candChild.getIdentifier()), regChild, candChild, listenerChanges);
                }

                for (RegistrationTreeNode<T> rc : regNode.getInexactChildren(candChild.getIdentifier())) {
                    notifyNode(path.node(candChild.getIdentifier()), rc, candChild, listenerChanges);
                }
            }
        }
    }

    private static <R extends ListenerRegistration<?>> void addToListenerChanges(final Collection<R> registrations,
            final YangInstanceIdentifier path, final DataTreeCandidateNode node,
            final Multimap<R, DataTreeCandidate> listenerChanges) {
        final DataTreeCandidate dataTreeCandidate = DataTreeCandidates.newDataTreeCandidate(path, node);

        for (R reg : registrations) {
            listenerChanges.put(reg, dataTreeCandidate);
        }
    }
}
