/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test leafref resolution when the leaf is from a grouping.
 */
public class Mdsal182Test {

    @Test
    public void testOneUpLeafref() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal-182/good-leafref.yang");
        final Collection<Type> types = new BindingGeneratorImpl().generateTypes(context);
        assertEquals(6, types.size());
    }

    @Test
    @Ignore
    public void testTwoUpLeafref() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal-182/grouping-leafref.yang");
        final Collection<Type> types = new BindingGeneratorImpl().generateTypes(context);
        assertNotNull(types);
        assertEquals(24, types.size());
    }
}
