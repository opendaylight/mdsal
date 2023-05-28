/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableClassToInstanceMap;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class SimpleDOMMountPointTest {
    @Test
    public void basicTest() throws Exception {
        final var domService = mock(DOMService.class);
        final var classToInstanceMap = ImmutableClassToInstanceMap.of(DOMService.class, domService);

        final SimpleDOMMountPoint simpleDOMMountPoint =
                SimpleDOMMountPoint.create(YangInstanceIdentifier.of(), classToInstanceMap);
        assertNotNull(simpleDOMMountPoint);

        assertSame(YangInstanceIdentifier.of(), simpleDOMMountPoint.getIdentifier());
        assertTrue(simpleDOMMountPoint.getService(DOMService.class).isPresent());
        assertSame(domService, simpleDOMMountPoint.getService(DOMService.class).orElseThrow());
    }
}