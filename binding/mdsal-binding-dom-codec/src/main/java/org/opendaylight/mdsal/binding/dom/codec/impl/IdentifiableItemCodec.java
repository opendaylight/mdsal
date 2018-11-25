/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.util.ImmutableOffsetMapTemplate;
import org.opendaylight.yangtools.util.SharedSingletonMapTemplate;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class IdentifiableItemCodec implements Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> {
    private static final class SingleKey extends IdentifiableItemCodec {
        private static final MethodType CTOR_TYPE = MethodType.methodType(Identifier.class, Object.class);

        private final SharedSingletonMapTemplate<QName> predicateTemplate;
        private final ValueContext keyContext;
        private final MethodHandle ctor;
        private final QName key;

        SingleKey(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
                final Class<?> identifiable, final QName key, final ValueContext keyContext) {
            super(schema, keyClass, identifiable);
            this.keyContext = requireNonNull(keyContext);
            this.key = requireNonNull(key);
            predicateTemplate = SharedSingletonMapTemplate.ordered(key);

            try {
                ctor = MethodHandles.publicLookup().unreflectConstructor(getConstructor(keyClass)).asType(CTOR_TYPE);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Missing constructor in class " + keyClass, e);
            }
        }

        @Override
        public NodeIdentifierWithPredicates serialize(final IdentifiableItem<?, ?> input) {
            return new NodeIdentifierWithPredicates(qname(), predicateTemplate.instantiateWithValue(
                keyContext.getAndSerialize(input.getKey())));
        }

        @Override
        Identifier<?> deserializeIdentifier(final Map<QName, Object> keyValues) {
            final Object value = keyContext.deserialize(keyValues.get(key));
            try {
                return (Identifier<?>) ctor.invokeExact(value);
            } catch (Throwable e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }
    }

    private static final class MultiKey extends IdentifiableItemCodec {
        private final ImmutableOffsetMapTemplate<QName> predicateTemplate;
        private final Map<QName, ValueContext> keyValueContexts;
        private final List<QName> keysInBindingOrder;
        private final MethodHandle ctor;

        MultiKey(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
                final Class<?> identifiable, final Map<QName, ValueContext> keyValueContexts) {
            super(schema, keyClass, identifiable);

            final MethodHandle tmpCtor;
            try {
                tmpCtor = MethodHandles.publicLookup().unreflectConstructor(getConstructor(keyClass));
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Missing constructor in class " + keyClass, e);
            }
            final MethodHandle inv = MethodHandles.spreadInvoker(tmpCtor.type(), 0);
            this.ctor = inv.asType(inv.type().changeReturnType(Identifier.class)).bindTo(tmpCtor);

            /*
             * We need to re-index to make sure we instantiate nodes in the order in which they are defined. We will also
             * need to instantiate values in the same order.
             */
            predicateTemplate = ImmutableOffsetMapTemplate.ordered(schema.getKeyDefinition());
            this.keyValueContexts = predicateTemplate.instantiateTransformed(keyValueContexts, (key, value) -> value);

            /*
             * When instantiating binding objects we need to specify constructor arguments
             * in alphabetic order. We play a couple of tricks here to optimize CPU/memory
             * trade-offs.
             *
             * We do not have to perform a sort if the source collection has less than two
             * elements.
             *
             * We always perform an ImmutableList.copyOf(), as that will turn into a no-op
             * if the source is already immutable. It will also produce optimized implementations
             * for empty and singleton collections.
             *
             * BUG-2755: remove this if order is made declaration-order-dependent
             */
            final List<QName> sortedKeys = new ArrayList<>(schema.getKeyDefinition());
            sortedKeys.sort(Comparator.comparing(qname -> BindingMapping.getPropertyName(qname.getLocalName())));
            this.keysInBindingOrder = ImmutableList.copyOf(sortedKeys);
        }

        @Override
        public NodeIdentifierWithPredicates serialize(final IdentifiableItem<?, ?> input) {
            final Object value = input.getKey();
            final Object[] values = new Object[keyValueContexts.size()];
            int offset = 0;
            for (final ValueContext valueCtx : keyValueContexts.values()) {
                values[offset++] = valueCtx.getAndSerialize(value);
            }

            return new NodeIdentifierWithPredicates(qname(), predicateTemplate.instantiateWithValues(values));
        }

        @Override
        @SuppressWarnings("checkstyle:illegalCatch")
        Identifier<?> deserializeIdentifier(final Map<QName, Object> keyValues) {
            final Object[] bindingValues = new Object[keysInBindingOrder.size()];
            int offset = 0;
            for (final QName key : keysInBindingOrder) {
                bindingValues[offset++] = keyValueContexts.get(key).deserialize(keyValues.get(key));
            }

            try {
                return (Identifier<?>) ctor.invokeExact(bindingValues);
            } catch (Throwable e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }
    }

    private final Class<?> identifiable;
    private final QName qname;

    IdentifiableItemCodec(final ListSchemaNode schema, final Class<? extends Identifier<?>> keyClass,
            final Class<?> identifiable) {
        this.identifiable = requireNonNull(identifiable);
        this.qname = schema.getQName();
    }

    public static Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> of(final ListSchemaNode schema,
            final Class<? extends Identifier<?>> keyClass, final Class<?> identifiable,
                    final Map<QName, ValueContext> keyValueContexts) {
        switch (keyValueContexts.size()) {
            case 0:
                throw new IllegalArgumentException("Key " + keyClass + " of " + identifiable + " has no components");
            case 1:
                final Entry<QName, ValueContext> entry = keyValueContexts.entrySet().iterator().next();
                return new SingleKey(schema, keyClass, identifiable, entry.getKey(), entry.getValue());
            default:
                return new MultiKey(schema, keyClass, identifiable, keyValueContexts);
        }
    }

    @Override
    public final IdentifiableItem<?, ?> deserialize(final NodeIdentifierWithPredicates input) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final IdentifiableItem identifiableItem = IdentifiableItem.of((Class) identifiable, (Identifier)
            deserializeIdentifier(input.getKeyValues()));
        return identifiableItem;
    }

    final QName qname() {
        return qname;
    }

    abstract Identifier<?> deserializeIdentifier(Map<QName, Object> keyValues);

    @SuppressWarnings("unchecked")
    static Constructor<? extends Identifier<?>> getConstructor(final Class<? extends Identifier<?>> clazz) {
        for (@SuppressWarnings("rawtypes") final Constructor constr : clazz.getConstructors()) {
            final Class<?>[] parameters = constr.getParameterTypes();
            if (!clazz.equals(parameters[0])) {
                // It is not copy constructor;
                return constr;
            }
        }
        throw new IllegalArgumentException("Supplied class " + clazz + "does not have required constructor.");
    }
}
