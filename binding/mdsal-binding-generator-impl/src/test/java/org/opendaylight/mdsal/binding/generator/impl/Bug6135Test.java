/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6135Test {

    @Ignore
    @Test
    public void bug6135Test() throws FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext context = YangParserTestUtils.parseYangSource("/bug-6135/foo.yang");
        assertNotNull(context);

        final List<Type> generateTypes = new BindingGeneratorImpl(false).generateTypes(context);
        assertFalse(generateTypes.isEmpty());

        GeneratedType genInterface = null;
        for (final Type type : generateTypes) {
            if (type.getName().equals("TestLeafrefData")) {
                genInterface = (GeneratedType) type;
                break;
            }
        }
        assertNotNull(genInterface);
        final List<Enumeration> enums = genInterface.getEnumerations();
        assertEquals(2, enums.size());
    }
}