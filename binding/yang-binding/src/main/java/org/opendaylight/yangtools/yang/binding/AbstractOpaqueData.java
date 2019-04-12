/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;

/**
 * Utility class for {@link OpaqueData} implementations. This class provides baseline implementation of
 * {@link #hashCode()} and {@link #equals(Object)} as specified by {@link OpaqueData}.
 *
 * @param <T> Data object model type
 */
@Beta
public abstract class AbstractOpaqueData<T> implements OpaqueData<T> {
    @Override
    public final int hashCode() {
        return 31 * getObjectModel().hashCode() + dataHashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpaqueData)) {
            return false;
        }
        final OpaqueData<?> other = (OpaqueData<?>) obj;
        return getObjectModel().equals(other.getObjectModel()) && dataEquals(other.getData());
    }

    protected int dataHashCode() {
        return getData().hashCode();
    }

    protected boolean dataEquals(final Object data) {
        return getData().equals(data);
    }
}
