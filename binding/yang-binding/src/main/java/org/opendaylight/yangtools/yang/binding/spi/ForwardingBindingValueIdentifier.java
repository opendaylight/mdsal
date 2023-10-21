/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.spi;

import org.opendaylight.yangtools.yang.binding.BindingValueIdentifier;

/**
 * A {@link BindingValueIdentifier} which forwards to a backing {@link #delegate()}.
 */
public abstract sealed class ForwardingBindingValueIdentifier<T, V> extends ForwardingBindingInstanceIdentifier
        implements BindingValueIdentifier<T, V>
        permits ForwardingBindingPropertyValueIdentifier, ForwardingBindingRootValueIdentifier {
    @Override
    public final Getter<T, V> getter() {
        return delegate().getter();
    }

    @Override
    protected abstract BindingValueIdentifier<T, V> delegate();
}
