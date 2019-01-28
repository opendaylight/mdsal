/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import java.util.Set;
import org.junit.Before;
import org.opendaylight.mdsal.binding.generator.impl.FixedModuleInfoSchemaContextProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class AbstractSchemaAwareTest {

    private final FixedModuleInfoSchemaContextProvider delegate = new FixedModuleInfoSchemaContextProvider();

    @Before
    public final void setup() throws Exception {
        setupWithSchema(getSchemaContext());
    }

    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return delegate.getModuleInfos();
    }

    protected SchemaContext getSchemaContext() throws Exception {
        return delegate.getSchemaContext();
    }

    /**
     * Setups test with Schema context.
     *
     * @param context schema context
     */
    protected abstract void setupWithSchema(SchemaContext context);
}
