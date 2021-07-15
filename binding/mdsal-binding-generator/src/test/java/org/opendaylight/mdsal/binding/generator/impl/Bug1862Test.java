/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug1862Test {
    // FIXME: re-enable this test
    @Ignore
    @Test
    public void restrictedTypedefTransformationTest() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResources(Bug1862Test.class,
            "/base-yang-types.yang", "/test-type-provider.yang");
        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(context);
        assertEquals(1, types.size());
        // FIXME: also explicitly assert bug-1862-restricted-typedef
    }
}