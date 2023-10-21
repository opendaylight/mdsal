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
import org.opendaylight.yangtools.yang.binding.BindingPropertyValueIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingPropertyValueIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link ForwardingBindingPropertyValueIdentifier} backed by a {@link YangInstanceIdentifier}.
 */
final class CodecPropertyValueIdentifier<T extends DataObject, V>
        extends ForwardingBindingPropertyValueIdentifier<T, V> implements CodecInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull BindingPropertyValueIdentifier<T, V> delegate;
    private final @NonNull YangInstanceIdentifier dom;

    CodecPropertyValueIdentifier(final BindingPropertyValueIdentifier<T, V> delegate,
                final YangInstanceIdentifier dom) {
        this.delegate = requireNonNull(delegate);
        this.dom = requireNonNull(dom);
    }

    @Override
    public YangInstanceIdentifier dom() {
        return dom;
    }

    @Override
    protected BindingPropertyValueIdentifier<T, V> delegate() {
        return delegate;
    }
}
