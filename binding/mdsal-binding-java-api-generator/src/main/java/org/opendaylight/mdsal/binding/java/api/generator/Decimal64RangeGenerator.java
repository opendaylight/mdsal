/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.Range;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Decimal64 boundary check generator. It requires instantiation of boundary values -- these are implemented by
 * generating an array constant within the class, which contains {@link Range} instances, which hold pre-instantiated
 * boundary values.
 */
final class Decimal64RangeGenerator extends AbstractRangeGenerator<Decimal64> {

    Decimal64RangeGenerator() {
        super(Decimal64.class);
    }

    @Override
    @Deprecated
    protected Decimal64 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
            || value instanceof Uint8 || value instanceof Uint16) {
            // FIXME: this is not quite right
            return Decimal64.valueOf(1, value.intValue());
        } else {
            // FIXME: this is not quite right
            return Decimal64.valueOf(1, value.longValue());
        }
    }

    @Override
    protected String generateRangeCheckerImplementation(final String checkerName,
        final RangeConstraint<?> constraint, final Function<Class<?>, String> classImporter) {
        final Set<? extends Range<? extends Number>> constraints = constraint.getAllowedRanges().asRanges();
        final StringBuilder sb = new StringBuilder();

        Range<? extends Number> range = constraints.stream().findAny().orElse(null);
        Decimal64 min = getValue(range.lowerEndpoint());
        Decimal64 max = getValue(range.upperEndpoint());

        String sc = String.valueOf(getValue(range.upperEndpoint()).scale());

        sb.append("private static void ").append(checkerName).append("(final ").append(getTypeName())
            .append(" value) {\n");
        sb.append("    if (value.scale() <= ").append(sc).append(") {\n");
        sb.append("        if (value.unscaledValue() >= ").append(min.unscaledValue()).append("L && ")
            .append("value.unscaledValue() <= ").append(max.unscaledValue()).append("L) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("        ").append(classImporter.apply(CodeHelpers.class)).append(".throwInvalidRange(")
            .append("\"[[").append(min).append("..").append(max).append("]]\", value);\n");
        sb.append("    }\n");
        sb.append("    ").append(classImporter.apply(CodeHelpers.class)).append(".throwInvalidScale(").append(sc)
            .append(" , value.scale());\n");
        sb.append("}\n");

        return sb.toString();
    }
}
