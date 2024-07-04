/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.adapter.query.DefaultQuery;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeQueryOperations;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadOperations;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.mdsal.dom.spi.query.DOMQueryEvaluator;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractForwardedTransaction<T extends DOMDataTreeTransaction> implements Delegator<T>,
        Identifiable<Object> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractForwardedTransaction.class);

    private final @NonNull AdapterContext adapterContext;
    private final @NonNull T delegate;

    AbstractForwardedTransaction(final AdapterContext adapterContext, final T delegateTx) {
        this.adapterContext = requireNonNull(adapterContext, "Codec must not be null");
        delegate = requireNonNull(delegateTx, "Delegate must not be null");
    }

    @Override
    public final Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public final T getDelegate() {
        return delegate;
    }

    protected final <S extends DOMDataTreeTransaction> S getDelegateChecked(final Class<S> txType) {
        checkState(txType.isInstance(delegate));
        return txType.cast(delegate);
    }

    protected final AdapterContext adapterContext() {
        return adapterContext;
    }

    protected final <D extends DataObject> @NonNull FluentFuture<Optional<D>> doRead(
            final DOMDataTreeReadOperations readOps, final LogicalDatastoreType store,
            final DataObjectIdentifier<D> path) {
        final var codecWithPath = adapterContext.currentSerializer().getSubtreeCodecWithPath(path);
        final var domPath = codecWithPath.path();
        final var codec = codecWithPath.codec();
        return readOps.read(store, domPath)
            .transform(optData -> optData.flatMap(data -> decodeRead(codec, data)), MoreExecutors.directExecutor());
    }

    @SuppressWarnings("unchecked")
    private static <D extends DataObject> Optional<D> decodeRead(final CommonDataObjectCodecTreeNode<D> codec,
            final @NonNull NormalizedNode data) {
        if (codec instanceof BindingDataObjectCodecTreeNode<?> dataObject) {
            return Optional.of((D) dataObject.deserialize(data));
        } else if (codec instanceof BindingAugmentationCodecTreeNode<?> augment) {
            return Optional.ofNullable((D) augment.filterFrom(data));
        } else {
            throw new VerifyException("Unhandled codec " + codec);
        }
    }

    protected final @NonNull FluentFuture<Boolean> doExists(final DOMDataTreeReadOperations readOps,
            final LogicalDatastoreType store, final DataObjectIdentifier<?> path) {
        final var codecWithPath = adapterContext.currentSerializer().getSubtreeCodecWithPath(path);
        final var domPath = codecWithPath.path();
        if (codecWithPath.codec() instanceof BindingAugmentationCodecTreeNode<?> augment) {
            // Complicated case: we need to check if any of the children exist, as DOM layer just does not know about
            //                   this indirection
            return FluentFuture.from(Futures.transform(
                Futures.allAsList(augment.childPathArguments().stream()
                    .map(child -> readOps.exists(store, domPath.node(child)))
                    .collect(ImmutableList.toImmutableList())),
                children -> children.contains(Boolean.TRUE),
                MoreExecutors.directExecutor()));
        } else {
            return readOps.exists(store, domPath);
        }
    }

    protected static final <T extends @NonNull DataObject> @NonNull FluentFuture<QueryResult<T>> doExecute(
            final DOMDataTreeReadOperations readOps, final @NonNull LogicalDatastoreType store,
            final @NonNull QueryExpression<T> query) {
        checkArgument(query instanceof DefaultQuery, "Unsupported query type %s", query);
        final var defaultQuery = (DefaultQuery<T>) query;

        final var domFuture = requireNonNull(readOps) instanceof DOMDataTreeQueryOperations dtqOps
            ? dtqOps.execute(store, defaultQuery.asDOMQuery())
                : fallbackExecute(readOps, store, defaultQuery.asDOMQuery());

        return domFuture.transform(defaultQuery::toQueryResult, MoreExecutors.directExecutor());
    }

    private static FluentFuture<DOMQueryResult> fallbackExecute(final @NonNull DOMDataTreeReadOperations readOps,
            final @NonNull LogicalDatastoreType store, final @NonNull DOMQuery domQuery) {
        LOG.trace("Fallback evaluation of {} on {}", domQuery, readOps);
        return readOps.read(store, domQuery.getRoot())
            .transform(
                node -> node.map(data -> DOMQueryEvaluator.evaluateOn(domQuery, data)).orElse(DOMQueryResult.of()),
                // TODO: execute on a dedicated thread pool
                MoreExecutors.directExecutor());
    }
}
