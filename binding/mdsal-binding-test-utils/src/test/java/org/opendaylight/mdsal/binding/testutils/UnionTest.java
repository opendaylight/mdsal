/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import ch.vorburger.xtendbeans.AssertBeans;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.LowestLevel2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.UnionTestType;

/**
 * Tests YANG "type union" gen. code. These used to confuse the hell out of
 * ch.vorburger.xtendbeans v1.2.0, and required a number of fixes in
 * v1.2.1.
 *
 * @author Michael Vorburger
 */
public class UnionTest {

    @Test
    public void testUnionType() {
        AssertBeans.assertEqualByText(
                "new UnionTestType(new LowestLevel2(\"testValue\"))",
                new UnionTestType(new LowestLevel2("testValue")));
    }

}
