/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeWriteCursor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingDOMDataTreeWriteCursorAdapter<T extends DOMDataTreeWriteCursor>
        extends AbstractBindingAdapter<T> implements DataTreeWriteCursor {
    private final Deque<PathArgument> stack = new ArrayDeque<>();

    public BindingDOMDataTreeWriteCursorAdapter(final DataTreeIdentifier<?> path, final T delegate,
            final BindingToNormalizedNodeCodec codec) {
        super(codec, delegate);
        path.getRootIdentifier().getPathArguments().forEach(stack::push);
    }

    private YangInstanceIdentifier.PathArgument convertToNormalized(final PathArgument child) {
        stack.push(child);
        final InstanceIdentifier<?> iid = InstanceIdentifier.create(stack);
        final YangInstanceIdentifier ret = getCodec().toNormalized(iid);
        stack.pop();
        return ret.getLastPathArgument();
    }

    private <P extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> convertToNormalized(
            final PathArgument child, final P data) {
        stack.push(child);
        final InstanceIdentifier<?> iid = InstanceIdentifier.create(stack);
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = getCodec()
                .toNormalizedNode(new SimpleEntry<>(iid, data));
        stack.pop();
        return entry;
    }

    @Override
    public void delete(final PathArgument child) {
        getDelegate().delete(convertToNormalized(child));
    }

    @Override
    public <P extends DataObject> void merge(final PathArgument child, final P data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = convertToNormalized(child, data);
        getDelegate().merge(entry.getKey().getLastPathArgument(), entry.getValue());
    }

    @Override
    public <P extends DataObject> void write(final PathArgument child, final P data) {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = convertToNormalized(child, data);
        getDelegate().write(entry.getKey().getLastPathArgument(), entry.getValue());
    }

    @Override
    public void enter(final PathArgument child) {
        stack.push(child);
    }

    @Override
    public void enter(final PathArgument... path) {
        for (final PathArgument pathArgument : path) {
            enter(pathArgument);
        }
    }

    @Override
    public void enter(final Iterable<PathArgument> path) {
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
        getDelegate().close();
    }

    @VisibleForTesting
    Deque<PathArgument> stack() {
        return stack;
    }
}
