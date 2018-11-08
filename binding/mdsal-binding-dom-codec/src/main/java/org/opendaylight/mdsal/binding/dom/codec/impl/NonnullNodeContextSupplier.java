/*
 * Copyright (c) 2018 Pantheon Technologies, s.ro.. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

final class NonnullNodeContextSupplier implements NodeContextSupplier {
    private final NodeContextSupplier delegate;

    private NonnullCodecContext<?> instance;

    NonnullNodeContextSupplier(final NodeContextSupplier delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public NonnullCodecContext<?> get() {
        NonnullCodecContext<?> tmp = instance;
        if (tmp == null) {
            synchronized (this) {
                tmp = instance;
                if (tmp == null) {
                    tmp = NonnullCodecContext.create(delegate.get());
                    instance = tmp;
                }
            }
        }

        return tmp;
    }
}
