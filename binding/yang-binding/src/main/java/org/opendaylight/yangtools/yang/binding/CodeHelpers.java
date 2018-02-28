/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper methods for generated binding code. This class concentrates useful primitives generated code may call
 * to perform specific shared functions. This allows for generated classes to be leaner. Methods in this class follows
 * general API stability requirements of the Binding Specification.
 *
 * @author Robert Varga
 */
public final class CodeHelpers {
    private CodeHelpers() {
        // Hidden
    }

    /**
     * Require that an a value-related expression is true.
     */
    public static void validValue(final boolean expression, final Object value, final String options) {
        checkArgument(expression, "expected one of: %s \n%but was: %s", options, value);
    }

    /**
     * Require an argument being received. This is similar to {@link java.util.Objects#requireNonNull(Object)}, but
     * throws an IllegalArgumentException.
     *
     * <p>
     * Implementation note: we expect argName to be a string literal or a constant, so that it's non-nullness can be
     *                      quickly discovered for a call site (where we are going to be inlined).
     *
     * @param value Value itself
     * @param name Symbolic name
     * @return non-null value
     * @throws IllegalArgumentException if value is null
     * @throws NullPointerException if name is null
     */
    // FIXME: another advantage is that it is JDT-annotated, but we could live without that. At some point we should
    //        schedule a big ISE-to-NPE conversion and just use Objects.requireNonNull() instead.
    public static <T> @NonNull T nonNullValue(@Nullable final T value, final @NonNull String name) {
        requireNonNull(name);
        checkArgument(value != null, "%s must not be null", name);
        return value;
    }

    public static Pattern[] compilePatterns(final List<String> patterns) {
        final int size = patterns.size();
        verify(size != 0, "Patterns may not be empty");
        final Pattern[] result = new Pattern[size];
        for (int i = 0; i < size; ++i) {
            result[i] = Pattern.compile(patterns.get(i));
        }
        return result;
    }

    public static void enforcePatternConstraints(final String value, final Pattern pattern, final String regex) {
        if (!pattern.matcher(value).matches()) {
            final String match = BindingMapping.isNegatedPattern(pattern) ? "matches" : "does not match";
            throw new IllegalArgumentException("Supplied value \"" + value + "\" " + match + " expression \""
                    + regex + "\"");
        }
    }

    public static void enforcePatternConstraints(final String value, final Pattern[] patterns, final String[] regexes) {
        verify(patterns.length == regexes.length, "Patterns and regular expression lengths have to match");
        for (int i = 0; i < patterns.length; ++i) {
            enforcePatternConstraints(value, patterns[i], regexes[i]);
        }
    }
}
