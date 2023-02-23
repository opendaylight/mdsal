/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Abstract base class for classes generated for YANG {@code extension} statements.
 */
@NonNullByDefault
public abstract non-sealed class YangExtension<X extends YangExtension<X, R>, R extends DataRoot>
        implements BindingContract<X> {
    @Override
    public abstract Class<X> implementedInterface();

    /**
     * Return the {@link QName} associated with this extension.
     *
     * @return A QName
     */
    public abstract QName qname();

    /**
     * Return the {@code module} which defines this extension.
     *
     * @return A module's {@link DataRoot} class
     */
    public abstract Class<R> definingModule();

    @Override
    public final int hashCode() {
        return implementedInterface().hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof YangExtension<?, ?> other
            && implementedInterface().equals(other.implementedInterface());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qname", qname()).toString();
    }
}
