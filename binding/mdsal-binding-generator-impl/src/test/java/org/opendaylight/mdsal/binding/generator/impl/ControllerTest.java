/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ControllerTest {
    @Test
    public void controllerAugmentationTest() {
        final List<Type> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            ControllerTest.class,
            "/controller-models/controller-network.yang", "/controller-models/controller-openflow.yang",
            "/ietf-models/ietf-inet-types.yang"));

        assertNotNull(genTypes);
        assertTrue(!genTypes.isEmpty());
    }
}
