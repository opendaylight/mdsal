/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class MockingUtilities {

    private MockingUtilities() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> T mock(Class<T> type, String toString) {
        T mock = Mockito.mock(type);
        Mockito.doReturn(toString).when(mock).toString();
        return mock;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, F extends T> ArgumentCaptor<F> captorFor(Class<T> rawClass) {
        return (ArgumentCaptor) ArgumentCaptor.forClass(rawClass);
    }
}
