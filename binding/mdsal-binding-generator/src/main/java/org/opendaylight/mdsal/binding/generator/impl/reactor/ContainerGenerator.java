/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultContainerRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code container} statement.
 */
final class ContainerGenerator extends CompositeSchemaTreeGenerator<ContainerEffectiveStatement, ContainerRuntimeType> {

    /*
     *  Indicates current container is a root of a 'yang-data' content.
     */
    private final boolean isYangDataRoot;

    ContainerGenerator(final ContainerEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        this(statement, parent, false);
    }

    ContainerGenerator(final ContainerEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent,
            final boolean isYangDataRoot) {
        super(statement, parent);
        this.isYangDataRoot = isYangDataRoot;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        addImplementsChildOf(builder);
        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());
//      builder.setSchemaPath(node.getPath());

        return builder.build();
    }

    @Override
    CompositeRuntimeTypeBuilder<ContainerEffectiveStatement, ContainerRuntimeType> createBuilder(
            final ContainerEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ContainerRuntimeType build(final GeneratedType type, final ContainerEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultContainerRuntimeType(type, statement, children, augments);
            }
        };
    }

    @Override
    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        final MethodSignatureBuilder ret = super.constructGetter(builder, returnType)
                .setMechanics(ValueMechanics.NORMAL);
        if (statement().findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isEmpty()) {
            final MethodSignatureBuilder nonnull = builder
                    .addMethod(BindingMapping.getNonnullMethodName(localName().getLocalName()))
                    .setReturnType(returnType)
                    .setDefault(false);
            annotateDeprecatedIfNecessary(nonnull);
        }
        return ret;
    }

    @Override
    void addAsGetterMethodOverride(final @NonNull GeneratedTypeBuilderBase<?> builder,
            final @NonNull TypeBuilderFactory builderFactory) {
        // This getter method construction is only invoked if current container is being marked
        // as 'addedByUses'. Generating a getter method is required for case when container represents 'yang-data'
        // with content being defined via usage of grouping.
        if (isYangDataRoot) {
            // extract type from original statement (grouping) resolved
            final Type returnType = getOriginal().methodReturnType(builderFactory);
            constructGetter(builder, returnType);
            constructRequire(builder, returnType);
        }
    }

}
