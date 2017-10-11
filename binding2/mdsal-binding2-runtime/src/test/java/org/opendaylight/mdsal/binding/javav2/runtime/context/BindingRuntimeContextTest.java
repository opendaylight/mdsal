/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.runtime.context;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingRuntimeContextTest {

    private SchemaContext schemaContext;
    private BindingRuntimeContext brc;
    private DataNodeContainer myCont;

    @Before
    public void setup() {
        schemaContext = YangParserTestUtils.parseYangResource("/yang/test-runtime.yang");
        myCont = (DataNodeContainer) schemaContext.getChildNodes().iterator().next();
        brc = BindingRuntimeContext.create(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), schemaContext);
    }

    @Test
    public void basicTest() {
        assertNotNull(brc.getSchemaContext());
        assertNotNull(brc.getStrategy());
        assertNotNull(brc.getChoiceCaseChildren(myCont));
    }
}
