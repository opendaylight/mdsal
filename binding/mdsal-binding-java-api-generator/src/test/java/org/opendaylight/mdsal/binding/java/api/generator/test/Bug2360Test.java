/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.compilerOutput;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.generatorOutput;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.testCompilation;

import java.io.File;
import org.junit.Test;

public class Bug2360Test extends BaseCompilationTest {

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = generatorOutput("bug2360test");
        final File compiledOutputDir = compilerOutput("bug2360test");
        generateTestSources(CompilationTestUtils.FS + "compilation" + CompilationTestUtils.FS + "bug2360test",
            sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
