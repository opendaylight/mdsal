/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
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
        extends AbstractRegistrationTree<AbstractDOMStoreTreeChangePublisher.RegImpl>
        implements DOMStoreTreeChangePublisher {
    /**
     * A handle to a registered {@link DOMDataTreeChangeListener}. Implementations of this interface are guaranteed to
     * use identity-based equality.
     */
    @NonNullByDefault
    protected sealed interface Reg permits RegImpl {
        /**
         * Return the underlying listener.
         *
         * @return the underlying listener
         */
        DOMDataTreeChangeListener listener();

        /**
         * Check if this handle has not been closed yet.
         *
         * @return {@code true} if this handle is still open
         */
        boolean notClosed();
    }

    /**
     * Registration handle for a {@link DOMDataTreeChangeListener}. This class is exposed to subclasses only as a
     * convenience, so they can use its identity-based equality while at the same time having access to the listener.
     *
     * <p>
     * Implementations must not invoke {@link #close()} nor should otherwise interact with the registration.
     */
    @NonNullByDefault
    final class RegImpl extends AbstractObjectRegistration<DOMDataTreeChangeListener> implements Reg {
        private RegImpl(final DOMDataTreeChangeListener instance) {
            super(instance);
        }

        @Override
        public DOMDataTreeChangeListener listener() {
            return getInstance();
        }

        @Override
        protected void removeRegistration() {
            registrationRemoved(this);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMStoreTreeChangePublisher.class);

    /**
     * Callback for subclass to notify a specified registration of a list of candidates. This method is guaranteed
     * to be only called from within {@link #processCandidateTree(DataTreeCandidate)}.
     *
     * @param registration the registration to notify
     * @param changes the list of DataTreeCandidate changes
     */
    protected abstract void notifyListener(@NonNull Reg registration, @NonNull List<DataTreeCandidate> changes);

    /**
     * Callback notifying the subclass that the specified registration is being closed and it's user no longer wishes to
     * receive notifications. This notification is invoked while the
     * {@link org.opendaylight.yangtools.concepts.Registration#close()} method is executing. Subclasses can use this
     * callback to properly remove any delayed notifications pending towards the registration.
     *
     * @param registration Registration which is being closed
     */
    protected abstract void registrationRemoved(@NonNull Reg registration);

    /**
     * Process a candidate tree with respect to registered listeners.
     *
     * @param candidate candidate three which needs to be processed
     * @return true if at least one listener was notified or false.
     */
    protected final boolean processCandidateTree(final @NonNull DataTreeCandidate candidate) {
        final var node = candidate.getRootNode();
        if (node.modificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", candidate);
            return false;
        }

        try (var snapshot = takeSnapshot()) {
            final var toLookup = List.copyOf(candidate.getRootPath().getPathArguments());
            final var listenerChanges = new IdentityHashMap<Reg, List<DataTreeCandidate>>();
            lookupAndNotify(toLookup, 0, snapshot.getRootNode(), candidate, listenerChanges);

            for (var entry : listenerChanges.entrySet()) {
                notifyListener(entry.getKey(), entry.getValue());
            }

            return !listenerChanges.isEmpty();
        }
    }

    @Override
    public final Registration registerTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener) {
        // Take the write lock
        takeLock();
        try {
            final var reg = new RegImpl(listener);
            addRegistration(findNodeFor(treeId.getPathArguments()), reg);
            return reg;
        } finally {
            // Always release the lock
            releaseLock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation calls {@link #registerTreeChangeListener(YangInstanceIdentifier, DOMDataTreeChangeListener)},
     * override if necessary.
     */
    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public Registration registerLegacyTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener) {
        return registerTreeChangeListener(treeId, listener);
    }

    private void lookupAndNotify(final List<PathArgument> args, final int offset, final Node<RegImpl> node,
            final DataTreeCandidate candidate, final Map<Reg, List<DataTreeCandidate>> listenerChanges) {
        if (args.size() == offset) {
            notifyNode(candidate.getRootPath(), node, candidate.getRootNode(), listenerChanges);
            return;
        }

        final var arg = args.get(offset);
        final var exactChild = node.getExactChild(arg);
        if (exactChild != null) {
            lookupAndNotify(args, offset + 1, exactChild, candidate, listenerChanges);
        }
        for (var child : node.getInexactChildren(arg)) {
            lookupAndNotify(args, offset + 1, child, candidate, listenerChanges);
        }
    }

    private void notifyNode(final YangInstanceIdentifier path, final Node<RegImpl> regNode,
            final DataTreeCandidateNode candNode, final Map<Reg, List<DataTreeCandidate>> listenerChanges) {
        if (candNode.modificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", path);
            return;
        }

        final var regs = regNode.getRegistrations();
        if (!regs.isEmpty()) {
            final var dataTreeCandidate = DataTreeCandidates.newDataTreeCandidate(path, candNode);
            for (var reg : regs) {
                listenerChanges.computeIfAbsent(reg, ignored -> new ArrayList<>()).add(dataTreeCandidate);
            }
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
}
