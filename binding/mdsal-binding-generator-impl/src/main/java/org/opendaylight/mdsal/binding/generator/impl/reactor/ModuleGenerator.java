/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code module} statement. These generators are roots for generating types for a
 * particular {@link QNameModule} as mapped into the root package.
 */
public final class ModuleGenerator extends AbstractCompositeGenerator<ModuleEffectiveStatement> {
    private final @NonNull JavaTypeName yangModuleInfo;

    ModuleGenerator(final ModuleEffectiveStatement statement) {
        super(statement);
        yangModuleInfo = JavaTypeName.create(javaPackage(), BindingMapping.MODULE_INFO_CLASS_NAME);
    }

    @Override
    ModuleGenerator currentModule() {
        return this;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    String createJavaPackage() {
        return BindingMapping.getRootPackageName(statement().localQNameModule());
    }

    @Override
    Member createMember() {
        return domain().addPrimary(new ModuleNamingStrategy(statement().argument()));
    }

    @Override
    ClassPlacement classPlacement() {
        for (Generator child : this) {
            if (child instanceof AbstractExplicitGenerator
                && ((AbstractExplicitGenerator<?>) child).statement() instanceof DataTreeEffectiveStatement) {
                switch (child.classPlacement()) {
                    case MEMBER:
                    case PHANTOM:
                    case TOP_LEVEL:
                        return ClassPlacement.TOP_LEVEL;
                    default:
                        break;
                }
            }
        }
        return ClassPlacement.NONE;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.setModuleName(statement().argument().getLocalName());
        builder.addImplementsType(BindingTypes.DATA_ROOT);

        final int usesCount = addUsesInterfaces(builder, builderFactory);
        // if we have more than 2 top level uses statements we need to define getImplementedInterface() on the top-level
        // DataRoot object
        if (usesCount > 1) {
            narrowImplementedInterface(builder);
        }

        for (Generator child : this) {
            child.ensureType(builderFactory);
        }

//      addCodegenInformation(builder, module);
        return builder.build();
    }

    void addQNameConstant(final GeneratedTypeBuilderBase<?> builder, final String localName) {
        builder.addConstant(BindingTypes.QNAME, BindingMapping.QNAME_STATIC_FIELD_NAME,
            Map.entry(yangModuleInfo, localName));
    }
}
