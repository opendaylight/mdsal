/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

@ExtendWith(MockitoExtension.class)
public class ForwardingDOMDataWriteTransactionTest extends ForwardingDOMDataWriteTransaction {
    @Mock(name = "domDataTreeWriteTransaction")
    private DOMDataTreeWriteTransaction domDataTreeWriteTransaction;

    @Test
    void basicTest() {
        doReturn(null).when(domDataTreeWriteTransaction).getIdentifier();
        getIdentifier();
        verify(domDataTreeWriteTransaction).getIdentifier();

        doNothing().when(domDataTreeWriteTransaction).put(null, null, null);
        put(null, null, null);
        verify(domDataTreeWriteTransaction).put(null, null, null);

        doNothing().when(domDataTreeWriteTransaction).merge(null, null, null);
        merge(null, null, null);
        verify(domDataTreeWriteTransaction).merge(null, null, null);

        doNothing().when(domDataTreeWriteTransaction).delete(null, null);
        delete(null, null);
        verify(domDataTreeWriteTransaction).delete(null, null);

        doReturn(null).when(domDataTreeWriteTransaction).commit();
        commit();
        verify(domDataTreeWriteTransaction).commit();

        doReturn(false).when(domDataTreeWriteTransaction).cancel();
        cancel();
        verify(domDataTreeWriteTransaction).cancel();
    }

    @Override
    protected DOMDataTreeWriteTransaction delegate() {
        return domDataTreeWriteTransaction;
    }
}
