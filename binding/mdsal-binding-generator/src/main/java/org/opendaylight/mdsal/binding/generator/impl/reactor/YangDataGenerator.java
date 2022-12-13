/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultYangDataRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code rc:yang-data} statement.
 */
final class YangDataGenerator extends AbstractCompositeGenerator<YangDataEffectiveStatement, YangDataRuntimeType> {

    YangDataGenerator(final YangDataEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        final QNameModule moduleQName = currentModule().getQName().getModule();
        dataTree.enterYangData(moduleQName, statement().argument());
    }

    @Override
    ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        final String templateName = statement().argument();
        final ClassNamingStrategy namingStrategy = new YangDataNamingStrategy(namespace(), templateName, "yang-data");

        // in case yang-data template name matches a child name, use name strategy fallback immediately
        // to use alternative name because any child won't be checking for name conflict with yang-data artifact
        // created at same package
        final boolean hasNameConflict = statement().effectiveSubstatements().stream()
                .anyMatch(stmt -> stmt.argument() instanceof QName qname && templateName.equals(qname.getLocalName()));
        return domain.addPrimary(this, hasNameConflict ? namingStrategy.fallback() : namingStrategy);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.YANG_DATA;
    }

    @Override
    String createJavaPackage() {
        return getPackageParent().javaPackage(); // set same package for child generators
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        // FIXME: MDSAL-808: add a NAME constant

        builder.addImplementsType(BindingTypes.yangData(builder));
        addUsesInterfaces(builder, builderFactory);

        addGetterMethods(builder, builderFactory);

        final ModuleGenerator module = currentModule();
        builder.setModuleName(module.statement().argument().getLocalName());
        builderFactory.addCodegenInformation(module, statement(), builder);

        return builder.build();
    }

    @Override
    CompositeRuntimeTypeBuilder<YangDataEffectiveStatement, YangDataRuntimeType> createBuilder(
            final YangDataEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            YangDataRuntimeType build(final GeneratedType type, final YangDataEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultYangDataRuntimeType(type, statement, children);
            }
        };
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // is not a part of any structure
    }
}
