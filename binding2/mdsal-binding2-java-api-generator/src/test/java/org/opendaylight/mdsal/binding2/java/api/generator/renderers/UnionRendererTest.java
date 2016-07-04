/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;

public class UnionRendererTest {
    private GeneratedTransferObject genType;

    @Before
    public void setup() {
        final GeneratedTOBuilderImpl generatedTOBuilderImpl = new GeneratedTOBuilderImpl("test.package.name", "testName");
        generatedTOBuilderImpl.setDescription("test description");
        genType = generatedTOBuilderImpl.toInstance();
    }

    @Test
    public void unionRendererTest() {
        //        TO DO implement tests
        String unionRenderer = new UnionRenderer(genType).generateTemplate();
        System.out.println(unionRenderer);
    }
}