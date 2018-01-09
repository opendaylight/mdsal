/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.test.mock.FooChild;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.test.mock.GroupingFoo;
import org.opendaylight.mdsal.binding.javav2.spec.base.BaseIdentity;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Output;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.yangtools.yang.common.QName;

public class BindingReflectionsTest {

    @Test
    public void testBindingWithDummyObject() throws Exception {
        assertEquals("Package name should be equal to string", "org.opendaylight.mdsal.gen.javav2.test.rev990939",
                BindingReflections.getModelRootPackageName("org.opendaylight.mdsal.gen.javav2.test.rev990939"));
        assertEquals("ModuleInfoClassName should be equal to string", "test.$YangModuleInfoImpl",
                BindingReflections.getModuleInfoClassName("test"));
        assertFalse("Should not be RpcType", BindingReflections.isOperationType(TreeNode.class));
        assertFalse("Should not be AugmentationChild", BindingReflections.isAugmentationChild(TreeNode.class));
        assertTrue("Should be BindingClass", BindingReflections.isBindingClass(TreeNode.class));
        assertFalse("Should not be Notification", BindingReflections.isNotification(TreeNode.class));

        assertNull(mock(TreeChildNode.class).treeParent());

        assertEquals(GroupingFoo.class, BindingReflections.findHierarchicalParent(FooChild.class));

        assertTrue(BindingReflections.isOperationMethod(TestImplementation.class
            .getDeclaredMethod("invoke", TestInput.class, RpcCallback.class)));
        assertEquals(TestImplementation.class, BindingReflections.findAugmentationTarget(TestImplementation.class));

        assertEquals(TestOutput.class, BindingReflections.resolveOperationOutputClass(TestImplementation.class
                    .getDeclaredMethod("invoke", TestInput.class, RpcCallback.class)).get());
        assertEquals(TestInput.class, BindingReflections.resolveOperationInputClass(TestImplementation.class
                .getDeclaredMethod("invoke", TestInput.class, RpcCallback.class)).get());
        assertFalse(BindingReflections.resolveOperationIIClass(TestImplementation.class
                    .getDeclaredMethod("invoke", TestInput.class, RpcCallback.class)).isPresent());
        assertFalse(BindingReflections.resolveOperationKeyedIIClass(TestImplementation.class
                .getDeclaredMethod("invoke", TestInput.class, RpcCallback.class)).isPresent());

        assertTrue(BindingReflections.getQName(TestImplementation.class).toString().equals("test"));
        assertNotNull(BindingReflections.getQNameModule(TestImplementation.class));
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = UnsupportedOperationException.class)
    public void testPrivateConstructor() throws Throwable {
        assertFalse(BindingReflections.class.getDeclaredConstructor().isAccessible());
        final Constructor constructor = BindingReflections.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    private static final class TestInput implements Input<TestInput>, Instantiable<TestInput> {

        @Override
        public Class<? extends TestInput> implementedInterface() {
            return TestInput.class;
        }
    }


    private static final class TestOutput implements Output<TestOutput>, Instantiable<TestOutput> {

        @Override
        public Class<? extends TestOutput> implementedInterface() {
            return TestOutput.class;
        }
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    private static final class TestImplementation extends BaseIdentity
            implements Augmentation<TestImplementation>, Rpc<TestInput, TestOutput> {

        public static final QName QNAME = QName.create("test");

        Future<List<Object>> rpcMethodTest() {
            return null;
        }

        Future rpcMethodTest2() {
            return null;
        }

        @Override
        public void invoke(final TestInput input, final RpcCallback<TestOutput> callback) {
        }
    }
}
