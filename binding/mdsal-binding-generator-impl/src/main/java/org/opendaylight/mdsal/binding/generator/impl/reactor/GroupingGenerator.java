/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code grouping} statement.
 */
public final class GroupingGenerator extends AbstractCompositeGenerator<GroupingEffectiveStatement> {
    GroupingGenerator(final GroupingEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.GROUPING;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterGrouping(statement().argument());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        narrowImplementedInterface(builder);
        addUsesInterfaces(builder, builderFactory);

//        annotateDeprecatedIfNecessary(grouping, genType);
//        context.addGroupingType(grouping, genType);
//        resolveDataSchemaNodes(context, genType, genType, grouping.getChildNodes(), true);
//        groupingsToGenTypes(context, grouping.getGroupings());
//        processUsesAugments(grouping, context, true);
//        actionsToGenType(context, genType, grouping, null, true);
//        notificationsToGenType(context, genType, grouping, null, true);

        // FIXME: implement this
        return builder.build();
    }
}
