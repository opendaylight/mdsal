/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.spi;

import org.opendaylight.yangtools.yang.binding.BindingRootValueIdentifier;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * A {@link BindingRootValueIdentifier} which forwards to a backing {@link #delegate()}.
 */
public abstract non-sealed class ForwardingBindingRootValueIdentifier<R extends DataRoot, V>
        extends ForwardingBindingValueIdentifier<R, V> implements BindingRootValueIdentifier<R, V> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    public final Class<R> rootType() {
        return delegate().rootType();
    }

    @Override
    protected abstract BindingRootValueIdentifier<R, V> delegate();
}
