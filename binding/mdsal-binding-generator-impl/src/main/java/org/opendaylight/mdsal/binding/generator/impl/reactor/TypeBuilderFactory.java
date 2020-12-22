/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.RuntimeEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.RuntimeGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;

/**
 * A factory component creating {@link TypeBuilder} instances.
 */
@Beta
@NonNullByDefault
public abstract class TypeBuilderFactory implements Immutable {
    static final class Codegen extends TypeBuilderFactory {
        private static final Codegen INSTANCE = new Codegen();

        private Codegen() {
            // Hidden on purpose
        }

        @Override
        GeneratedTOBuilder newGeneratedTOBuilder(final JavaTypeName identifier) {
            return new CodegenGeneratedTOBuilder(identifier);
        }

        @Override
        GeneratedTypeBuilder newGeneratedTypeBuilder(final JavaTypeName identifier) {
            return new CodegenGeneratedTypeBuilder(identifier);
        }

        @Override
        AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
            return new CodegenEnumerationBuilder(identifier);
        }
    }

    static final class Runtime extends TypeBuilderFactory {
        private static final Runtime INSTANCE = new Runtime();

        private Runtime() {
            // Hidden on purpose
        }

        @Override
        GeneratedTOBuilder newGeneratedTOBuilder(final JavaTypeName identifier) {
            return new RuntimeGeneratedTOBuilder(identifier);
        }

        @Override
        GeneratedTypeBuilder newGeneratedTypeBuilder(final JavaTypeName identifier) {
            return new RuntimeGeneratedTypeBuilder(identifier);
        }

        @Override
        AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
            return new RuntimeEnumerationBuilder(identifier);
        }
    }

    TypeBuilderFactory() {
        // Hidden on purpose
    }

    public static TypeBuilderFactory codegen() {
        return Codegen.INSTANCE;
    }

    public static TypeBuilderFactory runtime() {
        return Runtime.INSTANCE;
    }

    abstract AbstractEnumerationBuilder newEnumerationBuilder(JavaTypeName identifier);

    abstract GeneratedTOBuilder newGeneratedTOBuilder(JavaTypeName identifier);

    abstract GeneratedTypeBuilder newGeneratedTypeBuilder(JavaTypeName identifier);
}
