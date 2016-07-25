/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.RpcError;

public class ReadFailedExceptionTest {

    @Test(expected = ReadFailedException.class)
    public void readFailedExceptionTest() throws Exception {
        throw new ReadFailedException("test", mock(RpcError.class));
    }

    @Test(expected = ReadFailedException.class)
    public void readFailedExceptionWithThrowableTest() throws Exception {
        throw new ReadFailedException("test", ReadFailedException.MAPPER.apply(
                new NullPointerException()).getCause(), mock(RpcError.class));
    }
}