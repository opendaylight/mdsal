/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ActionLookupTest {
    private static BindingToNormalizedNodeCodec CODEC;

    @BeforeClass
    public static void beforeClass() {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());

        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        CODEC = new BindingToNormalizedNodeCodec(ctx, registry);
        CODEC.onGlobalContextUpdated(ctx.tryToCreateSchemaContext().get());
    }

    @AfterClass
    public static void afterClass() {
        CODEC = null;
    }

    @Test
    public void testActionSchemaPath() {
        SchemaPath path = CODEC.getActionPath(Foo.class);
        assertEquals(SchemaPath.create(true, Cont.QNAME, QName.create(Cont.QNAME, "foo")), path);
    }
}
