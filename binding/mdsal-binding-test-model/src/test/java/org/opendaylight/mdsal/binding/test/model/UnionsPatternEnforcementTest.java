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

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BgpSetMedType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BgpSetMedType2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BgpSetMedType3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.UnionTypedef;

public class UnionsPatternEnforcementTest {

    @Test
    public void testPatternEnforcement() {
        assertDoesNotThrow(() -> new BgpSetMedType("+12345"));
        assertDoesNotThrow(() -> new BgpSetMedType2("+12345"));
        assertDoesNotThrow(() -> new BgpSetMedType3("+12345"));
        assertDoesNotThrow(() -> new UnionTypedef("g"));
        assertThrows(IllegalArgumentException.class, () -> new BgpSetMedType("+abcd"));
        assertThrows(IllegalArgumentException.class, () -> new BgpSetMedType2("+abcd"));
        assertThrows(IllegalArgumentException.class, () -> new BgpSetMedType3("+abcd"));
        assertThrows(IllegalArgumentException.class, () -> new UnionTypedef("h"));
    }
}
