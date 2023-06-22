/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal505Test {
    @Test
    public void testLeafRefCircularReference() {
        final EffectiveModelContext schemaContext =
                YangParserTestUtils.parseYangResource("/mdsal-505/leafref-relative-circular.yang");
        final IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> DefaultBindingGenerator.generateFor(schemaContext));
        assertEquals(
                "Circular leafref chain detected at leaf (urn:xml:ns:yang:lrc?revision=2023-06-22)neighbor3-id",
                iae.getMessage());
    }

    @Test
    public void testLeafRefValidCircularReference() {
        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResource("/mdsal-505/leafref-valid-chain.yang"));
        assertEquals(2, types.size());

        final List<MethodSignature> neighborMethods = types.stream()
                .filter(type -> type.getName().equals("Neighbor"))
                .findFirst()
                .orElseThrow()
                .getMethodDefinitions();
        assertEquals(14, neighborMethods.size());

        final MethodSignature getNeighborId = neighborMethods.stream()
                .filter(method -> method.getName().equals("getNeighborId"))
                .findFirst()
                .orElseThrow();
        assertEquals(Types.STRING, getNeighborId.getReturnType());

        final MethodSignature getNeighbor2Id = neighborMethods.stream()
                .filter(method -> method.getName().equals("getNeighbor2Id"))
                .findFirst()
                .orElseThrow();
        assertEquals(Types.STRING, getNeighbor2Id.getReturnType());
    }
}
