/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class JarScanningSchemaServiceTest {

    private static ArrayList<URL> yangs;

    @BeforeClass
    public static void setup() {
        yangs = new ArrayList<>();
        yangs.add(JarScanningSchemaService.class.getResource("/odl-datastore-test.yang"));
    }

    @Test
    public void test() throws Exception {
        final JarScanningSchemaService service = new JarScanningSchemaService();
        service.start(yangs);
        final SchemaContext globalContext = service.getGlobalContext();
        assertNotNull(globalContext);
        service.close();
    }

    @Test
    public void test2() {
        final ServiceLoader<JarScanningSchemaService> service = ServiceLoader.load(JarScanningSchemaService.class);
        final Iterator<JarScanningSchemaService> iterator = service.iterator();
        final JarScanningSchemaService schemaService = iterator.next();
        assertNotNull(schemaService);

        schemaService.start(yangs);

        assertNotNull(schemaService.getGlobalContext());
    }
}
