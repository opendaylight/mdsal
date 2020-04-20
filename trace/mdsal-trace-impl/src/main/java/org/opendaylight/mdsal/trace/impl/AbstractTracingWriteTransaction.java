/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

abstract class AbstractTracingWriteTransaction implements DOMDataTreeWriteTransaction {
    private final List<String> logs = new ArrayList<>();
    private final DOMDataTreeWriteTransaction delegate;
    private final TracingBroker tracingBroker;

    AbstractTracingWriteTransaction(final DOMDataTreeWriteTransaction delegate, final TracingBroker tracingBroker) {
        this.delegate = requireNonNull(delegate);
        this.tracingBroker = requireNonNull(tracingBroker);
        recordOp(null, null, "instantiate", null);
    }

    private void recordOp(final LogicalDatastoreType store, final YangInstanceIdentifier yiid, final String method,
            final NormalizedNode node) {
        if (yiid != null && !tracingBroker.isWriteWatched(yiid, store)) {
            return;
        }

        final var value = node != null ? node.body() : null;
        if (value instanceof Set<?> set && set.isEmpty()) {
            tracingBroker.logEmptySet(yiid);
        } else {
            final var sb = new StringBuilder();
            sb.append("Method \"").append(method).append('"');
            if (store != null) {
                sb.append(" to ").append(store);
            }
            if (yiid != null) {
                sb.append(" at ").append(tracingBroker.toPathString(yiid));
            }
            sb.append('.');
            if (yiid != null) {
                // If we don’t have an id, we don’t expect data either
                sb.append(" Data: ");
                if (node != null) {
                    sb.append(node.body());
                } else {
                    sb.append("null");
                }
            }
            sb.append(" Stack:").append(tracingBroker.getStackSummary());
            synchronized (this) {
                logs.add(sb.toString());
            }
        }
    }

    @Override
    public void put(final LogicalDatastoreType store, final YangInstanceIdentifier yiid, final NormalizedNode node) {
        recordOp(store, yiid, "put", node);
        delegate.put(store, yiid, node);
    }

    @Override
    public void merge(final LogicalDatastoreType store, final YangInstanceIdentifier yiid, final NormalizedNode node) {
        recordOp(store, yiid, "merge", node);
        delegate.merge(store, yiid, node);
    }

    @Override
    public boolean cancel() {
        synchronized (this) {
            logs.clear();
        }
        return delegate.cancel();
    }

    @Override
    public void delete(final LogicalDatastoreType store, final YangInstanceIdentifier yiid) {
        recordOp(store, yiid, "delete", null);
        delegate.delete(store, yiid);
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        recordOp(null, null, "commit", null);
        synchronized (this) {
            TracingBroker.logOperations(getIdentifier(), logs);
            logs.clear();
        }
        return delegate.commit();
    }

    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public final FluentFuture<?> completionFuture() {
        return delegate.completionFuture();
    }

    // https://jira.opendaylight.org/browse/CONTROLLER-1792

    @Override
    public final boolean equals(final Object object) {
        return object == this || delegate.equals(object);
    }

    @Override
    public final int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public final String toString() {
        return getClass().getName() + "; delegate=" + delegate;
    }
}
