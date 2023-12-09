/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;

@ExtendWith(MockitoExtension.class)
class ActiveServiceGroupExceptionTest extends AbstractTest {
    @Mock
    DOMEntityOwnershipService mockEosService;

    /**
     * Test NULL ServiceIdent input for new ServiceGroup instance.
     */
    @Test
    void instantiationClusterSingletonServiceGroupNullIdentTest() {
        assertThrows(NullPointerException.class,
            () -> new ActiveServiceGroup(null, MAIN_ENTITY, CLOSE_ENTITY, mockEosService));
    }

    /**
     * Test empty ServiceIdent input for new ServiceGroup instance.
     */
    @Test
    void instantiationClusterSingletonServiceGroupEmptyIdentTest() {
        assertThrows(IllegalArgumentException.class,
            () -> new ActiveServiceGroup("", MAIN_ENTITY, CLOSE_ENTITY, mockEosService));
    }

    /**
     * Test NULL MainEntity input for new ServiceGroup instance.
     */
    @Test
    void instantiationClusterSingletonServiceGroupNullMainEntityTest() {
        assertThrows(NullPointerException.class,
            () -> new ActiveServiceGroup(SERVICE_NAME, null, CLOSE_ENTITY, mockEosService));
    }

    /**
     * Test NULL CloseEntity input for new ServiceGroup instance.
     */
    @Test
    void instantiationClusterSingletonServiceGroupNullCloseEntityTest() {
        assertThrows(NullPointerException.class,
            () -> new ActiveServiceGroup(SERVICE_NAME, MAIN_ENTITY, null, mockEosService));
    }

    /**
     * Test NULL EntityOwnershipService input for new ServiceGroup instance.
     */
    @Test
    void instantiationClusterSingletonServiceGroupNullEOS_Test() {
        assertThrows(NullPointerException.class,
            () -> new ActiveServiceGroup(SERVICE_NAME, MAIN_ENTITY, CLOSE_ENTITY, null));
    }


}
