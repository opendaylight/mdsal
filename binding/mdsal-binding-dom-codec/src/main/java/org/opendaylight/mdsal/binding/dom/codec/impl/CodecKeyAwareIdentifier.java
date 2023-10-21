/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BindingKeyAwareIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingKeyAwareIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link ForwardingBindingDataObjectIdentifier} backed by a {@link YangInstanceIdentifier}.
 */
final class CodecKeyAwareIdentifier<T extends DataObject & KeyAware<K>, K extends Key<T>>
        extends ForwardingBindingKeyAwareIdentifier<T, K> implements CodecInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull  BindingKeyAwareIdentifier<T, K> delegate;
    private final @NonNull YangInstanceIdentifier dom;

    CodecKeyAwareIdentifier(final BindingKeyAwareIdentifier<T, K> delegate, final YangInstanceIdentifier dom) {
        this.delegate = requireNonNull(delegate);
        this.dom = requireNonNull(dom);
    }

    @Override
    public YangInstanceIdentifier dom() {
        return dom;
    }

    @Override
    protected BindingKeyAwareIdentifier<T, K> delegate() {
        return delegate;
    }
}
