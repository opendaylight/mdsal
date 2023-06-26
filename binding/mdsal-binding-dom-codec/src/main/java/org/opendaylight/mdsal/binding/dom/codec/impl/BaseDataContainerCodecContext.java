/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Real base for {@link DataContainerCodecContext}. This defines baseline methods in a reasonable way, which we cannot
 * do so directly.
 */
// FIXME: Fold this into DataContainerCodecContext once RootCodecContext is gone
abstract sealed class BaseDataContainerCodecContext<D extends BindingObject & DataContainer,
        T extends CompositeRuntimeType>
    extends DataContainerCodecContext<D, T>
    permits CommonDataObjectCodecContext, YangDataCodecContext {

    BaseDataContainerCodecContext(final T type) {
        super(type);
    }

    @Override
    public final <C extends DataObject> CommonDataObjectCodecContext<C, ?> getStreamChild(final Class<C> childClass) {
        return childNonNull(streamChild(childClass), childClass,
            "Child %s is not valid child of %s", childClass, getBindingClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <C extends DataObject> CommonDataObjectCodecContext<C, ?> streamChild(final Class<C> childClass) {
        final var childProto = streamChildPrototype(childClass);
        return childProto == null ? null : (CommonDataObjectCodecContext<C, ?>) childProto.get();
    }

    abstract @Nullable CommonDataObjectCodecPrototype<?> streamChildPrototype(@NonNull Class<?> childClass);

    @Override
    public final CodecContext yangPathArgumentChild(final PathArgument arg) {
        CodecContextSupplier supplier;
        if (arg instanceof NodeIdentifier nodeId) {
            supplier = yangChildSupplier(nodeId);
        } else if (arg instanceof NodeIdentifierWithPredicates nip) {
            supplier = yangChildSupplier(new NodeIdentifier(nip.getNodeType()));
        } else {
            supplier = null;
        }
        return childNonNull(supplier, arg, "Argument %s is not valid child of %s", arg, getSchema()).get();
    }

    abstract @Nullable CodecContextSupplier yangChildSupplier(@NonNull NodeIdentifier arg);
}
