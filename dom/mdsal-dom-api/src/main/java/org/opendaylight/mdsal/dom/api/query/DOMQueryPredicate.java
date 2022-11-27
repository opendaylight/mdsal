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
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A single {@link DOMQuery} predicate. It is composed of a relative path and a match. The relative path needs to be
 * expanded using usual wildcard rules, i.e. NodeIdentifier being used as a 'match all' identifier. For all candidate
 * nodes selected by the relative path, the corresponding match needs to be invoked.
 */
@Beta
@NonNullByDefault
public final class DOMQueryPredicate implements Immutable {
    /**
     * A single match. The primary entrypoint is {@link #test(NormalizedNode)}, but during composition instances may
     * be combined in a manner similar to {@link Predicate}.
     */
    public abstract static class Match implements WritableObject {
        static final byte GREATER_THAN = 0x00;
        static final byte GREATER_THAN_OR_EQUAL = 0x01;
        static final byte LESS_THAN = 0x02;
        static final byte LESS_THAN_OR_EQUAL = 0x03;
        static final byte STRING_MATCHES = 0x04;
        static final byte STRING_STARTS_WITH = 0x05;
        static final byte STRING_ENDS_WITH = 0x06;
        static final byte STRING_CONTAINS = 0x07;
        static final byte VALUE_EQUAL = 0x08;
        static final byte NEGATE = 0x09;
        static final byte AND = 0x0A;
        static final byte OR = 0x0B;
        static final byte EXISTS = 0x0C;

        Match() {
            // Hidden on purpose
        }

        public static final Match exists() {
            return MatchExists.INSTACE;
        }

        public static final <T extends Comparable<T>> Match greaterThan(final T value) {
            return new MatchGreaterThan<>(value);
        }

        public static final <T extends Comparable<T>> Match greaterThanOrEqual(final T value) {
            return new MatchGreaterThanOrEqual<>(value);
        }

        public static final <T extends Comparable<T>> Match lessThan(final T value) {
            return new MatchLessThan<>(value);
        }

        public static final <T extends Comparable<T>> Match lessThanOrEqual(final T value) {
            return new MatchLessThanOrEqual<>(value);
        }

        public static final Match stringMatches(final Pattern pattern) {
            return new MatchStringMatches(pattern);
        }

        public static final Match stringStartsWith(final String str) {
            return new MatchStringStartsWith(str);
        }

        public static final Match stringEndsWith(final String str) {
            return new MatchStringEndsWith(str);
        }

        public static final Match stringContains(final String str) {
            return new MatchStringContains(str);
        }

        public static final <V> Match valueEquals(final V value) {
            return new MatchValueEquals<>(value);
        }

        /**
         * Return a {@link Match} which tests the opposite of this match.
         *
         * @return Negated match.
         */
        public Match negate() {
            return new MatchNot(this);
        }

        public Match and(final Match other) {
            return new MatchAll(ImmutableList.of(this, other));
        }

        public Match or(final Match other) {
            return new MatchAny(ImmutableList.of(this, other));
        }

        public abstract boolean test(@Nullable NormalizedNode data);

        final void appendTo(final StringBuilder sb) {
            sb.append(op()).append('(');
            appendArgument(sb);
            sb.append(')');
        }

        void appendArgument(final StringBuilder sb) {
            // No-op by default
        }

        abstract String op();

        @Override
        public final String toString() {
            final var sb = new StringBuilder();
            appendTo(sb);
            return sb.toString();
        }

        @Override
        public abstract void writeTo(@NonNull DataOutput out) throws IOException;

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            final byte matcherType = in.readByte();
            return switch (matcherType) {
                case GREATER_THAN -> MatchGreaterThan.readFrom(in);
                case GREATER_THAN_OR_EQUAL -> MatchGreaterThanOrEqual.readFrom(in);
                case LESS_THAN -> MatchLessThan.readFrom(in);
                case LESS_THAN_OR_EQUAL -> MatchLessThanOrEqual.readFrom(in);
                case STRING_MATCHES -> MatchStringMatches.readFrom(in);
                case STRING_STARTS_WITH -> MatchStringStartsWith.readFrom(in);
                case STRING_ENDS_WITH -> MatchStringEndsWith.readFrom(in);
                case STRING_CONTAINS -> MatchStringContains.readFrom(in);
                case VALUE_EQUAL -> MatchValueEquals.readFrom(in);
                case NEGATE -> MatchNot.readFrom(in);
                case AND -> MatchAll.readFrom(in);
                case OR -> MatchAny.readFrom(in);
                case EXISTS -> Match.exists();
                default -> throw new IOException("Unknown type " + matcherType);
            };
        }

        static <V> void writeValue(@NonNull final DataOutput out, final V value) throws IOException {
            final var data = serialize(value);
            out.writeInt(data.length);
            out.write(data);
        }

        static @NonNull Object readValue(final DataInput in) throws IOException, ClassNotFoundException {
            final int size = in.readInt();
            final byte[] data = new byte[size];
            in.readFully(data);
            return deserialize(data);
        }
    }

    private static byte[] serialize(final Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    private static Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }

    private static final class MatchAll extends CompositeMatch {
        MatchAll(final ImmutableList<Match> components) {
            super(components);
        }

        @Override
        public MatchAll and(final Match other) {
            return new MatchAll(newComponents(other));
        }

        @Override
        public boolean test(final @Nullable NormalizedNode data) {
            for (Match component : components()) {
                if (!component.test(data)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        String op() {
            return "allOf";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(AND);
            writeComponents(out);
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return new MatchAll(readComponents(in));
        }
    }

    private static final class MatchAny extends CompositeMatch {
        MatchAny(final ImmutableList<Match> components) {
            super(components);
        }

        @Override
        public MatchAny or(final Match other) {
            return new MatchAny(newComponents(other));
        }

        @Override
        public boolean test(final @Nullable NormalizedNode data) {
            for (Match component : components()) {
                if (component.test(data)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        String op() {
            return "anyOf";
        }

        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(OR);
            writeComponents(out);
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return new MatchAny(readComponents(in));
        }
    }

    private static final class MatchExists extends Match {
        static final MatchExists INSTACE = new MatchExists();

        private MatchExists() {
            // Hidden on purpose
        }

        @Override
        public boolean test(final @Nullable NormalizedNode data) {
            return data != null;
        }

        @Override
        String op() {
            return "exists";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(EXISTS);
        }
    }

    private static final class MatchNot extends Match {
        private final Match match;

        MatchNot(final Match match) {
            this.match = requireNonNull(match);
        }

        @Override
        public Match negate() {
            return match;
        }

        @Override
        public boolean test(final @Nullable NormalizedNode data) {
            return !match.test(data);
        }

        @Override
        String op() {
            return "not";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(NEGATE);
            match.writeTo(out);
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.readFrom(in).negate();
        }

        @Override
        void appendArgument(final StringBuilder sb) {
            match.appendTo(sb);
        }
    }

    private static final class MatchValueEquals<T> extends AbstractMatchValue<T> {
        MatchValueEquals(final T value) {
            super(value);
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(VALUE_EQUAL);
            writeValue(out, value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.valueEquals(readValue(in));
        }

        @Override
        String op() {
            return "eq";
        }

        @Override
        boolean testValue(final @Nullable Object data) {
            return value().equals(data);
        }
    }

    private static final class MatchStringContains extends AbstractMatchString {
        MatchStringContains(final String value) {
            super(value);
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(STRING_CONTAINS);
            out.writeUTF(value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException {
            return Match.stringContains(in.readUTF());
        }

        @Override
        String op() {
            return "contains";
        }

        @Override
        boolean testString(final String str) {
            return str.contains(value());
        }
    }

    private static final class MatchStringMatches extends AbstractMatch {
        private final Pattern pattern;

        MatchStringMatches(final Pattern pattern) {
            this.pattern = requireNonNull(pattern);
        }

        @Override
        String op() {
            return "matches";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(STRING_MATCHES);
            writeValue(out, pattern);
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.stringMatches((Pattern) readValue(in));
        }

        @Override
        void appendArgument(final StringBuilder sb) {
            sb.append(pattern);
        }

        @Override
        boolean testValue(final @Nullable Object data) {
            return data instanceof CharSequence && pattern.matcher((CharSequence) data).matches();
        }
    }

    private static final class MatchStringStartsWith extends AbstractMatchString {
        MatchStringStartsWith(final String value) {
            super(value);
        }

        @Override
        String op() {
            return "startsWith";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(STRING_STARTS_WITH);
            out.writeUTF(value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException {
            return Match.stringStartsWith(in.readUTF());
        }

        @Override
        boolean testString(final String str) {
            return str.startsWith(value());
        }
    }

    private static final class MatchStringEndsWith extends AbstractMatchString {
        MatchStringEndsWith(final String value) {
            super(value);
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(STRING_ENDS_WITH);
            out.writeUTF(value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException {
            return Match.stringEndsWith(in.readUTF());
        }

        @Override
        String op() {
            return "endsWith";
        }

        @Override
        boolean testString(final String str) {
            return str.endsWith(value());
        }
    }

    private static final class MatchGreaterThan<T extends Comparable<T>> extends AbstractMatchComparable<T> {
        MatchGreaterThan(final T value) {
            super(value);
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(GREATER_THAN);
            writeValue(out, value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.greaterThan((Comparable) readValue(in));
        }

        @Override
        String op() {
            return "gt";
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData < 0;
        }
    }

    private static final class MatchGreaterThanOrEqual<T extends Comparable<T>> extends AbstractMatchComparable<T> {
        MatchGreaterThanOrEqual(final T value) {
            super(value);
        }

        @Override
        String op() {
            return "gte";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(GREATER_THAN_OR_EQUAL);
            writeValue(out, value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.greaterThanOrEqual((Comparable) readValue(in));
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData <= 0;
        }
    }

    private static final class MatchLessThan<T extends Comparable<T>> extends AbstractMatchComparable<T> {
        MatchLessThan(final T value) {
            super(value);
        }

        @Override
        String op() {
            return "lt";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(LESS_THAN);
            writeValue(out, value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.lessThan((Comparable) readValue(in));
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData > 0;
        }
    }

    private static final class MatchLessThanOrEqual<T extends Comparable<T>> extends AbstractMatchComparable<T> {
        MatchLessThanOrEqual(final T value) {
            super(value);
        }

        @Override
        String op() {
            return "lte";
        }

        @Override
        public void writeTo(@NonNull final DataOutput out) throws IOException {
            out.write(LESS_THAN_OR_EQUAL);
            writeValue(out, value());
        }

        public static @NonNull Match readFrom(final DataInput in) throws IOException, ClassNotFoundException {
            return Match.lessThanOrEqual((Comparable) readValue(in));
        }

        @Override
        boolean testCompare(final int valueToData) {
            return valueToData >= 0;
        }
    }

    private abstract static class CompositeMatch extends Match {
        private final ImmutableList<Match> components;

        CompositeMatch(final ImmutableList<Match> components) {
            this.components = requireNonNull(components);
        }

        final ImmutableList<Match> components() {
            return components;
        }

        final ImmutableList<Match> newComponents(final Match nextComponent) {
            return ImmutableList.<Match>builderWithExpectedSize(components.size() + 1)
                .addAll(components)
                .add(nextComponent)
                .build();
        }

        final void writeComponents(final DataOutput out) throws IOException {
            out.writeInt(components.size());
            for (int i = 0; i < components.size(); i++) {
                components.get(i).writeTo(out);
            }
        }

        static ImmutableList<Match> readComponents(final DataInput in) throws IOException, ClassNotFoundException {
            final int size = in.readInt();
            final var matchers = ImmutableList.<Match>builderWithExpectedSize(size);
            for (int i = 0; i < size; i++) {
                matchers.add(Match.readFrom(in));
            }
            return matchers.build();
        }

        @Override
        final void appendArgument(final StringBuilder sb) {
            final Iterator<Match> it = components.iterator();
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(", ").append(it.next());
            }
        }
    }

    private abstract static class AbstractMatch extends Match {
        AbstractMatch() {
            // Hidden on purpose
        }

        @Override
        public final boolean test(final @Nullable NormalizedNode data) {
            return data instanceof LeafNode ? testValue(((LeafNode<?>) data).body()) : testValue(null);
        }

        abstract boolean testValue(@Nullable Object data);
    }

    private abstract static class AbstractMatchComparable<T extends Comparable<T>> extends AbstractMatchValue<T> {
        AbstractMatchComparable(final T value) {
            super(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        final boolean testValue(final @Nullable Object data) {
            return data != null && testCompare(value().compareTo((T) data));
        }

        /**
         * Evaluate the result of {@code value().compareTo(data)}. Note this result is an inversion of what the match
         * may be named.
         *
         * @param valueToData {@link Comparable#compareTo(Object)} result
         * @return True if the result indicates a match
         */
        abstract boolean testCompare(int valueToData);
    }

    private abstract static class AbstractMatchString extends AbstractMatchValue<String> {
        AbstractMatchString(final String value) {
            super(value);
        }

        @Override
        final boolean testValue(final @Nullable Object data) {
            return data instanceof String && testString((String) data);
        }

        abstract boolean testString(String str);
    }

    private abstract static class AbstractMatchValue<T> extends AbstractMatch {
        private final @NonNull T value;

        AbstractMatchValue(final T value) {
            this.value = requireNonNull(value);
        }

        final @NonNull T value() {
            return value;
        }

        @Override
        final void appendArgument(final StringBuilder sb) {
            sb.append(value);
        }
    }

    private final YangInstanceIdentifier relativePath;
    private final Match match;

    private DOMQueryPredicate(final YangInstanceIdentifier relativePath, final Match match) {
        this.relativePath = requireNonNull(relativePath);
        this.match = requireNonNull(match);
    }

    public static DOMQueryPredicate of(final YangInstanceIdentifier relativePath, final Match match) {
        return new DOMQueryPredicate(relativePath, match);
    }

    public YangInstanceIdentifier relativePath() {
        return relativePath;
    }

    public Match match() {
        return match;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("path", relativePath).add("match", match).toString();
    }
}
