/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

abstract sealed class CommonDataObjectCodecPrototype<T extends CompositeRuntimeType>
        extends LazyCodecContextSupplier<CommonDataObjectCodecContext<?, T>>
        permits AugmentationCodecPrototype, DataObjectCodecPrototype {
    private final @NonNull T type;
    private final @NonNull CodecContextFactory factory;
    private final @NonNull Item<?> bindingArg;

    CommonDataObjectCodecPrototype(final Item<?> bindingArg, final T type, final CodecContextFactory factory) {
        this.bindingArg = requireNonNull(bindingArg);
        this.type = requireNonNull(type);
        this.factory = requireNonNull(factory);
    }

    final @NonNull T getType() {
        return type;
    }

    final @NonNull CodecContextFactory getFactory() {
        return factory;
    }

    final @NonNull Class<?> getBindingClass() {
        return bindingArg.getType();
    }

    final @NonNull Item<?> getBindingArg() {
        return bindingArg;
    }

    abstract @NonNull NodeIdentifier getYangArg();

    // This method must allow concurrent loading, i.e. nothing in it may have effects outside of the loaded object
    @Override
    abstract @NonNull CommonDataObjectCodecContext<?, T> createInstance();
}
