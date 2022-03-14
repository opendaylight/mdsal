/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Abstract base class for {@code identity} interface implementations.
 */
public abstract class AbstractIdentity implements BaseIdentity {
    @Override
    public final int hashCode() {
        return implementedInterface().hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj == this || obj instanceof BaseIdentity
            && implementedInterface().equals(((BaseIdentity) obj).implementedInterface());
    }
}
