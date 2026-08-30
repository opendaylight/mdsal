/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.dom.adapter.ContextReferenceExtractor.Direct;
import org.opendaylight.mdsal.binding.dom.adapter.ContextReferenceExtractor.GetValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.EncapsulatedRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.EncapsulatedRouteInGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.RoutedSimpleRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.RoutedSimpleRouteInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.RoutedTransitiveEncapsulatedInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.RoutedTransitiveEncapsulatedInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

class ContextExtractorTest {
    private static final DataObjectIdentifier<?> TEST_ROUTE = DataObjectIdentifier.builder(Top.class).build();
    private static final RoutedTransitiveEncapsulatedInput TRANSITIVE = new RoutedTransitiveEncapsulatedInputBuilder()
        .setRoute(new EncapsulatedRoute(TEST_ROUTE))
        .build();

    @Test
    void testNonRoutedExtraction() {
        assertNull(ContextReferenceExtractor.of(RockTheHouseInput.class));
    }

    @Test
    void testRoutedSimpleExtraction() {
        final var extractor = assertInstanceOf(Direct.class,
            ContextReferenceExtractor.of(RoutedSimpleRouteInput.class));
        assertSame(TEST_ROUTE, extractor.extract(new RoutedSimpleRouteInputBuilder().setRoute(TEST_ROUTE).build()));
    }

    @Test
    void testRoutedEncapsulatedExtraction() {
        final var extractor = assertInstanceOf(GetValue.class,
            ContextReferenceExtractor.of(EncapsulatedRouteInGrouping.class));
        assertSame(TEST_ROUTE, extractor.extract(TRANSITIVE));
    }

    @Test
    void testRoutedEncapsulatedTransitiveExtraction() {
        final var extractor = assertInstanceOf(GetValue.class,
            ContextReferenceExtractor.of(RoutedTransitiveEncapsulatedInput.class));
        assertSame(TEST_ROUTE, extractor.extract(TRANSITIVE));
    }
}
