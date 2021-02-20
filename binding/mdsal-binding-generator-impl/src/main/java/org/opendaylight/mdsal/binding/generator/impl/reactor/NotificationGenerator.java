/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static org.opendaylight.mdsal.binding.model.util.BindingTypes.DATA_OBJECT;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.augmentable;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code notification} statement.
 */
public final class NotificationGenerator extends AbstractCompositeGenerator<NotificationEffectiveStatement> {
    NotificationGenerator(final NotificationEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().getIdentifier());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        builder.addImplementsType(DATA_OBJECT);
        builder.addImplementsType(NOTIFICATION);
        builder.addImplementsType(augmentable(builder));
        addUsesInterfaces(builder, builderFactory);

        addConcreteInterfaceMethods(builder);
        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        // No-op
    }
}
