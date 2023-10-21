/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.impl.BuiltBindingPropertyValueIdentifier;
import org.opendaylight.yangtools.yang.binding.spi.ForwardingBindingPropertyValueIdentifier;

/**
 * A {@link BindingInstanceIdentifier} identifying a value of a {@link DataObject} property.
 */
public sealed interface BindingPropertyValueIdentifier<T extends DataObject, V> extends BindingValueIdentifier<T, V>
        permits BuiltBindingPropertyValueIdentifier, ForwardingBindingPropertyValueIdentifier {

    @NonNull  BindingDataObjectIdentifier<T> dataObject();

    final class Builder<T extends DataObject, V> extends BindingValueIdentifier.Builder<T, V> {
        private final @NonNull BindingDataObjectIdentifier<T> dataObject;

        Builder(final BindingDataObjectIdentifier<T> dataObject, final Getter<T, V> getter) {
            super(getter);
            this.dataObject = requireNonNull(dataObject);
        }

        @Override
        public BindingPropertyValueIdentifier<T, V> build() {
            return new BuiltBindingPropertyValueIdentifier<>(dataObject, getter);
        }
    }
}
