/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.test;

import com.google.common.annotations.Beta;
import org.junit.Before;
import org.opendaylight.mdsal.binding.javav2.runtime.context.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public abstract class AbstractSchemaAwareTest {

    private Iterable<YangModuleInfo> moduleInfos;
    private SchemaContext schemaContext;

    protected Iterable<YangModuleInfo> getModuleInfos() throws Exception {
        return BindingReflections.loadModuleInfos();
    }

    @Before
    public final void setup() throws Exception {
        moduleInfos = getModuleInfos();
        final ModuleInfoBackedContext moduleContext = ModuleInfoBackedContext.create();
        moduleContext.addModuleInfos(moduleInfos);
        schemaContext = moduleContext.tryToCreateSchemaContext().get();
        setupWithSchema(schemaContext);
    }

    /**
     * Setups test with Schema context.
     *
     * @param context
     *            schema context
     */
    protected abstract void setupWithSchema(SchemaContext context);
}
