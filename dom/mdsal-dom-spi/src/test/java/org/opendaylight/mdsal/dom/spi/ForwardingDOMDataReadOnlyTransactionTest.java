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
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;

public class ForwardingDOMDataReadOnlyTransactionTest extends ForwardingDOMDataReadOnlyTransaction {

    @Mock(name = "domDataTreeReadTransaction")
    private DOMDataTreeReadTransaction domDataTreeReadTransaction;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        doReturn(null).when(domDataTreeReadTransaction).read(null, null);
        this.read(null, null);
        verify(domDataTreeReadTransaction).read(null, null);

        doReturn(null).when(domDataTreeReadTransaction).exists(null, null);
        this.exists(null, null);
        verify(domDataTreeReadTransaction).exists(null, null);

        doReturn(null).when(domDataTreeReadTransaction).getIdentifier();
        this.getIdentifier();
        verify(domDataTreeReadTransaction).getIdentifier();

        doNothing().when(domDataTreeReadTransaction).close();
        this.close();
        verify(domDataTreeReadTransaction).close();
    }

    @Nonnull
    @Override
    protected DOMDataTreeReadTransaction delegate() {
        return domDataTreeReadTransaction;
    }
}