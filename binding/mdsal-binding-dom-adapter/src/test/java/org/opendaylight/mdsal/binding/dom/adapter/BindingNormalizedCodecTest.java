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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.AbstractSchemaContext;

public class BindingNormalizedCodecTest extends AbstractSchemaAwareTest {

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST
        .augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES = BA_TOP_LEVEL_LIST
        .augmentation(TreeComplexUsesAugment.class);
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");
    private static final QName NAME_QNAME = QName.create(Top.QNAME, "name");
    private static final YangInstanceIdentifier BI_TOP_LEVEL_LIST = YangInstanceIdentifier.builder().node(Top.QNAME)
        .node(TopLevelList.QNAME).nodeWithKey(TopLevelList.QNAME, NAME_QNAME, TOP_FOO_KEY.getName()).build();

    private CurrentAdapterSerializer serializer;

    @Override
    protected void setupWithRuntimeContext(final BindingRuntimeContext runtimeContext) {
        serializer = new CurrentAdapterSerializer(new BindingCodecContext(runtimeContext));
    }

    @Test
    public void testComplexAugmentationSerialization() {
        final PathArgument lastArg = serializer.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES).getLastPathArgument();
        assertTrue(lastArg instanceof AugmentationIdentifier);
    }

    @Test
    public void testLeafOnlyAugmentationSerialization() {
        final PathArgument leafOnlyLastArg = serializer.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY)
            .getLastPathArgument();
        assertTrue(leafOnlyLastArg instanceof AugmentationIdentifier);
        assertTrue(((AugmentationIdentifier) leafOnlyLastArg).getPossibleChildNames().contains(SIMPLE_VALUE_QNAME));
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
