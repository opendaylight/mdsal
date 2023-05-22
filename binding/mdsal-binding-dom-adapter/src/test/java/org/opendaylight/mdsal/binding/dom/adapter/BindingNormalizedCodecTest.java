/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractSchemaAwareTest;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.AbstractSchemaContext;

public class BindingNormalizedCodecTest extends AbstractSchemaAwareTest {

    private CurrentAdapterSerializer serializer;

    @Override
    protected void setupWithRuntimeContext(final BindingRuntimeContext runtimeContext) {
        serializer = new CurrentAdapterSerializer(new BindingCodecContext(runtimeContext));
    }

    @Test
    public void testGetRpcMethodToQName() {
        assertTrue(serializer.createQNameToMethod(OpendaylightTestRpcServiceService.class)
            .values().stream()
            .map(Method::getName)
            .anyMatch("rockTheHouse"::equals));
    }

    static class EmptyEffectiveModelContext extends AbstractSchemaContext implements EffectiveModelContext {
        @Override
        public Set<Module> getModules() {
            return ImmutableSet.of();
        }

        @Override
        protected Map<QNameModule, Module> getModuleMap() {
            return ImmutableMap.of();
        }

        @Override
        protected SetMultimap<XMLNamespace, Module> getNamespaceToModules() {
            return ImmutableSetMultimap.of();
        }

        @Override
        protected SetMultimap<String, Module> getNameToModules() {
            return ImmutableSetMultimap.of();
        }

        @Override
        public Map<QNameModule, ModuleEffectiveStatement> getModuleStatements() {
            return ImmutableMap.of();
        }
    }
}
