/*
 * Copyright (c) 2018 Pantheon Technologies, s.ro.. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

final class NonnullCodecContext<T extends DataObject> extends NodeCodecContext<T> {
    private final ListNodeCodecContext<T> delegate;

    private NonnullCodecContext(final ListNodeCodecContext<T> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public static <T extends DataObject> NonnullCodecContext<T> create(final NodeCodecContext<T> delegate) {
        verify(delegate instanceof ListNodeCodecContext);
        return new NonnullCodecContext<>((ListNodeCodecContext<T>) delegate);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return delegate.deserializeObject(normalizedNode);
    }

    @Override
    Object defaultObject() {
        return ImmutableList.of();
    }

    @Override
    public Class<T> getBindingClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E extends DataObject> BindingCodecTreeNode<E> streamChild(final Class<E> childClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E extends DataObject> Optional<? extends BindingCodecTreeNode<E>> possibleStreamChild(
            final Class<E> childClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BindingCodecTreeNode<?> yangPathArgumentChild(final PathArgument child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BindingCodecTreeNode<?> bindingPathArgumentChild(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument arg,
            final List<PathArgument> builder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BindingNormalizedNodeCachingCodec<T> createCachingCodec(
            final ImmutableCollection<Class<? extends DataObject>> cacheSpecifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeAsNormalizedNode(final T data, final NormalizedNodeStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PathArgument serializePathArgument(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument deserializePathArgument(
            final PathArgument arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WithStatus getSchema() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildAddressabilitySummary getChildAddressabilitySummary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T deserialize(final NormalizedNode<?, ?> data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NormalizedNode<?, ?> serialize(final T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected PathArgument getDomPathArgument() {
        throw new UnsupportedOperationException();
    }
}
