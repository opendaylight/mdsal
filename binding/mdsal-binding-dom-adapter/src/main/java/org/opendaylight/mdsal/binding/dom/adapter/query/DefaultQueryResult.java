/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.base.VerifyException;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@NonNullByDefault
final class DefaultQueryResult<T extends DataObject>
        implements QueryResult<T>, Function<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>, QueryResult.Item<T>> {
    private static final VarHandle ITEM_CODEC;

    static {
        try {
            ITEM_CODEC = MethodHandles.lookup().findVarHandle(DefaultQueryResult.class,
                "itemCodec", BindingDataObjectCodecTreeNode.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> domResult;
    private final BindingCodecTree codec;

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "Ungrokked type annotation")
    private volatile @Nullable BindingDataObjectCodecTreeNode<T> itemCodec = null;

    DefaultQueryResult(final BindingCodecTree codec,
            final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> domResult) {
        this.codec = requireNonNull(codec);
        this.domResult = requireNonNull(domResult);
    }

    @Override
    public Spliterator<? extends Item<T>> spliterator() {
        return Lists.transform(domResult, this).spliterator();
    }

    @Override
    public Stream<? extends Item<T>> stream() {
        return domResult.stream().map(this);
    }

    /**
     * Returns a parallel {@link Stream} of values from the result.
     *
     * @return A stream of non-null values.
     */
    @Override
    public Stream<? extends Item<T>> parallelStream() {
        return domResult.parallelStream().map(this);
    }

    @Override
    public Item<T> apply(final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domItem) {
        return new DefaultQueryResultItem<>(this, domItem);
    }

    T createObject(final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domItem) {
        final @Nullable BindingDataObjectCodecTreeNode<T> local =
            (BindingDataObjectCodecTreeNode<T>) ITEM_CODEC.getAcquire(this);
        return (local != null ? local : loadItemCodec(domItem.getKey())).deserialize(domItem.getValue());
    }

    InstanceIdentifier<T> createPath(final YangInstanceIdentifier domPath) {
        return verifyNotNull(codec.getInstanceIdentifierCodec().toBinding(domPath), "path");
    }

    private BindingDataObjectCodecTreeNode<T> loadItemCodec(final YangInstanceIdentifier domPath) {
        final BindingCodecTreeNode codecNode = codec.getSubtreeCodec(domPath);
        if (!(codecNode instanceof BindingDataObjectCodecTreeNode)) {
            throw new VerifyException("Unexpected codec " + codecNode);
        }

        @SuppressWarnings("unchecked")
        final BindingDataObjectCodecTreeNode<T> ret = (BindingDataObjectCodecTreeNode<T>) codecNode;
        final Object witness = ITEM_CODEC.compareAndExchangeRelease(this, null, ret);
        return witness == null ? ret : (BindingDataObjectCodecTreeNode<T>) witness;
    }
}
