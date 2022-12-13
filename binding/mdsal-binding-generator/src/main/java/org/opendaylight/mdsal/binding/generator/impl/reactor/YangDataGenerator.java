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
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultYangDataRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code rc:yang-data} statement.
 */
final class YangDataGenerator extends AbstractCompositeGenerator<YangDataEffectiveStatement, YangDataRuntimeType> {
    // FIXME: not needed once argument is yang.common.YangDataName
    private final @NonNull QNameModule module;

    YangDataGenerator(final YangDataEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
        module = parent.statement().localQNameModule();
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterYangData(module, statement().argument());
    }

    @Override
    ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        final var templateName = statement().argument();
        final var localName = UnresolvedQName.tryLocalName(templateName);
        if (localName != null) {
            // Template name conforms to 'identifier' ABNF rules, use camel-case strategy
            return domain.addPrimary(this, new CamelCaseNamingStrategy(namespace(), localName));
        }

        // Template name is some random string: use bijective mapping, which deals ensures this is unique in this
        // collision domain.
        // FIXME: we need a NamingStrategy which deals with this
        final var assignedName = BindingMapping.mapYangDataName(templateName);
        throw new UnsupportedOperationException();
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
        addConcreteInterfaceMethods(builder);

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
