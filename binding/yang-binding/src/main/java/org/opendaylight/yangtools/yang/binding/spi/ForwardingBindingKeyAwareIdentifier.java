/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.spi;

import org.opendaylight.yangtools.yang.binding.BindingKeyAwareIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;

/**
 * A {@link BindingKeyAwareIdentifier} which forwards to a backing {@link #delegate()}.
 */
public abstract non-sealed class ForwardingBindingKeyAwareIdentifier
        <T extends DataObject & KeyAware<K>, K extends Key<T>> extends ForwardingBindingInstanceIdentifier
        implements BindingKeyAwareIdentifier<T, K> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    public final K key() {
        return delegate().key();
    }

    @Override
    protected abstract BindingKeyAwareIdentifier<T, K> delegate();
}
