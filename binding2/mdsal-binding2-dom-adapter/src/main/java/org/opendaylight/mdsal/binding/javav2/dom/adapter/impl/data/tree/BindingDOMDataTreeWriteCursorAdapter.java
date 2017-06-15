/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import com.google.common.annotations.Beta;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeWriteCursor;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Data tree write cursor adapter.
 *
 * @param <T>
 *            - {@link DOMDataTreeWriteCursor} type
 */
@Beta
public class BindingDOMDataTreeWriteCursorAdapter<T extends DOMDataTreeWriteCursor> implements DataTreeWriteCursor {

    private final T delegate;
    private final BindingToNormalizedNodeCodec codec;
    private final Deque<TreeArgument<?>> stack = new ArrayDeque<>();

    public BindingDOMDataTreeWriteCursorAdapter(final DataTreeIdentifier<?> path, final T delegate,
            final BindingToNormalizedNodeCodec codec) {

        this.delegate = delegate;
        this.codec = codec;
        path.getRootIdentifier().getPathArguments().forEach(stack::push);
    }

    private YangInstanceIdentifier.PathArgument convertToNormalized(final TreeArgument<?> child) {
        stack.push(child);
        final InstanceIdentifier<?> iid = InstanceIdentifier.create(stack);
        final YangInstanceIdentifier ret = codec.toNormalized(iid);
        stack.pop();
        return ret.getLastPathArgument();
    }

    private <P extends TreeNode> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>
            convertToNormalized(final TreeArgument<?> child, final P data) {
        stack.push(child);
        final InstanceIdentifier<?> iid = InstanceIdentifier.create(stack);
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
                codec.toNormalizedNode(new SimpleEntry<>(iid, data));
        stack.pop();
        return entry;
    }

    @Override
    public void delete(final TreeArgument<?> child) {
        delegate.delete(convertToNormalized(child));
    }

    @Override
    public <U extends TreeNode> void merge(final TreeArgument<U> child, final U data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = convertToNormalized(child, data);
        delegate.merge(entry.getKey().getLastPathArgument(), entry.getValue());
    }

    @Override
    public <P extends TreeNode> void write(final TreeArgument<P> child, final P data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = convertToNormalized(child, data);
        delegate.write(entry.getKey().getLastPathArgument(), entry.getValue());
    }

    @Override
    public void enter(@Nonnull final TreeArgument<?> child) {
        stack.push(child);
    }

    @Override
    public void enter(@Nonnull final TreeArgument<?>... path) {
        for (final TreeArgument<?> pathArgument : path) {
            enter(pathArgument);
        }
    }

    @Override
    public void enter(@Nonnull final Iterable<TreeArgument<?>> path) {
        path.forEach(this::enter);
    }

    @Override
    public void exit() {
        stack.pop();
    }

    @Override
    public void exit(final int depth) {
        for (int i = 0; i < depth; i++) {
            exit();
        }
    }

    @Override
    public void close() {
        delegate.close();
    }
}
