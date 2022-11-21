/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.extension.yang.ext.rev130709.$YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class RpcRoutingStrategyTest {
    private static List<RpcEffectiveStatement> RPCS;

    @BeforeClass
    public static void beforeClass() {
        final EffectiveModelContext ctx = YangParserTestUtils.parseYangSources(YangParserConfiguration.DEFAULT, null,
            YangTextSchemaSource.delegateForByteSource("yang-ext.yang",
                $YangModuleInfoImpl.getInstance().getYangTextByteSource()),
            YangTextSchemaSource.forResource(RpcRoutingStrategy.class, "/rpc-routing-strategy.yang"));

        RPCS = Iterables.getOnlyElement(ctx.findModuleStatements("foo"))
            .streamEffectiveSubstatements(RpcEffectiveStatement.class)
            .collect(Collectors.toUnmodifiableList());
    }

    @AfterClass
    public static void afterClass() {
        RPCS = null;
    }

    @Test
    public void unroutedRpcStrategyTest() {
        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(Iterables.get(RPCS, 1));
        assertNotNull(strategy);

        assertEquals(QName.create("foo", "unrouted"), strategy.getIdentifier());
        assertFalse(strategy.isContextBasedRouted());
        assertThrows(UnsupportedOperationException.class, () -> strategy.getLeaf());
        assertThrows(UnsupportedOperationException.class, () -> strategy.getContext());
    }

    @Test
    public void routedRpcStrategyTest() {
        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(Iterables.get(RPCS, 0));
        assertNotNull(strategy);

        assertEquals(QName.create("foo", "routed"), strategy.getIdentifier());
        assertTrue(strategy.isContextBasedRouted());
        assertEquals(QName.create("foo", "identity"), strategy.getContext());
        assertEquals(QName.create("foo", "ctx"), strategy.getLeaf());
    }
}