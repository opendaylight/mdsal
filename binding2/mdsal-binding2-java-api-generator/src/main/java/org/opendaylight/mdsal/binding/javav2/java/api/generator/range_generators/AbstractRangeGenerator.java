/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRangeGenerator<T extends Number & Comparable<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRangeGenerator.class);
    private static final Map<String, AbstractRangeGenerator<?>> GENERATORS;

    private static void addGenerator(final Builder<String, AbstractRangeGenerator<?>> builder,
            final AbstractRangeGenerator<?> generator) {
        builder.put(generator.getTypeClass().getCanonicalName(), generator);
    }

    static {
        final Builder<String, AbstractRangeGenerator<?>> b = ImmutableMap.builder();
        addGenerator(b, new ByteRangeGenerator());
        addGenerator(b, new ShortRangeGenerator());
        addGenerator(b, new IntegerRangeGenerator());
        addGenerator(b, new LongRangeGenerator());
        addGenerator(b, new Uint8RangeGenerator());
        addGenerator(b, new Uint16RangeGenerator());
        addGenerator(b, new Uint32RangeGenerator());
        addGenerator(b, new Uint64RangeGenerator());
        addGenerator(b, new BigDecimalRangeGenerator());
        addGenerator(b, new BigIntegerRangeGenerator());
        GENERATORS = b.build();
    }

    private final Class<T> type;

    protected AbstractRangeGenerator(final Class<T> typeClass) {
        this.type = requireNonNull(typeClass);
    }

    public static AbstractRangeGenerator<?> forType(@Nonnull final Type type) {
        final ConcreteType javaType = TypeUtils.getBaseYangType(type);
        return GENERATORS.get(javaType.getFullyQualifiedName());
    }

    /**
     * Return the type's class.
     *
     * @return A class object
     */
    @Nonnull protected final Class<T> getTypeClass() {
        return type;
    }

    /**
     * Return the type's fully-qualified name.
     *
     * @return Fully-qualified name
     */
    @Nonnull protected final String getTypeName() {
        return type.getName();
    }

    /**
     * Return the value in the native type from a particular Number instance.
     *
     * @param value Value as a Number
     * @return Value in native format.
     */
    @Nonnull protected final T getValue(final Number value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        LOG.debug("Converting value {} from {} to {}", value, value.getClass(), type);
        final T ret = convert(value);

        // Check if the conversion lost any precision by performing conversion the other way around
        final AbstractRangeGenerator<?> gen = GENERATORS.get(value.getClass().getName());
        final Number check = gen.convert(ret);
        if (!value.equals(check)) {
            LOG.warn("Number class conversion from {} to {} truncated value {} to {}", value.getClass(),
                type, value, ret);
        }

        return ret;
    }

    // FIXME: Once BUG-3399 is fixed, we should never need this
    protected abstract T convert(Number value);

    /**
     * Format a value into a Java-compilable expression which results in the appropriate
     * type.
     *
     * @param value Number value
     * @return Java language string representation
     */
    @Nonnull protected abstract String format(T value);

    /**
     * Generate the checker method source code.
     * @param checkerName Name of the checker method.
     * @param constraint Restrictions which need to be applied.
     * @return Method source code.
     */
    @Nonnull protected abstract String generateRangeCheckerImplementation(@Nonnull String checkerName,
            @Nonnull RangeConstraint<?> constraint);

    private static String rangeCheckerName(final String member) {
        return "check" + member + "Range";
    }

    public String generateRangeChecker(@Nonnull final String member, @Nonnull final RangeConstraint<?> constraint) {
        return generateRangeCheckerImplementation(rangeCheckerName(member), constraint);
    }

    public String generateRangeCheckerCall(@Nonnull final String member, @Nonnull final String valueReference) {
        return rangeCheckerName(member) + '(' + valueReference + ");\n";
    }
}
