/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@NonNullByDefault
final class DefaultQueryResultItem<T extends DataObject> implements QueryResult.Item<T> {
    private static final VarHandle OBJECT;
    private static final VarHandle PATH;

    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            OBJECT = lookup.findVarHandle(DefaultQueryResultItem.class, "object", DataObject.class);
            PATH = lookup.findVarHandle(DefaultQueryResultItem.class, "path", InstanceIdentifier.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Entry<YangInstanceIdentifier, NormalizedNode> domItem;
    private final DefaultQueryResult<T> result;

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = { "NP_STORE_INTO_NONNULL_FIELD", "URF_UNREAD_FIELD"},
        justification = "Ungrokked type annotation")
    private volatile @Nullable InstanceIdentifier<T> path = null;
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = { "NP_STORE_INTO_NONNULL_FIELD", "URF_UNREAD_FIELD"},
        justification = "Ungrokked type annotation")
    private volatile @Nullable T object = null;

    DefaultQueryResultItem(final DefaultQueryResult<T> result,
            final Entry<YangInstanceIdentifier, NormalizedNode> domItem) {
        this.result = requireNonNull(result);
        this.domItem = requireNonNull(domItem);
    }

    @Override
    public T object() {
        final @Nullable T local = (T) OBJECT.getAcquire(this);
        return local != null ? local : loadObject();
    }

    @Override
    public InstanceIdentifier<T> path() {
        final @Nullable InstanceIdentifier<T> local = (InstanceIdentifier<T>) PATH.getAcquire(this);
        return local != null ? local : loadPath();
    }

    private T loadObject() {
        final T ret = result.createObject(domItem);
        final Object witness = OBJECT.compareAndExchangeRelease(this, null, ret);
        return witness == null ? ret : (T) witness;
    }

    @SuppressWarnings("unchecked")
    private InstanceIdentifier<T> loadPath() {
        final InstanceIdentifier<T> ret = result.createPath(domItem.getKey());
        final Object witness = PATH.compareAndExchangeRelease(this, null, ret);
        return witness == null ? ret : (InstanceIdentifier<T>) witness;
    }
}