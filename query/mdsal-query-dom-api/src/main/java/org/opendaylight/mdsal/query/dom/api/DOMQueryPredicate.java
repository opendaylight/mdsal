/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public abstract class DOMQueryPredicate implements Immutable, Predicate<Object> {
    abstract static class AbstractValueDOMQueryPredicate<T> extends DOMQueryPredicate {
        private final @NonNull T value;

        AbstractValueDOMQueryPredicate(final T value) {
            this.value = requireNonNull(value);
        }

        final @NonNull T value() {
            return value;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("value", value);
        }
    }

    abstract static class AbstractComparableDOMQueryPredicate<T extends Comparable<T>>
            extends AbstractValueDOMQueryPredicate<T> {
        AbstractComparableDOMQueryPredicate(final T value) {
            super(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final boolean test(final Object data) {
            return data != null && test(value().compareTo((T) data));
        }

        abstract boolean test(int valueToData);
    }

    abstract static class AbstractStringDOMQueryPredicate extends AbstractValueDOMQueryPredicate<String> {
        AbstractStringDOMQueryPredicate(final String value) {
            super(value);
        }

        @Override
        public final boolean test(final Object data) {
            return data instanceof String && test((String) data);
        }

        abstract boolean test(@NonNull String str);
    }

    public static final class Exists extends DOMQueryPredicate {
        public static final @NonNull Exists INSTANCE = new Exists();

        private Exists() {
            // Hidden on purpose
        }

        @Override
        public boolean test(final Object data) {
            return data != null;
        }
    }

    public static final class ValueEquals<T> extends AbstractValueDOMQueryPredicate<T> {
        public ValueEquals(final T value) {
            super(value);
        }

        @Override
        public boolean test(final Object data) {
            return value().equals(data);
        }
    }

    public static final class GreaterThan<T extends Comparable<T>> extends AbstractComparableDOMQueryPredicate<T> {
        public GreaterThan(final T value) {
            super(value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData <= 0;
        }
    }

    public static final class GreaterThanOrEqual<T extends Comparable<T>>
            extends AbstractComparableDOMQueryPredicate<T> {
        public GreaterThanOrEqual(final T value) {
            super(value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData < 0;
        }
    }

    public static final class LessThan<T extends Comparable<T>> extends AbstractComparableDOMQueryPredicate<T> {
        public LessThan(final T value) {
            super(value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData >= 0;
        }
    }

    public static final class LessThanOrEqual<T extends Comparable<T>> extends AbstractComparableDOMQueryPredicate<T> {
        public LessThanOrEqual(final T value) {
            super(value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData > 0;
        }
    }

    public static final class StartsWith extends AbstractStringDOMQueryPredicate {
        public StartsWith(final String str) {
            super(str);
        }

        @Override
        boolean test(final String str) {
            return str.startsWith(value());
        }
    }

    public static final class EndsWith extends AbstractStringDOMQueryPredicate {
        public EndsWith(final String str) {
            super(str);
        }

        @Override
        boolean test(final String str) {
            return str.endsWith(value());
        }
    }

    public static final class Contains extends AbstractStringDOMQueryPredicate {
        public Contains(final String str) {
            super(str);
        }

        @Override
        boolean test(final String str) {
            return str.contains(value());
        }
    }

    public static final class MatchesPattern extends DOMQueryPredicate {
        private final Pattern pattern;

        public MatchesPattern(final Pattern pattern) {
            this.pattern = requireNonNull(pattern);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("pattern", pattern);
        }

        @Override
        public boolean test(final Object data) {
            return data instanceof CharSequence && pattern.matcher((CharSequence) data).matches();
        }
    }

    DOMQueryPredicate() {
        // Hidden on purpose
    }

    @Override
    public abstract boolean test(@Nullable Object data);

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
