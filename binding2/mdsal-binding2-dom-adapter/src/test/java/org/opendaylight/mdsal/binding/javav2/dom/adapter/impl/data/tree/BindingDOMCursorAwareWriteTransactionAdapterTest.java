/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeWriteCursor;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class BindingDOMCursorAwareWriteTransactionAdapterTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void basicTest() throws Exception {
        final DOMDataTreeCursorAwareTransaction delegate = mock(DOMDataTreeCursorAwareTransaction.class);
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(
                        (GeneratedClassLoadingStrategy) GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                        registry);
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.EMPTY;
        doReturn(yangInstanceIdentifier).when(registry).toYangInstanceIdentifier(any());

        final BindingDOMCursorAwareWriteTransactionAdapter adapter =
                new BindingDOMCursorAwareWriteTransactionAdapter<>(delegate, codec);

        final DataTreeIdentifier<TreeNode> dti =
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TreeNode.class));

        final DOMDataTreeWriteCursor dtwc = mock(DOMDataTreeWriteCursor.class);
        doReturn(dtwc).when(delegate)
                .createCursor(new DOMDataTreeIdentifier(dti.getDatastoreType(), yangInstanceIdentifier));
        final DataTreeWriteCursor cursor = adapter.createCursor(dti);
        assertNotNull(cursor);

        doReturn(null).when(delegate).commit();
        final BiConsumer callback = mock(BiConsumer.class);
        adapter.submit(callback);
        verify(delegate).commit();

        doReturn(true).when(delegate).cancel();
        assertTrue(adapter.cancel());
        verify(delegate).cancel();
    }
}
