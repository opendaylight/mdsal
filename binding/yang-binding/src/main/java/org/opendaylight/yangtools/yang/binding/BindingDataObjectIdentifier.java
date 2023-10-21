/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingDataObjectIdentifier;

/**
 * A {@link BindingInstanceIdentifier} identifying a {@link DataObject}.
 */
public sealed interface BindingDataObjectIdentifier<T extends DataObject> extends BindingInstanceIdentifier
        permits BindingKeyAwareIdentifier, ForwardingBindingDataObjectIdentifier {

    // FIXME: sealed permitting the internal implementation
    non-sealed interface Builder<T extends DataObject> extends BindingInstanceIdentifier.Builder {
        @Override
        BindingDataObjectIdentifier<T> build();
    }
}
