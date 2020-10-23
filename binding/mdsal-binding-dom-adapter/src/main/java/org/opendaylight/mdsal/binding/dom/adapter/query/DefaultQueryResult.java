/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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
    private static final class DefaultItem<T extends DataObject> implements Item<T> {
        private static final VarHandle OBJECT;
        private static final VarHandle PATH;

        static {
            final Lookup lookup = MethodHandles.lookup();
            try {
                OBJECT = lookup.findVarHandle(DefaultItem.class, "object", DataObject.class);
                PATH = lookup.findVarHandle(DefaultItem.class, "path", InstanceIdentifier.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domItem;
        private final BindingCodecTree codec;

        @SuppressWarnings("unused")
        private volatile @Nullable InstanceIdentifier<T> path;
        @SuppressWarnings("unused")
        private volatile @Nullable T object;

        DefaultItem(final BindingCodecTree codec, final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domItem) {
            this.codec = requireNonNull(codec);
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
            final T ret = createObject();
            final Object witness = OBJECT.compareAndExchangeRelease(this, null, ret);
            return witness == null ? ret : (T) witness;
        }

        private T createObject() {
            final BindingCodecTreeNode codecNode = codec.getSubtreeCodec(domItem.getKey());
            verify(codecNode instanceof BindingDataObjectCodecTreeNode, "Unexpected codec %s", codecNode);
            @SuppressWarnings("unchecked")
            final BindingDataObjectCodecTreeNode<T> dataCodec = (BindingDataObjectCodecTreeNode<T>) codecNode;
            return dataCodec.deserialize(domItem.getValue());
        }

        @SuppressWarnings("unchecked")
        private InstanceIdentifier<T> loadPath() {
            final InstanceIdentifier<T> ret = createPath();
            final Object witness = PATH.compareAndExchangeRelease(this, null, ret);
            return witness == null ? ret : (InstanceIdentifier<T>) witness;
        }

        private InstanceIdentifier<T> createPath() {
            return verifyNotNull(codec.getInstanceIdentifierCodec().toBinding(domItem.getKey()), "path");
        }
    }

    private final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> domResult;
    private final BindingCodecTree codec;

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
        return new DefaultItem<>(codec, domItem);
    }
}
