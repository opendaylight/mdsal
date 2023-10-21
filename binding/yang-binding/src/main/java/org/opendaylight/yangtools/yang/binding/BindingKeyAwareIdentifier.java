/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingKeyAwareIdentifier;

/**
 * A {@link BindingInstanceIdentifier} identifying a {@link KeyAware} {@link DataObject}.
 */
public sealed interface BindingKeyAwareIdentifier<T extends DataObject & KeyAware<K>, K extends Key<T>>
        extends BindingDataObjectIdentifier<T> permits ForwardingBindingKeyAwareIdentifier {

    @NonNull K key();

    // FIXME: sealed permitting the internal implementation
    interface Builder<T extends DataObject & KeyAware<K>, K extends Key<T>>
            extends BindingDataObjectIdentifier.Builder<T> {
        @Override
        BindingKeyAwareIdentifier<T, K> build();
    }
}
