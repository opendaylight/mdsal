/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.QueryStructureException;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class QueryBuilderTest {
    private static BindingNormalizedNodeCodecRegistry CODEC;

    private final QueryFactory factory = new DefaultQueryFactory(CODEC);

    @BeforeClass
    public static void beforeClass() {
        ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        final SchemaContext schemaContext = ctx.tryToCreateSchemaContext().get();

        CODEC = new BindingNormalizedNodeCodecRegistry(
            BindingRuntimeContext.create(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), schemaContext));
    }

    @Test
    public void bar() throws QueryStructureException {
        final QueryExpression<TopLevelList> query = factory.querySubtree(InstanceIdentifier.builder(Top.class).build())
                .extractChild(TopLevelList.class)
                .matching()
                    .childObject(NestedList.class)
                    .leaf(NestedList::getName).contains("foo")
                    .and().leaf(TopLevelList::getName).valueEquals("bar")
                .build();
//
//        // Execution start
//        final Result<TopLevelList> result = query.getResult();
//        // Execution fetch
//        final TopLevelList value = result.getValue();
    }
}
