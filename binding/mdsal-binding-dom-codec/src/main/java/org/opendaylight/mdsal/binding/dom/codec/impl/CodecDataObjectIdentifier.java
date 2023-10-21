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
import org.opendaylight.yangtools.yang.binding.BindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link ForwardingBindingDataObjectIdentifier} backed by a {@link YangInstanceIdentifier}.
 */
final class CodecDataObjectIdentifier<T extends DataObject> extends ForwardingBindingDataObjectIdentifier<T>
        implements CodecInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull BindingDataObjectIdentifier<T> delegate;
    private final @NonNull YangInstanceIdentifier dom;

    CodecDataObjectIdentifier(final BindingDataObjectIdentifier<T> delegate, final YangInstanceIdentifier dom) {
        this.delegate = requireNonNull(delegate);
        this.dom = requireNonNull(dom);
    }

    @Override
    public YangInstanceIdentifier dom() {
        return dom;
    }

    @Override
    protected BindingDataObjectIdentifier<T> delegate() {
        return delegate;
    }
}
