/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.yangtools.concepts.Registration;

@ExtendWith(MockitoExtension.class)
class AbstractDOMRpcProviderServiceTest extends AbstractDOMRpcProviderService {
    @Mock(name = "domRpcImplementationRegistration")
    private DOMRpcImplementationRegistration<?> domRpcImplementationRegistration;

    @Test
    void registerRpcImplementation() {
        assertEquals(domRpcImplementationRegistration, registerRpcImplementation(mock(DOMRpcImplementation.class)));
    }

    @Override
    public <T extends DOMRpcImplementation> DOMRpcImplementationRegistration<T> registerRpcImplementation(
            final T implementation, final Set<DOMRpcIdentifier> rpcs) {
        return (DOMRpcImplementationRegistration<T>) domRpcImplementationRegistration;
    }

    @Override
    public Registration registerRpcImplementations(final Map<DOMRpcIdentifier, DOMRpcImplementation> map) {
        throw new UnsupportedOperationException();
    }
}