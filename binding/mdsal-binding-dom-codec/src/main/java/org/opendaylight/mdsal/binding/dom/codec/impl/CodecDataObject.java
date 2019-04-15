/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A base class for {@link DataObject}s backed by {@link DataObjectCodecContext}. While this class is public, it not
 * part of API surface and is an implementation detail. The only reason for it being public is that it needs to be
 * accessible by code generated at runtime.
 *
 * @param <T> DataObject type
 */
public abstract class CodecDataObject<T extends DataObject> implements DataObject {
    private static final @NonNull Object NULL_VALUE = new Object();

    @SuppressWarnings("rawtypes")
    final NormalizedNodeContainer data;
    final DataObjectCodecContext<T, ?> context;

    private volatile Integer cachedHashcode = null;

    public CodecDataObject(final DataObjectCodecContext<T, ?> ctx, final NormalizedNodeContainer<?, ?, ?> data) {
        this.context = requireNonNull(ctx, "Context must not be null");
        this.data = requireNonNull(data, "Data must not be null");
    }

    @Override
    public final int hashCode() {
        final Integer cached = cachedHashcode;
        if (cached != null) {
            return cached;
        }

        final int result = codecAugmentedHashCode();
        cachedHashcode = result;
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        final Class<? extends DataObject> iface = implementedInterface();
        if (!iface.isInstance(obj)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final T other = (T) iface.cast(obj);
        if (other instanceof CodecDataObject) {
            return data.equals(((CodecDataObject<?>) obj).data);
        }
        return codecAugmentedEquals(other);
    }

    @Override
    public final String toString() {
        return codecAugmentedFillToString(MoreObjects.toStringHelper(implementedInterface()).omitNullValues())
                .toString();
    }

    protected final Object codecMember(final String methodName) {
        return context.getBindingChildValue(methodName, data);
    }

    protected static final @NonNull Object codecMaskNull(final @Nullable Object unmasked) {
        return unmasked == null ? NULL_VALUE : unmasked;
    }

    @SuppressWarnings("unchecked")
    protected static final <T> @Nullable T codecUnmaskNull(final Object masked) {
        return masked == NULL_VALUE ? null : (T) masked;
    }

    protected abstract int codecHashCode();

    protected abstract boolean codecEquals(T other);

    protected abstract ToStringHelper codecFillToString(ToStringHelper helper);

    // Non-final to allow specialization in AugmentableCodecDataObject
    int codecAugmentedHashCode() {
        return codecHashCode();
    }

    // Non-final to allow specialization in AugmentableCodecDataObject
    boolean codecAugmentedEquals(final T other) {
        return codecEquals(other);
    }

    // Non-final to allow specialization in AugmentableCodecDataObject
    ToStringHelper codecAugmentedFillToString(final ToStringHelper helper) {
        return codecFillToString(helper);
    }
}
