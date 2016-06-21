/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class SimpleDOMMountPointTest {

    @Test
    public void basicTest() throws Exception {
        final YangInstanceIdentifier yangInstanceIdentifier = mock(YangInstanceIdentifier.class);
        final SchemaContext schemaContext = mock(SchemaContext.class);
        final DOMService domService = mock(DOMService.class);
        final ClassToInstanceMap classToInstanceMap =
                ImmutableClassToInstanceMap.builder().put(DOMService.class, domService).build();

        final SimpleDOMMountPoint simpleDOMMountPoint =
                SimpleDOMMountPoint.create(yangInstanceIdentifier, classToInstanceMap, schemaContext);

        assertNotNull(simpleDOMMountPoint);

        assertEquals(yangInstanceIdentifier, simpleDOMMountPoint.getIdentifier());
        assertEquals(schemaContext, simpleDOMMountPoint.getSchemaContext());

        assertTrue(simpleDOMMountPoint.getService(DOMService.class).isPresent());
        assertEquals(domService, simpleDOMMountPoint.getService(DOMService.class).get());
    }
}