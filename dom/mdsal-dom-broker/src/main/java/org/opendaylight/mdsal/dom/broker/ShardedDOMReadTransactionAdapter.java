/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardedDOMReadTransactionAdapter implements DOMDataTreeReadTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMReadTransactionAdapter.class.getName());

    private final List<ListenerRegistration<DOMDataTreeListener>> registrations = new ArrayList<>();
    private final @NonNull DOMDataTreeService service;
    private final @NonNull Object txIdentifier;

    private boolean finished = false;

    ShardedDOMReadTransactionAdapter(final Object identifier, final DOMDataTreeService service) {
        this.service = requireNonNull(service);
        this.txIdentifier = requireNonNull(identifier);
    }

    @Override
    public void close() {
        LOG.debug("{}: Closing read transaction", txIdentifier);
        if (finished) {
            return;
        }

        registrations.forEach(ListenerRegistration::close);
        // TODO should we also cancel all read futures?
        finished = true;
    }

    @Override
    public Object getIdentifier() {
        return txIdentifier;
    }

    @Override
    public FluentFuture<Optional<NormalizedNode<?, ?>>> read(final LogicalDatastoreType store,
            final YangInstanceIdentifier path) {
        checkRunning();
        LOG.debug("{}: Invoking read at {}:{}", txIdentifier, store, path);
        final ListenerRegistration<DOMDataTreeListener> reg;
        final SettableFuture<Optional<NormalizedNode<?, ?>>> initialDataTreeChangeFuture = SettableFuture.create();
        try {
            reg = service.registerListener(new ReadShardedListener(initialDataTreeChangeFuture),
                    Collections.singleton(new DOMDataTreeIdentifier(store, path)), false, Collections.emptyList());
            registrations.add(reg);
        } catch (final DOMDataTreeLoopException e) {
            // This should not happen, we are not specifying any producers when registering listener
            throw new IllegalStateException("Loop in listener and producers detected", e);
        }

        // After data tree change future is finished, we can close the listener registration
        initialDataTreeChangeFuture.addListener(reg::close, MoreExecutors.directExecutor());
        return FluentFuture.from(initialDataTreeChangeFuture);
    }

    @Override
    public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        checkRunning();
        LOG.debug("{}: Invoking exists at {}:{}", txIdentifier, store, path);
        return read(store, path).transform(Optional::isPresent, MoreExecutors.directExecutor());
    }

    private void checkRunning() {
        checkState(!finished, "Transaction is already closed");
    }

    static final class ReadShardedListener implements DOMDataTreeListener {
        private final SettableFuture<Optional<NormalizedNode<?, ?>>> readResultFuture;

        ReadShardedListener(final SettableFuture<Optional<NormalizedNode<?, ?>>> future) {
            this.readResultFuture = requireNonNull(future);
        }

        @Override
        public void onDataTreeChanged(final Collection<DataTreeCandidate> changes,
                final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
            checkState(changes.size() == 1 && subtrees.size() == 1,
                    "DOMDataTreeListener registered exactly on one subtree");
            if (changes.iterator().next().getRootNode().getModificationType().equals(ModificationType.UNMODIFIED)) {
                readResultFuture.set(Optional.empty());
            } else {
                readResultFuture.set(Optional.of(subtrees.values().iterator().next()));
            }
        }

        @Override
        public void onDataTreeFailed(final Collection<DOMDataTreeListeningException> causes) {
            // TODO If we get just one exception, we don't need to do chaining

            // We chain all exceptions and return aggregated one
            readResultFuture.setException(new DOMDataTreeListeningException("Aggregated DOMDataTreeListening exception",
                    causes.stream().reduce((e1, e2) -> {
                        e1.addSuppressed(e2);
                        return e1;
                    }).get()));
        }
    }
}
