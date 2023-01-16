/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.mdsal405.norev.MyUnion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.mdsal405.norev.MyUnion2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.mdsal405.norev.UnionTypedef;

public class Mdsal405Test {

    @Test
    public void testPatternEnforcement() {
        assertDoesNotThrow(() -> new MyUnion2("+12345"));
        assertDoesNotThrow(() -> new MyUnion("+12345"));
        assertDoesNotThrow(() -> new UnionTypedef("g"));
        assertThrows(IllegalArgumentException.class, () -> new MyUnion2("+abcd"));
        assertThrows(IllegalArgumentException.class, () -> new MyUnion("+abcd"));
        assertThrows(IllegalArgumentException.class, () -> new UnionTypedef("h"));
    }
}
