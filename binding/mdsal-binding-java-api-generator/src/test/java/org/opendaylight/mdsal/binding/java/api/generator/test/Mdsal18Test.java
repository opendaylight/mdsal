/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.testCompilation;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

/**
 * Test correct generation of nonnullFoo() methods.
 */
public class Mdsal18Test extends BaseCompilationTest {

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal18");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal18");
        generateTestSources("/compilation/mdsal18", sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> rpcInterface = loader.loadClass("org.opendaylight.yang.gen.v1.mdsal._18.norev.Mdsal18Service");
        for (Method method : rpcInterface.getDeclaredMethods()) {
            final Annotation[] annotations = method.getAnnotations();
            assertEquals(1, annotations.length);
        }

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
