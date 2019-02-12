/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.base;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * An IdentifiableItem represents a object that is usually present in a collection and can be
 * identified uniquely by a key. In YANG terms this would probably represent an item in a list.
 *
 * @param <I> An object that is identifiable by an identifier
 * @param <T> The identifier of the object
 */
@Beta
public final class IdentifiableItem<I extends TreeNode, T> extends TreeArgument<I> {
    private final Class<I> type;
    private final T key;

    public IdentifiableItem(final Class<I> type, final T key) {
        this.type = requireNonNull(type);
        this.key = requireNonNull(key, "Key may not be null.");
    }

    @Override
    public Class<I> getType() {
        return type;
    }

    public T getKey() {
        return this.key;
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj) && key.equals(((IdentifiableItem<?, ?>) obj).getKey());
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + key.hashCode();
    }

    @Override
    public String toString() {
        return type.getName() + "[key=" + key + "]";
    }
}