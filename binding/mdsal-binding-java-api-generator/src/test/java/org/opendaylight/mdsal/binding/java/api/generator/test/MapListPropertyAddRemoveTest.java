/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.*;

public class MapListPropertyAddRemoveTest {
    private static final Logger LOG = LoggerFactory.getLogger(MapListPropertyAddRemoveTest.class);

    private File sourcesOutputDir;
    private File compiledOutputDir;
    private Map<String, File> files;



    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("list-as-child");
        compiledOutputDir = CompilationTestUtils.compilerOutput("list-as-child");
        files = FileSearchUtil.getFiles(sourcesOutputDir);
    }

    @Test
    public void listAddMethodDeclaration() throws IOException {
        File file = files.get("UnkeyedBuilder.java");
        assertNotNull(Files.readString(Path.of(file.getAbsolutePath())));
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        LOG.info(content);
        assertEquals(content, "");
        assertTrue(content.contains("@Nullable Object getLeaf1();"));
    }

}
