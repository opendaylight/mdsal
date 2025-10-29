/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMDataWriteTransactionTest {
    @Mock(name = "domDataTreeWriteTransaction")
    private DOMDataTreeWriteTransaction delegate;

    private ForwardingDOMDataWriteTransaction tx;

    @BeforeEach
    void beforeEach() {
        tx = new ForwardingDOMDataWriteTransaction() {
            @Override
            protected DOMDataTreeWriteTransaction delegate() {
                return delegate;
            }
        };
    }

    @Test
    void basicTest() {
        doReturn(null).when(delegate).getIdentifier();
        tx.getIdentifier();
        verify(delegate).getIdentifier();

        doNothing().when(delegate).put(null, null, null);
        tx.put(null, null, null);
        verify(delegate).put(null, null, null);

        doNothing().when(delegate).merge(null, null, null);
        tx.merge(null, null, null);
        verify(delegate).merge(null, null, null);

        doNothing().when(delegate).delete(null, null);
        tx.delete(null, null);
        verify(delegate).delete(null, null);

        doReturn(null).when(delegate).commit();
        assertNotNull(tx.commit());
        verify(delegate).commit();

        doReturn(false).when(delegate).cancel();
        tx.cancel();
        verify(delegate).cancel();
    }
}
