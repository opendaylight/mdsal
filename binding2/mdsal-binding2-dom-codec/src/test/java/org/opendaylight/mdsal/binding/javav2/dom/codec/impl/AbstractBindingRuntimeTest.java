/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import org.junit.Before;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.runtime.context.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class AbstractBindingRuntimeTest {

    private SchemaContext schemaContext;
    private BindingRuntimeContext runtimeContext;

    @Before
    public void setup() {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        schemaContext = ctx.tryToCreateSchemaContext().get();
        runtimeContext = BindingRuntimeContext.create(ctx, schemaContext);

    }

    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
