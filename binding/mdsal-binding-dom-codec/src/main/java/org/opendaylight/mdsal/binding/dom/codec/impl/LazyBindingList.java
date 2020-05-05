/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.RandomAccess;
import java.util.function.UnaryOperator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * Lazily-populated List implementation backed
 *
 * @param <E> the type of elements in this list
 */
final class LazyBindingList<E extends DataObject> extends AbstractList<E> implements RandomAccess {
    private static final VarHandle OBJECTS = MethodHandles.arrayElementVarHandle(Object[].class);

    private final ListNodeCodecContext<E> codec;
    private final Object[] objects;

    LazyBindingList(final ListNodeCodecContext<E> codec,
            final Collection<? extends NormalizedNodeContainer<?, ?, ?>> entries) {
        this.codec = requireNonNull(codec);
        objects = entries.toArray();
    }

    @Override
    public int size() {
        return objects.length;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public E get(final int index) {
        final Object obj;
        return (obj = OBJECTS.getAcquire(objects, index)) instanceof NormalizedNodeContainer
                ? load(index, (NormalizedNodeContainer<?, ?, ?>) obj) : (E) obj;
    }

    private @NonNull E load(final int index, final NormalizedNodeContainer<?, ?, ?> node) {
        final E ret = codec.createBindingProxy(node);
        final Object witness;
        return (witness = OBJECTS.compareAndExchangeRelease(objects, index, node, ret)) == node ? ret : (E) witness;
    }

    @Override
    public boolean remove(final Object o) {
        throw uoe();
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        throw uoe();
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        throw uoe();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw uoe();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw uoe();
    }

    @Override
    public void clear() {
        throw uoe();
    }

    @Override
    public void sort(final Comparator<? super E> c) {
        throw uoe();
    }

    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Modification not supported");
    }
}
