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
import org.opendaylight.yangtools.yang.binding.BindingRootValueIdentifier;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingRootValueIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link ForwardingBindingRootValueIdentifier} backed by a {@link YangInstanceIdentifier}.
 */
final class CodecRootValueIdentifier<R extends DataRoot, V> extends ForwardingBindingRootValueIdentifier<R, V>
        implements CodecInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull BindingRootValueIdentifier<R, V> delegate;
    private final @NonNull YangInstanceIdentifier dom;

    CodecRootValueIdentifier(final BindingRootValueIdentifier<R, V> delegate,  final YangInstanceIdentifier dom) {
        this.delegate = requireNonNull(delegate);
        this.dom = requireNonNull(dom);
    }

    @Override
    public YangInstanceIdentifier dom() {
        return dom;
    }

    @Override
    protected BindingRootValueIdentifier<R, V> delegate() {
        return delegate;
    }
}
