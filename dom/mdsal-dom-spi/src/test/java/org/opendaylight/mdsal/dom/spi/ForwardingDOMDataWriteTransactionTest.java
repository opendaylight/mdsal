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
import static org.mockito.MockitoAnnotations.initMocks;

import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

public class ForwardingDOMDataWriteTransactionTest extends ForwardingDOMDataWriteTransaction {

    @Mock(name = "domDataTreeWriteTransaction")
    private DOMDataTreeWriteTransaction domDataTreeWriteTransaction;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        doReturn(null).when(domDataTreeWriteTransaction).getIdentifier();
        this.getIdentifier();
        verify(domDataTreeWriteTransaction).getIdentifier();

        doNothing().when(domDataTreeWriteTransaction).put(null, null, null);
        this.put(null, null, null);
        verify(domDataTreeWriteTransaction).put(null, null, null);

        doNothing().when(domDataTreeWriteTransaction).merge(null, null, null);
        this.merge(null, null, null);
        verify(domDataTreeWriteTransaction).merge(null, null, null);

        doNothing().when(domDataTreeWriteTransaction).delete(null, null);
        this.delete(null, null);
        verify(domDataTreeWriteTransaction).delete(null, null);

        doReturn(null).when(domDataTreeWriteTransaction).submit();
        this.submit();
        verify(domDataTreeWriteTransaction).submit();

        doReturn(false).when(domDataTreeWriteTransaction).cancel();
        this.cancel();
        verify(domDataTreeWriteTransaction).cancel();
    }

    @Nonnull
    @Override
    protected DOMDataTreeWriteTransaction delegate() {
        return domDataTreeWriteTransaction;
    }
}