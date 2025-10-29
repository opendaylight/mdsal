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
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMDataReadOnlyTransactionTest {
    @Mock(name = "domDataTreeReadTransaction")
    private DOMDataTreeReadTransaction delegate;

    private ForwardingDOMDataReadOnlyTransaction tx;

    @BeforeEach
    void beforeEach() {
        tx = new ForwardingDOMDataReadOnlyTransaction() {
            @Override
            protected DOMDataTreeReadTransaction delegate() {
                return delegate;
            }
        };
    }

    @Test
    void basicTest() {
        doReturn(null).when(delegate).read(null, null);
        assertNotNull(tx.read(null, null));
        verify(delegate).read(null, null);

        doReturn(null).when(delegate).exists(null, null);
        assertNotNull(tx.exists(null, null));
        verify(delegate).exists(null, null);

        doReturn(null).when(delegate).getIdentifier();
        tx.getIdentifier();
        verify(delegate).getIdentifier();

        doNothing().when(delegate).close();
        tx.close();
        verify(delegate).close();
    }
}