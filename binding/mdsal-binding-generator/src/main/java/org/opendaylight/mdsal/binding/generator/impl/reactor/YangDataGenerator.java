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
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultYangDataRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code rc:yang-data} statement.
 */
final class YangDataGenerator extends AbstractCompositeGenerator<YangDataEffectiveStatement, YangDataRuntimeType> {

    private final QName qname;

    YangDataGenerator(final YangDataEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
        // explicitly define qname due to statement().argument() is string
        qname = QName.create(parent.statement().localQNameModule(), statement().argument());
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(qname);
    }

    @Override
    @NonNull
    AbstractQName localName() {
        return qname;
    }

    @Override
    @NonNull
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

        builder.addImplementsType(BindingTypes.DATA_ROOT);
        addUsesInterfaces(builder, builderFactory);

        builder.addImplementsType(BindingTypes.DATA_OBJECT); // getImplementedMethods()
        addConcreteInterfaceMethods(builder);

        addAugmentable(builder); // required to generate a Builder

        addGetterMethods(builder, builderFactory);

        final ModuleGenerator module = currentModule();
        builder.setModuleName(module.statement().argument().getLocalName());
        builderFactory.addCodegenInformation(module, statement(), builder);

        return builder.build();
    }

    @Override
    @NonNull CompositeRuntimeTypeBuilder<YangDataEffectiveStatement, YangDataRuntimeType> createBuilder(
            YangDataEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {

            @Override
            YangDataRuntimeType build(final GeneratedType type, final YangDataEffectiveStatement statement,
                    final List<RuntimeType> children, List<AugmentRuntimeType> augments) {
                return new DefaultYangDataRuntimeType(type, statement, children);
            }
        };
    }

    @Override
    void addAsGetterMethod(@NonNull GeneratedTypeBuilderBase<?> builder, @NonNull TypeBuilderFactory builderFactory) {
        // is not a part of any structure
    }

}
