/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingInstanceIdentifier;

/**
 * Binding representation of an {@code type instance-identifier} value.
 */
public sealed interface BindingInstanceIdentifier extends HierarchicalIdentifier<BindingInstanceIdentifier>
        permits BindingDataObjectIdentifier, BindingValueIdentifier, ForwardingBindingInstanceIdentifier {
    @Override
    default boolean contains(final BindingInstanceIdentifier other) {
        throw new UnsupportedOperationException("FIXME: implement this method");
    }

    sealed interface Builder permits BindingDataObjectIdentifier.Builder, BindingValueIdentifier.Builder {

        @NonNull BindingInstanceIdentifier build();
    }
}
