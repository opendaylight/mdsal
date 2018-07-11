/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.io.IOUtil;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class ScanningSchemaServiceProviderTest {

    private ArrayList<URL> yangs;
    private ScanningSchemaServiceProvider schemaService;

    @Before
    public void setup() {
        yangs = new ArrayList<>();
        addYang("/odl-datastore-test.yang");

        schemaService = new ScanningSchemaServiceProvider();
        assertNotNull(schemaService);
        addYangs(schemaService);
    }

    @After
    public void close() {
        schemaService.close();
    }

    private void addYang(final String yang) {
        yangs.add(ScanningSchemaServiceProvider.class.getResource(yang));
    }

    @Test
    public void initJarScanningSchemaServiceTest() throws Exception {
        assertNotNull(schemaService.getGlobalContext());
        assertNotNull(schemaService.getSchemaContext());
        assertEquals(schemaService.getGlobalContext(), schemaService.getSchemaContext());
    }

    @Test
    public void listenersTests() {
        assertFalse(schemaService.hasListeners());

        final SchemaContextHolder actualSchemaCtx = new SchemaContextHolder();
        final SchemaContextListener listener = actualSchemaCtx::setSchemaContext;
        final ListenerRegistration<SchemaContextListener> registerSchemaContextListener =
                schemaService.registerSchemaContextListener(listener);
        assertEquals(registerSchemaContextListener.getInstance(), listener);
        assertEquals(schemaService.getSchemaContext(), actualSchemaCtx.getSchemaContext());
    }

    @Test
    public void notifyListenersTest() {
        final SchemaContext baseSchemaCtx = schemaService.getGlobalContext();
        assertNotNull(baseSchemaCtx);
        assertTrue(baseSchemaCtx.getModules().size() == 1);

        final SchemaContextHolder actualSchemaCtx = new SchemaContextHolder();

        final SchemaContextListener schemaCtxListener = actualSchemaCtx::setSchemaContext;
        final ListenerRegistration<SchemaContextListener> registerSchemaContextListener =
                schemaService.registerSchemaContextListener(schemaCtxListener);
        assertEquals(registerSchemaContextListener.getInstance(), schemaCtxListener);
        assertNotNull(actualSchemaCtx.getSchemaContext());
        assertEquals(baseSchemaCtx, actualSchemaCtx.getSchemaContext());

        addYang("/empty-test1.yang");
        addYangs(schemaService);

        final SchemaContext nextSchemaCtx = schemaService.getGlobalContext();
        assertNotNull(nextSchemaCtx);
        assertTrue(nextSchemaCtx.getModules().size() == 2);

        assertNotEquals(baseSchemaCtx, nextSchemaCtx);

        schemaService.notifyListeners(nextSchemaCtx);
        assertEquals(nextSchemaCtx, actualSchemaCtx.getSchemaContext());

        addYang("/empty-test2.yang");
        addYangs(schemaService);

        final SchemaContext unregistredListenerSchemaCtx = schemaService.getGlobalContext();
        assertNotNull(unregistredListenerSchemaCtx);
        assertTrue(unregistredListenerSchemaCtx.getModules().size() == 3);

        assertNotEquals(baseSchemaCtx, unregistredListenerSchemaCtx);
        assertNotEquals(nextSchemaCtx, unregistredListenerSchemaCtx);

        schemaService.removeListener(schemaCtxListener);
        schemaService.notifyListeners(unregistredListenerSchemaCtx);

        assertNotEquals(unregistredListenerSchemaCtx, actualSchemaCtx.getSchemaContext());
        assertEquals(nextSchemaCtx, actualSchemaCtx.getSchemaContext());

        schemaService.registerSchemaContextListener(schemaCtxListener);
        assertEquals(unregistredListenerSchemaCtx, actualSchemaCtx.getSchemaContext());
    }

    @Test
    public void tryToUpdateSchemaCtxTest() {
        final SchemaContext baseSchemaContext = schemaService.getSchemaContext();
        assertNotNull(baseSchemaContext);
        assertTrue(baseSchemaContext.getModules().size() == 1);

        final SchemaContextHolder actualSchemaCtx = new SchemaContextHolder();
        schemaService.registerSchemaContextListener(actualSchemaCtx::setSchemaContext);

        assertEquals(baseSchemaContext, actualSchemaCtx.getSchemaContext());

        addYang("/empty-test1.yang");
        addYangs(schemaService);

        final SchemaContext nextSchemaContext = schemaService.getSchemaContext();
        assertNotNull(baseSchemaContext);
        assertTrue(baseSchemaContext.getModules().size() == 1);

        assertNotEquals(baseSchemaContext, nextSchemaContext);

        schemaService.tryToUpdateSchemaContext();
        assertEquals(nextSchemaContext, actualSchemaCtx.getSchemaContext());
    }

    @Test
    public void getSourceTest() throws Exception {
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("odl-datastore-test",
            Revision.of("2014-03-13"));
        final YangTextSchemaSource yangTextSchemaSource = schemaService.getSource(sourceIdentifier).get();
        final Collection<String> lines = IOUtil.readLines(yangTextSchemaSource.openStream());
        assertEquals("module odl-datastore-test {", lines.iterator().next());
    }

    @Test
    public void getSupportedExtensionsTest() {
        assertEquals(schemaService.getExtensions().values().iterator().next(), schemaService);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSessionContextTest() {
        schemaService.getSessionContext();
    }

    private void addYangs(final ScanningSchemaServiceProvider service) {
        final List<Registration> registerAvailableYangs = service.registerAvailableYangs(yangs);
        assertTrue(!registerAvailableYangs.isEmpty());
    }

    private class SchemaContextHolder {

        private SchemaContext schemaCtx;

        public void setSchemaContext(final SchemaContext ctx) {
            schemaCtx = ctx;
        }

        public SchemaContext getSchemaContext() {
            return schemaCtx;
        }
    }
}
