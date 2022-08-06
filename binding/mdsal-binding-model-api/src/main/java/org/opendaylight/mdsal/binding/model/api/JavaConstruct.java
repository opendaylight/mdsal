/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.immutables.value.Value;

/**
 * A top-level Java class file construct. This can be one of
 * <ul>
 *   <li>{@code class}, modeled by {@link JavaConstruct.Class}</li>
 *   <li>{@code enum}, modeled by {@link JavaConstruct.Enum}</li>
 *   <li>{@code interface}, modeled by {@link JavaConstruct.Interface}</li>
 *   <li>{@code record}, modeled by {@link JavaConstruct.Record}</li>
 * </ul>
 */
@Beta
@NonNullByDefault
public sealed interface JavaConstruct {
    /**
     * A Java {@code class}.
     */
    @DoNotMock
    @Value.Immutable
    non-sealed interface Class extends ClassLike {
        // Note: we do not want to model 'static' -- none of our classes are designed to be non-static, therefore it
        //       being static is implied to all nested types
        enum ClassCompleteness {
            /**
             * An {@code abstract} class. Also applies to interface, but we do not model that right now.
             */
            ABSTRACT,
            /**
             * Non-final class. May apply to other constructs, but we do not model that here. This construct has no
             * relevance to {@code default} methods.
             */
            DEFAULT,
            /**
             * A {@code final} class.
             */
            FINAL;
        }

        ClassCompleteness completeness();

        @Nullable Type extendsType();

        static Builder builder() {
            return new Builder();
        }

        final class Builder extends ClassBuilder {
            Builder() {
                // Hidden on purpose
            }
        }
    }

    /**
     * A Java {@code class}.
     */
    @DoNotMock
    @Value.Immutable
    non-sealed interface Enum extends ClassLike {

        static Builder builder() {
            return new Builder();
        }

        final class Builder extends EnumBuilder {
            Builder() {
                // Hidden on purpose
            }
        }
    }

    /**
     * A Java {@code interface}.
     */
    @DoNotMock
    @Value.Immutable
    // stagedBuilder = true, but it is not nice
    non-sealed interface Interface extends JavaConstruct {
        /**
         * The set of interfaces this interface extends.
         *
         * @return set of interfaces this interface extends
         */
        Set<Type> extendsTypes();

        static Builder builder() {
            return new Builder();
        }

        final class Builder extends InterfaceBuilder {
            Builder() {
                // Hidden on purpose
            }
        }
    }

    /**
     * A Java {@code record}.
     */
    @DoNotMock
    @Value.Immutable
    non-sealed interface Record extends ClassLike {
        // Note: order is important
        Map<String, Type> components();

        static Builder builder() {
            return new Builder();
        }

        final class Builder extends RecordBuilder {
            Builder() {
                // Hidden on purpose
            }
        }
    }

    /**
     * Common interface to for {@link Class}, {@link Enum} and {@link Record}.
     */
    sealed interface ClassLike extends JavaConstruct {

        Set<Type> implementsTypes();
    }
}
