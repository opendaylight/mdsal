/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.binding.Enumeration;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EnumerationCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final Logger LOG = LoggerFactory.getLogger(EnumerationCodec.class);

    private final ImmutableBiMap<String, Enum<?>> yangValueToBinding;

    EnumerationCodec(final Class<? extends Enum<?>> enumeration, final ImmutableBiMap<String, Enum<?>> schema) {
        super(enumeration);
        yangValueToBinding = requireNonNull(schema);
    }

    static Callable<EnumerationCodec> loader(final Class<?> returnType, final EnumTypeDefinition enumSchema) {
        checkArgument(Enum.class.isAssignableFrom(returnType));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<? extends Enum<?>> enumType = (Class) returnType;
        return () -> {
            final Builder<String, Enum<?>> builder = ImmutableBiMap.builder();
            for (Enum<?> enumValue : enumType.getEnumConstants()) {
                checkArgument(enumValue instanceof Enumeration,
                    "Enumeration constant %s is not implementing Enumeration", enumValue);
                builder.put(((Enumeration) enumValue).getName(), enumValue);
            }
            final ImmutableBiMap<String, Enum<?>> mapping = builder.build();

            // Check if mapping is a bijection
            final Set<String> assignedNames =  enumSchema.getValues().stream().map(EnumPair::getName)
                    .collect(Collectors.toSet());
            for (String name : assignedNames) {
                if (!mapping.containsKey(name)) {
                    LOG.warn("Enumeration {} does not contain assigned name '{}' from {}", enumType, name, enumSchema);
                }
            }
            for (String name : mapping.keySet()) {
                if (!assignedNames.contains(name)) {
                    LOG.warn("Enumeration {} contains assigned name '{}' not covered by {}", enumType, name,
                        enumSchema);
                }
            }

            return new EnumerationCodec(enumType, mapping);
        };
    }

    @Override
    public Object deserialize(final Object input) {
        Enum<?> value = yangValueToBinding.get(input);
        checkArgument(value != null, "Invalid enumeration value %s. Valid values are %s", input,
                yangValueToBinding.keySet());
        return value;
    }

    @Override
    public Object serialize(final Object input) {
        checkArgument(getTypeClass().isInstance(input), "Input must be instance of %s", getTypeClass());
        return yangValueToBinding.inverse().get(input);
    }
}