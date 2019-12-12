/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.testkit.spi.EffectiveModelContextTestKit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.AbstractSchemaContext;

public class BindingNormalizedCodecTest {

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

    private EffectiveModelContextTestKit testkit;
    private BindingToNormalizedNodeCodec codec;

    @Before
    public void setup() {
        testkit = new EffectiveModelContextTestKit();
        codec = new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
            new BindingNormalizedNodeCodecRegistry(), true);
    }


    @Test
    public void testComplexAugmentationSerialization() {
        codec.onGlobalContextUpdated(testkit.effectiveModelContext());
        final PathArgument lastArg = this.codec.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES).getLastPathArgument();
        assertTrue(lastArg instanceof AugmentationIdentifier);
    }

    @Test
    public void testLeafOnlyAugmentationSerialization() {
        codec.onGlobalContextUpdated(testkit.effectiveModelContext());
        final PathArgument leafOnlyLastArg = this.codec.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY)
            .getLastPathArgument();
        assertTrue(leafOnlyLastArg instanceof AugmentationIdentifier);
        assertTrue(((AugmentationIdentifier) leafOnlyLastArg).getPossibleChildNames().contains(SIMPLE_VALUE_QNAME));
    }

    @Test
    @SuppressWarnings("checkstyle:illegalCatch")
    public void testToYangInstanceIdentifierBlocking() {
        this.codec.onGlobalContextUpdated(new EmptySchemaContext());

        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<YangInstanceIdentifier> yangId = new AtomicReference<>();
        final AtomicReference<RuntimeException> error = new AtomicReference<>();
        new Thread(() -> {
            try {
                yangId.set(BindingNormalizedCodecTest.this.codec.toYangInstanceIdentifierBlocking(BA_TOP_LEVEL_LIST));
            } catch (final RuntimeException e) {
                error.set(e);
            } finally {
                done.countDown();
            }
        }).start();

        Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
        this.codec.onGlobalContextUpdated(testkit.effectiveModelContext());

        assertTrue("toYangInstanceIdentifierBlocking completed",
                Uninterruptibles.awaitUninterruptibly(done, 3, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw error.get();
        }

        assertEquals("toYangInstanceIdentifierBlocking", BI_TOP_LEVEL_LIST, yangId.get());
    }

    @Test
    public void testGetRpcMethodToSchemaPathWithNoInitialSchemaContext() {
        testGetRpcMethodToSchemaPath();
    }

    @Test
    public void testGetRpcMethodToSchemaPathBlocking() {
        this.codec.onGlobalContextUpdated(new EmptySchemaContext());
        testGetRpcMethodToSchemaPath();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void testGetRpcMethodToSchemaPath() {
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<ImmutableBiMap<Method, SchemaPath>> retMap = new AtomicReference<>();
        final AtomicReference<RuntimeException> error = new AtomicReference<>();
        new Thread(() -> {
            try {
                retMap.set(BindingNormalizedCodecTest.this.codec.getRpcMethodToSchemaPath(
                            OpendaylightTestRpcServiceService.class));
            } catch (final RuntimeException e) {
                error.set(e);
            } finally {
                done.countDown();
            }
        }).start();

        Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
        this.codec.onGlobalContextUpdated(testkit.effectiveModelContext());

        assertTrue("getRpcMethodToSchemaPath completed",
                Uninterruptibles.awaitUninterruptibly(done, 3, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw error.get();
        }

        for (final Method method : retMap.get().keySet()) {
            if (method.getName().equals("rockTheHouse")) {
                return;
            }
        }

        fail("rockTheHouse RPC method not found");
    }

    static class EmptySchemaContext extends AbstractSchemaContext {
        @Override
        public Set<Module> getModules() {
            return ImmutableSet.of();
        }

        @Override
        protected Map<QNameModule, Module> getModuleMap() {
            return ImmutableMap.of();
        }

        @Override
        protected SetMultimap<URI, Module> getNamespaceToModules() {
            return ImmutableSetMultimap.of();
        }

        @Override
        protected SetMultimap<String, Module> getNameToModules() {
            return ImmutableSetMultimap.of();
        }
    }
}
