/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.BindingPropertyValueIdentifier;
import org.opendaylight.yangtools.yang.binding.BindingValueIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Default implementation of {@link BindingPropertyValueIdentifier}.
 */
public record BuiltBindingPropertyValueIdentifier<T extends DataObject, V>(
        @NonNull BindingDataObjectIdentifier<T> dataObject,
        BindingValueIdentifier.@NonNull Getter<T, V> getter) implements BindingPropertyValueIdentifier<T, V> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public BuiltBindingPropertyValueIdentifier {
        requireNonNull(dataObject);
        requireNonNull(getter);
    }
}
