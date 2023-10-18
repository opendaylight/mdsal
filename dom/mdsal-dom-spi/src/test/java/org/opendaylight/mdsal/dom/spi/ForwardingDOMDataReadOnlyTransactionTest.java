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
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;

@ExtendWith(MockitoExtension.class)
public class ForwardingDOMDataReadOnlyTransactionTest extends ForwardingDOMDataReadOnlyTransaction {
    @Mock(name = "domDataTreeReadTransaction")
    private DOMDataTreeReadTransaction domDataTreeReadTransaction;

    @Test
    void basicTest() {
        doReturn(null).when(domDataTreeReadTransaction).read(null, null);
        read(null, null);
        verify(domDataTreeReadTransaction).read(null, null);

        doReturn(null).when(domDataTreeReadTransaction).exists(null, null);
        exists(null, null);
        verify(domDataTreeReadTransaction).exists(null, null);

        doReturn(null).when(domDataTreeReadTransaction).getIdentifier();
        getIdentifier();
        verify(domDataTreeReadTransaction).getIdentifier();

        doNothing().when(domDataTreeReadTransaction).close();
        close();
        verify(domDataTreeReadTransaction).close();
    }

    @Override
    protected DOMDataTreeReadTransaction delegate() {
        return domDataTreeReadTransaction;
    }
}