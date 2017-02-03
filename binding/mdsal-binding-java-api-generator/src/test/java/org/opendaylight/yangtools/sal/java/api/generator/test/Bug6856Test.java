/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.sal.java.api.generator.test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6856Test {

    @Test
    public void test() throws Exception {
        final File yang = new File(getClass().getResource("/rpctest.yang").toURI());
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSources(yang);
        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-02-03");
        final Module rpcModule = schemaContext.findModuleByName("rpctest", revision);
        assertNotNull(rpcModule);

        final Set<RpcDefinition> rpcs = rpcModule.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition testRpc = rpcs.iterator().next();
        final ContainerSchemaNode input = testRpc.getInput();
        assertNotNull(input);

        final ContainerSchemaNode output = testRpc.getOutput();
        assertNotNull(output);
    }
}
