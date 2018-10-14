/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.lang.reflect.Method;
import java.util.List;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class KeyedListNodeCodecContext<D extends DataObject & Identifiable<?>> extends ListNodeCodecContext<D> {
    static final class NonMandatory<D extends DataObject & Identifiable<?>> extends KeyedListNodeCodecContext<D>
            implements NonMandatoryList {
        NonMandatory(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
            super(prototype);
        }
    }

    private final Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> codec;
    private final Method keyGetter;

    KeyedListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        super(prototype);

        this.codec = factory().getPathArgumentCodec(getBindingClass(), getSchema());
        try {
            this.keyGetter = getBindingClass().getMethod(BindingMapping.IDENTIFIABLE_KEY_NAME);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Required method not available", e);
        }
    }

    @Override
    protected final void addYangPathArgument(final PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        /*
         * DOM Instance Identifier for list is always represent by two entries one for map and one for children. This
         * is also true for wildcarded instance identifiers
         */
        if (builder == null) {
            return;
        }

        super.addYangPathArgument(arg, builder);
        if (arg instanceof IdentifiableItem) {
            builder.add(codec.serialize((IdentifiableItem<?, ?>) arg));
        } else {
            // Adding wildcarded
            super.addYangPathArgument(arg, builder);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    final Object getBindingChildValue(final Method method, final NormalizedNodeContainer dom) {
        if (dom instanceof MapEntryNode && keyGetter.equals(method)) {
            NodeIdentifierWithPredicates identifier = ((MapEntryNode) dom).getIdentifier();
            return codec.deserialize(identifier).getKey();
        }
        return super.getBindingChildValue(method, dom);
    }

    @Override
    final protected PathArgument getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        if (domArg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) domArg);
        }
        return super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    final NodeIdentifierWithPredicates serialize(final Identifier<?> key) {
        return codec.serialize(IdentifiableItem.of((Class)getBindingClass(), (Identifier)key));
    }

    @Override
    public final YangInstanceIdentifier.PathArgument serializePathArgument(final PathArgument arg) {
        if (arg instanceof IdentifiableItem) {
            return codec.serialize((IdentifiableItem<?, ?>) arg);
        }
        return super.serializePathArgument(arg);
    }

    @Override
    public final PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        if (arg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) arg);
        }
        return super.deserializePathArgument(arg);
    }
}
