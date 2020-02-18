/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BinaryTypeTest {
    private static final List<File> YANG_MODELS = new ArrayList<>();
    private static final URL YANG_MODELS_FOLDER = AugmentedTypeTest.class.getResource("/binary-type-test-models");

    @BeforeClass
    public static void loadTestResources() throws URISyntaxException {
        final File augFolder = new File(YANG_MODELS_FOLDER.toURI());
        for (final File fileEntry : augFolder.listFiles()) {
            if (fileEntry.isFile()) {
                YANG_MODELS.add(fileEntry);
            }
        }
    }

    @Test
    public void binaryTypeTest() {
        final SchemaContext context = YangParserTestUtils.parseYangFiles(YANG_MODELS);

        assertNotNull("context is null", context);
        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        //TODO: implement test
    }
}
