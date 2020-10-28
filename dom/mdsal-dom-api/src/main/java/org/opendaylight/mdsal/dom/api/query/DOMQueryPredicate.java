/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
public abstract class DOMQueryPredicate implements Immutable, Predicate<NormalizedNode<?, ?>> {
    private abstract static class Match<T extends NormalizedNode<?, ?>, M extends Match<T, M>> {
        abstract boolean test(final NormalizedNode<?, ?> data);

        abstract M negate();

        abstract M and(M other);

        abstract M or(M other);
    }

    private abstract static class LeafMatch extends Match<LeafNode<?>, LeafMatch> {
        @Override
        public final boolean test(final NormalizedNode<?, ?> data) {
            return testValue(data instanceof LeafNode ? ((LeafNode<?>) data).getValue() : null);
        }

        @Override
        LeafMatch negate() {
            return new LeafMatchNot(this);
        }

        @Override
        LeafMatchAll and(final LeafMatch other) {
            return new LeafMatchAll(ImmutableList.of(this, other));
        }

        @Override
        LeafMatchAny or(final LeafMatch other) {
            return new LeafMatchAny(ImmutableList.of(this, other));
        }

        abstract boolean testValue(Object data);
    }

    private abstract static class LeafMatchValue<T> extends LeafMatch {
        private final @NonNull T value;

        LeafMatchValue(final T value) {
            this.value = requireNonNull(value);
        }

        final @NonNull T value() {
            return value;
        }
    }

    abstract static class LeafMatchComparable<T extends Comparable<T>> extends LeafMatchValue<T> {
        LeafMatchComparable(final T value) {
            super(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final boolean testValue(final Object data) {
            return data != null && testCompare(value().compareTo((T) data));
        }

        abstract boolean testCompare(int valueToData);
    }

    private abstract static class LeafMatchString extends LeafMatchValue<String> {
        LeafMatchString(final String value) {
            super(value);
        }

        @Override
        final boolean testValue(final Object data) {
            return data instanceof String && testString((String) data);
        }

        abstract boolean testString(String str);
    }

    private abstract static class CompositeLeafMatch extends LeafMatch {
        private final ImmutableList<LeafMatch> components;

        CompositeLeafMatch(final List<LeafMatch> components) {
            this.components = ImmutableList.copyOf(components);
        }

        final ImmutableList<LeafMatch> components() {
            return components;
        }

        final ImmutableList<LeafMatch> newComponents(final LeafMatch nextComponent) {
            return ImmutableList.<LeafMatch>builderWithExpectedSize(components.size() + 1)
                .addAll(components)
                .add(nextComponent)
                .build();
        }
    }

    private static final class LeafMatchAll extends CompositeLeafMatch {
        LeafMatchAll(final List<LeafMatch> components) {
            super(components);
        }

        @Override
        LeafMatchAll and(final LeafMatch other) {
            return new LeafMatchAll(newComponents(other));
        }

        @Override
        boolean testValue(final Object data) {
            for (LeafMatch component : components()) {
                if (!component.testValue(data)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class LeafMatchAny extends CompositeLeafMatch {
        LeafMatchAny(final List<LeafMatch> components) {
            super(components);
        }

        @Override
        LeafMatchAny or(final LeafMatch other) {
            return new LeafMatchAny(newComponents(other));
        }

        @Override
        boolean testValue(final Object data) {
            for (LeafMatch component : components()) {
                if (component.testValue(data)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class LeafMatchNot extends LeafMatch {
        private final LeafMatch match;

        LeafMatchNot(final LeafMatch match) {
            this.match = requireNonNull(match);
        }

        @Override
        boolean testValue(final Object data) {
            return !match.testValue(data);
        }

        @Override
        LeafMatch negate() {
            return match;
        }
    }

    private static final class LeafMatchValueEquals<T> extends LeafMatchValue<T> {
        LeafMatchValueEquals(final T value) {
            super(value);
        }

        @Override
        boolean testValue(final Object data) {
            return value().equals(data);
        }
    }

    private static final class LeafMatchStringContains extends LeafMatchString {
        LeafMatchStringContains(final String value) {
            super(value);
        }

        @Override
        boolean testString(final String str) {
            return str.contains(value());
        }
    }

    private static final class LeafMatchStringStartsWith extends LeafMatchString {
        LeafMatchStringStartsWith(final String value) {
            super(value);
        }

        @Override
        boolean testString(final String str) {
            return str.startsWith(value());
        }
    }

    private static final class LeafMatchStringEndsWith extends LeafMatchString {
        LeafMatchStringEndsWith(final String value) {
            super(value);
        }

        @Override
        boolean testString(final String str) {
            return str.endsWith(value());
        }
    }

    private static final class LeafMatchGreaterThan<T extends Comparable<T>> extends LeafMatchComparable<T> {
        LeafMatchGreaterThan(final T value) {
            super(value);
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData <= 0;
        }
    }

    private static final class LeafMatchGreaterThanOrEqual<T extends Comparable<T>> extends LeafMatchComparable<T> {
        LeafMatchGreaterThanOrEqual(final T value) {
            super(value);
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData < 0;
        }
    }

    private static final class LeafMatchLessThan<T extends Comparable<T>> extends LeafMatchComparable<T> {
        LeafMatchLessThan(final T value) {
            super(value);
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData >= 0;
        }
    }

    private static final class LeafMatchGreaterLessOrEqual<T extends Comparable<T>> extends LeafMatchComparable<T> {
        LeafMatchGreaterLessOrEqual(final T value) {
            super(value);
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData > 0;
        }
    }

















    abstract static class AbstractLeafDOMQueryPredicate extends DOMQueryPredicate {
        AbstractLeafDOMQueryPredicate(final YangInstanceIdentifier relativePath) {
            super(relativePath);
        }

        @Override
        public final boolean test(final NormalizedNode<?, ?> data) {
            return testValue(data instanceof LeafNode ? ((LeafNode<?>) data).getValue() : null);
        }

        abstract boolean testValue(Object data);
    }

    abstract static class AbstractValueDOMQueryPredicate<T> extends AbstractLeafDOMQueryPredicate {
        private final @NonNull T value;

        AbstractValueDOMQueryPredicate(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath);
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
        AbstractComparableDOMQueryPredicate(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        final boolean testValue(final Object data) {
            return data != null && test(value().compareTo((T) data));
        }

        abstract boolean test(int valueToData);
    }

    abstract static class AbstractStringDOMQueryPredicate extends AbstractValueDOMQueryPredicate<String> {
        AbstractStringDOMQueryPredicate(final YangInstanceIdentifier relativePath, final String value) {
            super(relativePath, value);
        }

        @Override
        final boolean testValue(final Object data) {
            return data instanceof String && test((String) data);
        }

        abstract boolean test(@NonNull String str);
    }

    public static final class Exists extends DOMQueryPredicate {
        public Exists(final YangInstanceIdentifier relativePath) {
            super(relativePath);
        }

        @Override
        public boolean test(final NormalizedNode<?, ?> data) {
            return data != null;
        }
    }

    public static final class Not extends DOMQueryPredicate {
        private final DOMQueryPredicate predicate;

        Not(final DOMQueryPredicate predicate) {
            super(predicate.relativePath);
            this.predicate = predicate;
        }

        public @NonNull DOMQueryPredicate predicate() {
            return predicate;
        }

        @Override
        public DOMQueryPredicate negate() {
            return predicate;
        }

        @Override
        public boolean test(final NormalizedNode<?, ?> data) {
            return !predicate.test(data);
        }
    }

    public static final class ValueEquals<T> extends AbstractValueDOMQueryPredicate<T> {
        public ValueEquals(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath, value);
        }

        @Override
        boolean testValue(final Object data) {
            return value().equals(data);
        }
    }

    public static final class GreaterThan<T extends Comparable<T>> extends AbstractComparableDOMQueryPredicate<T> {
        public GreaterThan(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath, value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData <= 0;
        }
    }

    public static final class GreaterThanOrEqual<T extends Comparable<T>>
            extends AbstractComparableDOMQueryPredicate<T> {
        public GreaterThanOrEqual(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath, value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData < 0;
        }
    }

    public static final class LessThan<T extends Comparable<T>> extends AbstractComparableDOMQueryPredicate<T> {
        public LessThan(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath, value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData >= 0;
        }
    }

    public static final class LessThanOrEqual<T extends Comparable<T>> extends AbstractComparableDOMQueryPredicate<T> {
        public LessThanOrEqual(final YangInstanceIdentifier relativePath, final T value) {
            super(relativePath, value);
        }

        @Override
        boolean test(final int valueToData) {
            return valueToData > 0;
        }
    }

    public static final class StartsWith extends AbstractStringDOMQueryPredicate {
        public StartsWith(final YangInstanceIdentifier relativePath, final String str) {
            super(relativePath, str);
        }

        @Override
        boolean test(final String str) {
            return str.startsWith(value());
        }
    }

    public static final class EndsWith extends AbstractStringDOMQueryPredicate {
        public EndsWith(final YangInstanceIdentifier relativePath, final String str) {
            super(relativePath, str);
        }

        @Override
        boolean test(final String str) {
            return str.endsWith(value());
        }
    }

    public static final class Contains extends AbstractStringDOMQueryPredicate {
        public Contains(final YangInstanceIdentifier relativePath, final String str) {
            super(relativePath, str);
        }

        @Override
        boolean test(final String str) {
            return str.contains(value());
        }
    }

    public static final class MatchesPattern extends AbstractLeafDOMQueryPredicate {
        private final Pattern pattern;

        public MatchesPattern(final YangInstanceIdentifier relativePath, final Pattern pattern) {
            super(relativePath);
            this.pattern = requireNonNull(pattern);
        }

        @Override
        boolean testValue(final Object data) {
            return data instanceof CharSequence && pattern.matcher((CharSequence) data).matches();
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("pattern", pattern);
        }
    }

    private final @NonNull YangInstanceIdentifier relativePath;

    DOMQueryPredicate(final YangInstanceIdentifier relativePath) {
        this.relativePath = requireNonNull(relativePath);
    }

    public final @NonNull YangInstanceIdentifier getPath() {
        return relativePath;
    }

    @Override
    public @NonNull DOMQueryPredicate negate() {
        return new Not(this);
    }

    @Override
    public abstract boolean test(@Nullable NormalizedNode<?, ?> data);

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("path", relativePath)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
