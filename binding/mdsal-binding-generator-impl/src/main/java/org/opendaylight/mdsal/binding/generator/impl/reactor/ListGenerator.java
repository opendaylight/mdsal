/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static org.opendaylight.mdsal.binding.model.util.BindingTypes.identifiable;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
public final class ListGenerator extends AbstractCompositeGenerator<ListEffectiveStatement> {
    private KeyGenerator keyGen;

    ListGenerator(final ListEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    void linkCompositeDependencies(final GeneratorContext context) {
        super.linkCompositeDependencies(context);
        if (producesType()) {
            keyGen = statement().findFirstEffectiveSubstatement(KeyEffectiveStatement.class)
                .map(stmt -> getParent().getGenerator(stmt))
                .map(KeyGenerator.class::cast)
                .orElse(null);
        }
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        addImplementsChildOf(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);
        annotateDeprecatedIfNecessary(builder);

        //    final Module module = context.module();
        //    builder.setModuleName(module.getName());
        //    addCodegenInformation(builder, module, node);
        //    builder.setSchemaPath(node.getPath());

        final GeneratedType keyType = findKeyType(builderFactory);
        if (keyType != null) {
            // Add yang.binding.Identifiable and its key() method
            builder.addImplementsType(identifiable(keyType));
            builder.addMethod(BindingMapping.IDENTIFIABLE_KEY_NAME)
                .setReturnType(keyType)
                .addAnnotation(OVERRIDE_ANNOTATION);
        }

        addGetterMethods(builder, builderFactory);

        return builder.build();
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory) {
        final Type generatedType = super.methodReturnType(builderFactory);
        // We are wrapping the generated type in either a List or a Map based on presence of the key
        final GeneratedType keyType = findKeyType(builderFactory);
        return keyType != null ? Types.mapTypeFor(keyType, generatedType) : Types.listTypeFor(generatedType);
    }

    @Override
    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        final MethodSignatureBuilder ret = super.constructGetter(builder, returnType)
            .setMechanics(ValueMechanics.NULLIFY_EMPTY);

        final MethodSignatureBuilder nonnull = builder
            .addMethod(BindingMapping.getNonnullMethodName(localName().getLocalName()))
            .setReturnType(returnType)
            .setDefault(true);
        annotateDeprecatedIfNecessary(nonnull);

        return ret;
    }

    private @Nullable GeneratedType findKeyType(final TypeBuilderFactory builderFactory) {
        if (keyGen != null) {
            final Ordering ordering = statement()
                .findFirstEffectiveSubstatementArgument(OrderedByEffectiveStatement.class)
                .orElse(Ordering.SYSTEM);
            if (ordering == Ordering.SYSTEM) {
                return keyGen.getGeneratedType(builderFactory);
            }
        }
        return null;
    }
}
