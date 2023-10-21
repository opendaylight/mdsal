/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingValueIdentifier;

/**
 * A {@link BindingInstanceIdentifier} identifying a value.
 */
public sealed interface BindingValueIdentifier<T, V> extends BindingInstanceIdentifier
        permits BindingRootValueIdentifier, BindingPropertyValueIdentifier, ForwardingBindingValueIdentifier {
    // FIXME: sealed and specialized to possible values: plus naked yang.common values and BindingObject specializations
    //        BaseIdentity, DataObject, OpaqueObject, TypeObject, YangData plus BindingInstanceIdentifier and lists
    //        of all these, (plus Maps of for KeyAware?)
    // FIXME: Serializable here is important for LambdaDecoder -- document that
    @FunctionalInterface
    interface Getter<T, V> extends Serializable {

        V getFrom(T obj);
    }

    @NonNull Getter<T, V> getter();

    abstract sealed class Builder<T, V> implements BindingInstanceIdentifier.Builder
            permits BindingPropertyValueIdentifier.Builder, BindingRootValueIdentifier.Builder {
        final @NonNull Getter<T, V> getter;

        Builder(final Getter<T, V> getter) {
            this.getter = requireNonNull(getter);
        }

        @Override
        public abstract BindingValueIdentifier<T, V> build();
    }
}
