/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.extension.yang.ext.rev130709.$YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ContentRoutedRpcContextTest {
    private static List<RpcEffectiveStatement> RPCS;

    @BeforeClass
    public static void beforeClass() {
        final var ctx = YangParserTestUtils.parseYangSources(YangParserConfiguration.DEFAULT, null,
            YangTextSchemaSource.delegateForByteSource("yang-ext.yang",
                $YangModuleInfoImpl.getInstance().getYangTextByteSource()),
            YangTextSchemaSource.forResource(ContentRoutedRpcContext.class, "/rpc-routing-strategy.yang"));

        RPCS = ctx.findModuleStatements("foo").iterator().next()
            .streamEffectiveSubstatements(RpcEffectiveStatement.class)
            .collect(Collectors.toUnmodifiableList());
    }

    @AfterClass
    public static void afterClass() {
        RPCS = null;
    }

    @Test
    public void unroutedRpcStrategyTest() {
        assertNull(ContentRoutedRpcContext.forRpc(RPCS.get(1)));
    }

    @Test
    public void routedRpcStrategyTest() {
        final var context = ContentRoutedRpcContext.forRpc(RPCS.get(0));
        assertNotNull(context);

        assertEquals(QName.create("foo", "identity"), context.identity());
        assertEquals(QName.create("foo", "ctx"), context.leaf());
    }
}