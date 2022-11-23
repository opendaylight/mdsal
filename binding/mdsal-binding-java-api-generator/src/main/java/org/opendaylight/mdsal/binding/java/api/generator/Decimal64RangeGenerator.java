/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.Range;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
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

//    private static String range(final Function<Class<?>, String> classImporter) {
//        return classImporter.apply(Range.class);
//    }

//    private static String itemType(final Function<Class<?>, String> classImporter) {
//        return range(classImporter) + '<' + classImporter.apply(Decimal64.class) + '>';
//    }

//    private static String arrayType(final Function<Class<?>, String> classImporter) {
//        return itemType(classImporter) + "[]";
//    }

    private static String format(final Function<Class<?>, String> classImporter, final Decimal64 value) {
        return classImporter.apply(Decimal64.class) + ".of(" + value.scale() + ", " + value.unscaledValue() + "L)";
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

        Decimal64 min = null;
        Decimal64 max = null;

        String sc = null;
        for (Range<? extends Number> r : constraints) {

            min = getValue(r.lowerEndpoint());
            max = getValue(r.upperEndpoint());
            sc = String.valueOf(getValue(r.upperEndpoint()).scale());
        }
        sb.append("private static void ").append(checkerName).append("(final ").append(getTypeName())
            .append(" value) {\n");
        sb.append("    if (value.scale() == ").append(sc).append(") {\n");
        sb.append("        if (value.unscaledValue() >= ").append(min.unscaledValue()).append("L && ")
            .append("value.unscaledValue() <= ").append(max.unscaledValue()).append("L) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("        ").append(classImporter.apply(CodeHelpers.class)).append(".throwInvalidRange(")
            .append("\"[[").append(min).append("..").append(max).append("]]\", value);\n");
        sb.append("    }\n");
        sb.append("    ").append(classImporter.apply(CodeHelpers.class)).append(".throwInvalidScale(").append(sc)
            .append(" , value);\n");
        sb.append("}\n");

        return sb.toString();
    }
}
