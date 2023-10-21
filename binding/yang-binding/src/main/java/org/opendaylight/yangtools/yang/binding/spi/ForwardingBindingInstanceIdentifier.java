/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.spi;

import com.google.common.collect.ForwardingObject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BindingInstanceIdentifier;

/**
 * A {@link BindingInstanceIdentifier} which forwards to a backing {@link #delegate()}.
 */
public abstract sealed class ForwardingBindingInstanceIdentifier extends ForwardingObject
        implements BindingInstanceIdentifier
        permits ForwardingBindingDataObjectIdentifier, ForwardingBindingKeyAwareIdentifier,
                ForwardingBindingValueIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected abstract @NonNull BindingInstanceIdentifier delegate();
}
