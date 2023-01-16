/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Mdsal807Test extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal807");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal807");
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testBitsTypedef() throws IOException, URISyntaxException {
        generateTestSources("/compilation/mdsal807", sourcesOutputDir);
        final var pmDataType = FileSearchUtil.getFiles(sourcesOutputDir).get("TableConfig.java");
        assertNotNull(pmDataType);

        final var content = Files.readString(pmDataType.toPath());
        FileSearchUtil.assertFileContainsConsecutiveLines(pmDataType, content,
            // FIXME: adjust this
            "    public String stringValue() {",
            "        if (_uint64 != null) {",
            "            return _uint64.toCanonicalString();",
            "        }",
            "        if (_int64 != null) {",
            "            return _int64.toString();",
            "        }",
            "        if (_decimal64 != null) {",
            "            return _decimal64.toCanonicalString();",
            "        }",
            "        throw new IllegalStateException(\"No value assigned\");",
            "    }");

        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }
}
