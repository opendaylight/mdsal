/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.runtime.context.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.gen.javav2.org.test.runtime.rev170710.data.MyCont;
import org.opendaylight.mdsal.gen.javav2.org.test.runtime.rev170710.data.my_cont.MyChoice;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingSchemaContextUtilsTest {

    private SchemaContext schemaContext;
    private DataNodeContainer myCont;
    private ChoiceSchemaNode myChoice;

    private static final InstanceIdentifier<MyCont> MY_CONT_NODE_PATH
            = InstanceIdentifier.create(MyCont.class);


    @Before
    public void setup() {
        schemaContext = YangParserTestUtils.parseYangResource("/yang/test-runtime.yang");
        myCont = (DataNodeContainer) schemaContext.getChildNodes().iterator().next();
        myChoice = (ChoiceSchemaNode) myCont.getChildNodes().iterator().next();
    }

    @Test
    public void utilTest() {
        assertNotNull(BindingSchemaContextUtils.findDataNodeContainer(schemaContext, MY_CONT_NODE_PATH));
        assertNotNull(BindingSchemaContextUtils.findInstantiatedChoice(myCont, MyChoice.class));
        assertNotNull(BindingSchemaContextUtils.findInstantiatedCase(myChoice, myChoice.findCaseNodes("one")
            .iterator().next()));
    }
}
