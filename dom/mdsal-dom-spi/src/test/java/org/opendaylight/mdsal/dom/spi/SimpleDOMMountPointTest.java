/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.collect.ImmutableClassToInstanceMap;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@ExtendWith(MockitoExtension.class)
class SimpleDOMMountPointTest {
    private interface MockService extends DOMService<MockService, MockService.Extension> {
        interface Extension extends DOMService.Extension<MockService, Extension> {
            // Marker
        }
    }

    @Mock
    private MockService service;

    @Test
    void basicTest() {
        final var impl = SimpleDOMMountPoint.create(YangInstanceIdentifier.of(),
            ImmutableClassToInstanceMap.of(MockService.class, service));
        assertNotNull(impl);

        assertSame(YangInstanceIdentifier.of(), impl.getIdentifier());
        assertEquals(Optional.of(service), impl.getService(MockService.class));
    }
}