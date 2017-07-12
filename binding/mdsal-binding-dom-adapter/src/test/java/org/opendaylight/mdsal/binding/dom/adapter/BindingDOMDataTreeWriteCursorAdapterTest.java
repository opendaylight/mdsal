/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Deque;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingDOMDataTreeWriteCursorAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final DataTreeIdentifier<?> identifier =
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                        InstanceIdentifier.create(DataObject.class));
        final DOMDataTreeWriteCursor delegate = mock(DOMDataTreeWriteCursor.class);
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), registry);
        final BindingDOMDataTreeWriteCursorAdapter<?> adapter =
                new BindingDOMDataTreeWriteCursorAdapter<>(identifier, delegate, codec);

        final PathArgument pathArgument = new Item<>(DataObject.class);
        final DataObject data = mock(DataObject.class);

        adapter.enter(pathArgument, pathArgument);
        adapter.enter(ImmutableList.of(pathArgument));

        doNothing().when(delegate).write(any(), any());
        doNothing().when(delegate).merge(any(), any());
        doNothing().when(delegate).delete(any());
        doReturn(YangInstanceIdentifier.EMPTY).when(registry).toYangInstanceIdentifier(any());
        doNothing().when(delegate).close();
        final NormalizedNode<?, ?> normalizedNode = mock(NormalizedNode.class);

        doReturn(new SimpleEntry<YangInstanceIdentifier,NormalizedNode<?,?>>(YangInstanceIdentifier.EMPTY,
                normalizedNode)).when(registry).toNormalizedNode(any(), any());
        adapter.write(pathArgument, data);
        verify(delegate).write(any(), any());

        adapter.merge(pathArgument, data);
        verify(delegate).merge(any(), any());

        adapter.delete(pathArgument);
        verify(delegate).delete(any());

        final Deque<PathArgument> stack = adapter.stack();
        assertTrue(stack.contains(pathArgument));

        adapter.exit(stack.size());
        assertFalse(stack.contains(pathArgument));

        adapter.close();
        verify(delegate).close();
    }
}