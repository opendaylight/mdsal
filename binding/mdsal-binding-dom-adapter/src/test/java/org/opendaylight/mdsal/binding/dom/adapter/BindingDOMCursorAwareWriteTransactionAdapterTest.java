/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class BindingDOMCursorAwareWriteTransactionAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final DOMDataTreeCursorAwareTransaction delegate = mock(DOMDataTreeCursorAwareTransaction.class);
        final BindingNormalizedNodeCodecRegistry registry = mock(BindingNormalizedNodeCodecRegistry.class);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), registry);
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.EMPTY;
        doReturn(yangInstanceIdentifier).when(registry).toYangInstanceIdentifier(any());
        doReturn(mock(DOMDataTreeWriteCursor.class)).when(delegate).createCursor(any());

        final BindingDOMCursorAwareWriteTransactionAdapter adapter =
                new BindingDOMCursorAwareWriteTransactionAdapter<>(delegate, codec);

        assertNotNull(adapter.createCursor(DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(DataObject.class))));

        doReturn(null).when(delegate).submit();
        adapter.submit();
        verify(delegate).submit();

        doReturn(true).when(delegate).cancel();
        assertTrue(adapter.cancel());
        verify(delegate).cancel();
    }
}