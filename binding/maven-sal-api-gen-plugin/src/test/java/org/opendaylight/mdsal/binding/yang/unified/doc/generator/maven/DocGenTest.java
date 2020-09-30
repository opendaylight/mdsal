/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.unified.doc.generator.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import static org.junit.Assert.*;

public class DocGenTest {
    public static final String FS = File.separator;
    private static final String TEST_PATH = "target" + FS + "test" + FS + "site";
    private static final File GENERATOR_OUTPUT_DIR = new File(TEST_PATH);
    private static final File HTML_PATH = new File("src" + FS + "test" + FS + "resources" + FS + "doc-gen-html");
    private static Map<String, File> expectedHtml;

    @Before
    public void init() {
        if (GENERATOR_OUTPUT_DIR.exists()) {
            deleteTestDir(GENERATOR_OUTPUT_DIR);
        }
        assertTrue(GENERATOR_OUTPUT_DIR.mkdirs());
        initExpectedHtml();
    }

//    @After
//    public void cleanUp() {
//        if (GENERATOR_OUTPUT_DIR.exists()) {
//            deleteTestDir(GENERATOR_OUTPUT_DIR);
//        }
//    }

    @Test
    public void testListGeneration() throws Exception {
        final List<File> sourceFiles = getSourceFiles("/doc-gen");
        final EffectiveModelContext context = YangParserTestUtils.parseYangFiles(sourceFiles);
        final DocumentationGeneratorImpl generator = new DocumentationGeneratorImpl();
        generator.setBuildContext(new DefaultBuildContext());
        Collection<File> generatedFiles = generator.generateSources(context, GENERATOR_OUTPUT_DIR,
            Set.copyOf(context.getModules()), (module, representation) -> Optional.empty());
        assertEquals(4, generatedFiles.size());
        for (File generatedFile : generatedFiles) {
            final File file = expectedHtml.get(generatedFile.getName());
            assertNotNull(file);
            final String actual = Files.readString(generatedFile.toPath());
            final String expected = Files.readString(file.toPath());
            assertEquals(actual, expected);
        }
    }

    private static void initExpectedHtml() {
        assertTrue(HTML_PATH.exists());
        final File[] files = HTML_PATH.listFiles();
        assertNotNull(files);
        expectedHtml = Maps.uniqueIndex(Arrays.asList(files), File::getName);
    }

    private static List<File> getSourceFiles(final String path) throws Exception {
        final URI resPath = DocGenTest.class.getResource(path).toURI();
        final File sourcesDir = new File(resPath);
        if (!sourcesDir.exists()) {
            throw new FileNotFoundException("Testing files were not found(" + sourcesDir.getName() + ")");
        }

        final List<File> sourceFiles = new ArrayList<>();
        final File[] fileArray = sourcesDir.listFiles();
        if (fileArray == null) {
            throw new IllegalArgumentException("Unable to locate files in " + sourcesDir);
        }
        sourceFiles.addAll(Arrays.asList(fileArray));
        return sourceFiles;
    }

    private static void deleteTestDir(final File file) {
        if (file.isDirectory()) {
            File[] filesToDelete = file.listFiles();
            if (filesToDelete != null) {
                for (File f : filesToDelete) {
                    deleteTestDir(f);
                }
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Failed to clean up after test");
        }
    }

}
