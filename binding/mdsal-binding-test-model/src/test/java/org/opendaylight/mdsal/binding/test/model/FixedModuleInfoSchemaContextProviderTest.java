/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.mockito.internal.util.io.IOUtil;
import org.opendaylight.mdsal.binding.generator.impl.FixedModuleInfoSchemaContextProvider;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * Unit Test for {@link FixedModuleInfoSchemaContextProvider}.
 *
 * @author Michael Vorburger.ch
 */
public class FixedModuleInfoSchemaContextProviderTest {

    final int numberOfTestModules = 36;

    FixedModuleInfoSchemaContextProvider schemaContextProvider = new FixedModuleInfoSchemaContextProvider();

    @Test
    public void testNonEmptyModuleInfos() {
        assertEquals(numberOfTestModules, schemaContextProvider.getModuleInfos().size());
    }

    @Test
    public void testEmptySchemaContext() {
        assertNotNull(schemaContextProvider.getSchemaContext());
        assertEquals(numberOfTestModules, schemaContextProvider.getSchemaContext().getModules().size());
    }

    @Test
    public void testGetSource() throws InterruptedException, ExecutionException, IOException {
        SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", Revision.of("2017-01-01"));
        YangTextSchemaSource yangTextSchemaSource = schemaContextProvider.getSource(sourceIdentifier).get();
        Collection<String> lines = IOUtil.readLines(yangTextSchemaSource.openStream());
        assertEquals("module test{", lines.iterator().next());
    }
}
