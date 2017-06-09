/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@Beta
final class EnumerationCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {

    private final ImmutableBiMap<String, Enum<?>> yangValueToBinding;

    private EnumerationCodec(final Class<? extends Enum<?>> enumeration, final Map<String, Enum<?>> schema) {
        super(enumeration);
        yangValueToBinding = ImmutableBiMap.copyOf(schema);
    }

    static Callable<EnumerationCodec> loader(final Class<?> returnType, final EnumTypeDefinition enumSchema) {
        Preconditions.checkArgument(Enum.class.isAssignableFrom(returnType));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<? extends Enum<?>> enumType = (Class) returnType;
        return () -> {
            final Map<String, Enum<?>> nameToValue = new HashMap<>();
            for (final Enum<?> enumValue : enumType.getEnumConstants()) {
                nameToValue.put(enumValue.toString(), enumValue);
            }
            final Map<String, Enum<?>> yangNameToBinding = new HashMap<>();
            for (final EnumPair yangValue : enumSchema.getValues()) {
                final String bindingName = JavaIdentifierNormalizer.normalizeSpecificIdentifier(yangValue.getName(),
                        JavaIdentifier.ENUM_VALUE);
                final Enum<?> bindingVal = nameToValue.get(bindingName);
                yangNameToBinding.put(yangValue.getName(), bindingVal);
            }
            return new EnumerationCodec(enumType, yangNameToBinding);
        };
    }

    @Override
    public Object deserialize(final Object input) {
        final Enum<?> value = yangValueToBinding.get(input);
        Preconditions.checkArgument(value != null, "Invalid enumeration value %s. Valid values are %s", input,
                yangValueToBinding.keySet());
        return value;
    }

    @Override
    public Object serialize(final Object input) {
        Preconditions.checkArgument(getTypeClass().isInstance(input), "Input must be instance of %s", getTypeClass());
        return yangValueToBinding.inverse().get(input);
    }
}
