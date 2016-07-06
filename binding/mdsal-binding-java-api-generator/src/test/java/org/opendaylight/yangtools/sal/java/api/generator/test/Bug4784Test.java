package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test to ensure the proper license header is appended to the top of Java generated files.
 */
public class Bug4784Test extends BaseCompilationTest {

    private static final String BUG_ID = "bug4784";
    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = new File(CompilationTestUtils.GENERATOR_OUTPUT_PATH + CompilationTestUtils.FS + BUG_ID);
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(CompilationTestUtils.COMPILER_OUTPUT_PATH + CompilationTestUtils.FS + BUG_ID);
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        generateTestSources(CompilationTestUtils.FS + "compilation" + CompilationTestUtils.FS + BUG_ID, sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final Map<String, File> generatedFiles = getFiles(sourcesOutputDir);
        assertEquals(1, generatedFiles.size());
        final File fooFile = generatedFiles.get("FooData.java");
        final String fileContents = readFile(fooFile);
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        assertTrue(fileContents.startsWith(
                "/*\n" +
                " * Copyright © " + year + " OpenDaylight Project and others.  All rights reserved.\n" +
                " *\n" +
                " * This program and the accompanying materials are made available under the\n" +
                " * terms of the Eclipse Public License v1.0 which accompanies this distribution,\n" +
                " * and is available at http://www.eclipse.org/legal/epl-v10.html\n" +
                " */"
        ));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private static String readFile(final File file) throws FileNotFoundException {
        final StringBuilder s = new StringBuilder();
        final Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            s.append(scanner.nextLine());
            s.append("\n");
        }
        return s.toString();
    }

    private static Map<String, File> getFiles(final File path) {
        return getFiles(path, Maps.newHashMap());
    }

    private static Map<String, File> getFiles(final File path, final Map<String, File> files) {
        final File [] dirFiles = path.listFiles();
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                return getFiles(file, files);
            } else {
                files.put(file.getName(), file);
            }
        }
        return files;
    }

    private void generateTestSources(final String resourceDirPath, final File sourcesOutputDir) throws Exception {
        final List<File> sourceFiles = CompilationTestUtils.getSourceFiles(resourceDirPath);
        final SchemaContext context = TestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);
    }

}
