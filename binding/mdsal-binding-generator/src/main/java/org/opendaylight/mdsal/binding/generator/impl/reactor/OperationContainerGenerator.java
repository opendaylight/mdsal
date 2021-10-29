/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.Map;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultInputRuntimeType;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultOutputRuntimeType;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to an {@code input} or an {@code output} statement.
 */
class OperationContainerGenerator
        extends CompositeSchemaTreeGenerator<SchemaTreeEffectiveStatement<?>, OperationContainerGenerator> {
    private final ConcreteType baseInterface;

    OperationContainerGenerator(final InputEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        baseInterface = BindingTypes.RPC_INPUT;
    }

    OperationContainerGenerator(final OutputEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        baseInterface = BindingTypes.RPC_OUTPUT;
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().getIdentifier());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final AbstractCompositeGenerator<?> parent = getParent();
        if (parent instanceof ActionGenerator && ((ActionGenerator) parent).isAddedByUses()) {
            //        final ActionDefinition orig = findOrigAction(parentSchema, action).get();
            //        // Original definition may live in a different module, make sure we account for that
            //        final ModuleContext origContext = moduleContext(
            //            orig.getPath().getPathFromRoot().iterator().next().getModule());
            //        input = context.addAliasType(origContext, orig.getInput(), action.getInput());
            //        output = context.addAliasType(origContext, orig.getOutput(), action.getOutput());

            throw new UnsupportedOperationException("Lookup in original");
        }

        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(baseInterface);
        addAugmentable(builder);

        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);
        addGetterMethods(builder, builderFactory);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        annotateDeprecatedIfNecessary(builder);
        if (builderFactory instanceof TypeBuilderFactory.Codegen) {
            addCodegenInformation(module, statement(), builder);
        }
//                builder.setSchemaPath(schemaNode.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());

        return builder.build();
    }

    @Override
    final CompositeRuntimeType toRuntimeType(final GeneratedType type,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children) {
        final var stmt = statement();
        if (stmt instanceof InputEffectiveStatement) {
            return new DefaultInputRuntimeType(type, (InputEffectiveStatement) stmt, children);
        } else if (stmt instanceof OutputEffectiveStatement) {
            return new DefaultOutputRuntimeType(type, (OutputEffectiveStatement) stmt, children);
        } else {
            throw new IllegalStateException("Unexpected statement " + stmt);
        }
    }
}
