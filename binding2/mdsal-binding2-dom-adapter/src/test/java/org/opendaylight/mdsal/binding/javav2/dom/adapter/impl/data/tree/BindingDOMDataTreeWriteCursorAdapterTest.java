/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.Deque;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingDOMDataTreeWriteCursorAdapterTest {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void basicTest() throws Exception {
        final DataTreeIdentifier identifier =
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                        InstanceIdentifier.create(TreeNode.class));
        final DOMDataTreeWriteCursor delegate = mock(DOMDataTreeWriteCursor.class);
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(
                        (GeneratedClassLoadingStrategy) GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                        registry);
        final BindingDOMDataTreeWriteCursorAdapter adapter =
                new BindingDOMDataTreeWriteCursorAdapter<>(identifier, delegate, codec);

        final TreeArgument<?> pathArgument = new Item<>(TreeNode.class);
        final TreeNode data = mock(TreeNode.class);

        adapter.enter(pathArgument, pathArgument);
        adapter.enter(ImmutableList.of(pathArgument));

        doNothing().when(delegate).write(any(), any());
        doNothing().when(delegate).merge(any(), any());
        doNothing().when(delegate).delete(any());
        doReturn(YangInstanceIdentifier.EMPTY).when(registry).toYangInstanceIdentifier(any());
        doNothing().when(delegate).close();
        final NormalizedNode normalizedNode = mock(NormalizedNode.class);

        doReturn(new SimpleEntry<YangInstanceIdentifier,NormalizedNode<?,?>>(YangInstanceIdentifier.EMPTY,
                normalizedNode)).when(registry).toNormalizedNode(any(), any());
        adapter.write(pathArgument, data);
        verify(delegate).write(any(), any());

        adapter.merge(pathArgument, data);
        verify(delegate).merge(any(), any());

        adapter.delete(pathArgument);
        verify(delegate).delete(any());

        final Field stackField = BindingDOMDataTreeWriteCursorAdapter.class.getDeclaredField("stack");
        stackField.setAccessible(true);
        final Deque stack = (Deque) stackField.get(adapter);
        assertTrue(stack.contains(pathArgument));

        adapter.exit(stack.size());
        assertFalse(stack.contains(pathArgument));

        adapter.close();
        verify(delegate).close();
    }
}