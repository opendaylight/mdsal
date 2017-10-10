/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import java.lang.reflect.Method;
import java.util.List;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecPrototype;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Codec context for serializing and deserializing keyed list node and it's
 * path.
 *
 * @param <D>
 *            - type of tree node
 */
@Beta
public final class KeyedListNodeCodecContext<D extends TreeNode & Identifiable<?>> extends ListNodeCodecContext<D> {

    private final Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> codec;
    private final Method keyGetter;

    /**
     * Prepare context for keyed list node from prototype.
     *
     * @param prototype
     *            - codec prototype of keyed list node
     */
    public KeyedListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        super(prototype);
        this.codec = factory().getPathArgumentCodec(getBindingClass(), getSchema());
        try {
            this.keyGetter = getBindingClass().getMethod("getIdentifier");
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Required method not available", e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addYangPathArgument(final TreeArgument arg, final List<PathArgument> builder) {
        /*
         * DOM Instance Identifier for list is always represent by two entries
         * one for map and one for children. This is also true for wildcarded
         * instance identifiers
         */
        if (builder == null) {
            return;
        }

        super.addYangPathArgument(arg, builder);
        if (arg instanceof IdentifiableItem<?, ?>) {
            builder.add(codec.serialize((IdentifiableItem<?, ?>) arg));
        } else {
            // Adding wildcarded
            super.addYangPathArgument(arg, builder);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getBindingChildValue(final Method method, final NormalizedNodeContainer dom) {
        if (dom instanceof MapEntryNode && keyGetter.equals(method)) {
            final NodeIdentifierWithPredicates identifier = ((MapEntryNode) dom).getIdentifier();
            return codec.deserialize(identifier).getKey();
        } else {
            return super.getBindingChildValue(method, dom);
        }
    }

    @Override
    protected TreeArgument<?> getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        if (domArg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) domArg);
        }
        return super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings("rawtypes")
    public NodeIdentifierWithPredicates serialize(final IdentifiableItem keyValues) {
        return codec.serialize(keyValues);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final TreeArgument arg) {
        if (arg instanceof IdentifiableItem) {
            return codec.serialize((IdentifiableItem<?, ?>) arg);
        }
        return super.serializePathArgument(arg);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public TreeArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        if (arg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) arg);
        }
        return super.deserializePathArgument(arg);
    }
}

