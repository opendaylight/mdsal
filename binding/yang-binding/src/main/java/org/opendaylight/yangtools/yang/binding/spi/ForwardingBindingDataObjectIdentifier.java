/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.spi;

import org.opendaylight.yangtools.yang.binding.BindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A {@link BindingDataObjectIdentifier} which forwards to a backing {@link #delegate()}.
 */
public abstract non-sealed class ForwardingBindingDataObjectIdentifier<T extends DataObject>
        extends ForwardingBindingInstanceIdentifier implements BindingDataObjectIdentifier<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected abstract BindingDataObjectIdentifier<T> delegate();
}
