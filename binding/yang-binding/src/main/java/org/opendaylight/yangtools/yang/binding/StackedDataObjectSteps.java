/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

final class StackedDataObjectSteps extends AbstractList<DataObjectStep<?>> implements DataObjectSteps {
    private final DataObjectSteps base;
    private final DataObjectStep<?> last;

    StackedDataObjectSteps(final DataObjectSteps base, final DataObjectStep<?> last) {
        this.base = requireNonNull(base);
        this.last = requireNonNull(last);
    }

    @Override
    public DataObjectStep<?> getLast() {
        return last;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return base.size() + 1;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        return last.equals(o) || base.contains(o);
    }

    @Override
    public DataObjectStep<?> get(final int index) {
        final var baseSize = base.size();
        Objects.checkIndex(index, baseSize + 1);
        return index == baseSize ? last : base.get(index);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int indexOf(final Object o) {
        final var ret = base.indexOf(o);
        if (ret == -1) {
            if (last.equals(o)) {
                return base.size();
            }
        }
        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int lastIndexOf(final Object o) {
        return last.equals(o) ? base.size() : base.lastIndexOf(o);
    }

    @Override
    public UnmodifiableIterator<DataObjectStep<?>> iterator() {
        return new IteratorImpl(base, last);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final Collection<? extends DataObjectStep<?>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final int index, final Collection<? extends DataObjectStep<?>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private static final class IteratorImpl extends UnmodifiableIterator<DataObjectStep<?>> {
        private final Iterator<DataObjectStep<?>> stack;
        private final Iterator<DataObjectStep<?>> base;

        IteratorImpl(final Iterable<DataObjectStep<?>> base, final DataObjectStep<?> last) {
            this.base = base.iterator();
            stack = Iterators.singletonIterator(last);
        }

        @Override
        public boolean hasNext() {
            return stack.hasNext();
        }

        @Override
        public DataObjectStep<?> next() {
            return base.hasNext() ? base.next() : stack.next();
        }
    }
}