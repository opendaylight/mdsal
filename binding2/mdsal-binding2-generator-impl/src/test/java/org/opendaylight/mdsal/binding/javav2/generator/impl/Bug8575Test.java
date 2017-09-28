/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import java.io.File;
import java.lang.reflect.Method;


public class Bug8575Test {
    @Test
    public void bug8575Test() throws Exception {
        final Class[] parameterTypes = { SchemaContext.class, SchemaPath.class, UsesNode.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("findOriginalTargetFromGrouping", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        File foo = new File(getClass().getResource(
                "/bug-8575/foo.yang").toURI());

        SchemaContext context = YangParserTestUtils.parseYangFiles(foo);

        final QName groupingQname = QName.create("foo", "2017-05-15", "A");
        final QName containerQname = QName.create("foo", "2017-05-15", "A1");
        final SchemaPath groupingPath = SchemaPath.create(true, groupingQname);
        final SchemaPath targetPath = SchemaPath.create(true, containerQname);

        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(groupingPath);

        final Object[] args = { context, targetPath, usesNode };
        final DataSchemaNode result = (DataSchemaNode) generate.invoke(AugmentToGenType.class, args);
        assertNotNull(result);
        assertTrue(result instanceof ContainerSchemaNode);
    }
}
