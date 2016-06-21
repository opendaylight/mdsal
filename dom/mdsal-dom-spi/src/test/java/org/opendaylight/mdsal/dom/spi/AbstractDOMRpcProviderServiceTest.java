/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Set;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;

public class AbstractDOMRpcProviderServiceTest extends AbstractDOMRpcProviderService {

    @Mock(name = "domRpcImplementationRegistration")
    private DOMRpcImplementationRegistration domRpcImplementationRegistration;

    @Test
    public void registerRpcImplementation() throws Exception {
        initMocks(this);
        assertEquals(domRpcImplementationRegistration, this.registerRpcImplementation(null));
    }

    @Nonnull
    @Override
    public <T extends DOMRpcImplementation> DOMRpcImplementationRegistration<T> registerRpcImplementation
            (@Nonnull T implementation, @Nonnull Set<DOMRpcIdentifier> rpcs) {
        return domRpcImplementationRegistration;
    }
}