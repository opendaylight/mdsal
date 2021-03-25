/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.yang.types.AbstractTypeProvider;
import org.opendaylight.mdsal.binding.yang.types.CodegenTypeProvider;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class TypeProviderIntegrationTest {
    private static final String PKG = "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.";
    private static SchemaContext CONTEXT;
    private AbstractTypeProvider provider;
    private Module module;

    @BeforeClass
    public static void setup() {
        CONTEXT = YangParserTestUtils.parseYangResources(TypeProviderIntegrationTest.class, "/type-provider/test.yang",
            "/ietf-models/ietf-inet-types.yang");
        assertNotNull(CONTEXT);
    }

    @AfterClass
    public static void teardown() {
        CONTEXT = null;
    }

    @Before
    public void init() {
        provider = new CodegenTypeProvider(CONTEXT, ImmutableMap.of());
        module = CONTEXT.findModule("test", Revision.of("2013-10-08")).get();
    }

    @Test
    public void testGetParamNameFromType() {
        module = CONTEXT.findModule("ietf-inet-types", Revision.of("2010-09-24")).get();
        TypeDefinition<?> ipv4 = null;
        TypeDefinition<?> ipv6 = null;
        TypeDefinition<?> ipv4Pref = null;
        TypeDefinition<?> ipv6Pref = null;
        for (TypeDefinition<?> type : module.getTypeDefinitions()) {
            if ("ipv4-address".equals(type.getQName().getLocalName())) {
                ipv4 = type;
            } else if ("ipv6-address".equals(type.getQName().getLocalName())) {
                ipv6 = type;
            } else if ("ipv4-prefix".equals(type.getQName().getLocalName())) {
                ipv4Pref = type;
            } else if ("ipv6-prefix".equals(type.getQName().getLocalName())) {
                ipv6Pref = type;
            }
        }

        assertNotNull(ipv4);
        assertNotNull(ipv6);
        assertNotNull(ipv4Pref);
        assertNotNull(ipv6Pref);
        assertEquals("ipv4Address", provider.getParamNameFromType(ipv4));
        assertEquals("ipv6Address", provider.getParamNameFromType(ipv6));
        assertEquals("ipv4Prefix", provider.getParamNameFromType(ipv4Pref));
        assertEquals("ipv6Prefix", provider.getParamNameFromType(ipv6Pref));
    }
}
