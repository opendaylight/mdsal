/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link Collection} backed by 2 other collections (efficiently implemented).
 *
 * @author Michael Vorburger.ch
 */
class UnionCollection<T> implements Collection<T> {
    // intentionally package local here, for now; may be later move somewhere else

    // Surpring that Guava does not offer this (or I could no find it)

    private final Collection<T> first;
    private final Collection<T> second;

    UnionCollection(Collection<T> first, Collection<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int size() {
        return first.size() + second.size();
    }

    @Override
    public boolean isEmpty() {
        return first.isEmpty() && second.isEmpty();
    }

    @Override
    public boolean contains(Object element) {
        return first.contains(element) || second.contains(element);
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.concat(first.iterator(), second.iterator());
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("TODO implement me correctly if actually really needed...");
    }

    @Override
    public <T> T[] toArray(T[] array) {
        throw new UnsupportedOperationException("TODO implement me correctly if actually really needed...");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new UnsupportedOperationException("TODO implement me correctly if actually really needed...");
    }

    @Override
    public boolean remove(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
