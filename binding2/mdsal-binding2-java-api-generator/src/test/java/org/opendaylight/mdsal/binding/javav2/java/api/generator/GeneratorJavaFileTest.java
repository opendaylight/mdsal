/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.javav2.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

public class GeneratorJavaFileTest {

    @Test
    public void generatedFilesTest() throws Exception {
        final SchemaContext context = YangParserTestUtils.parseYangResourceDirectory("/base/with_import/");
        final BindingGenerator bindingGenerator = new BindingGeneratorImpl(true);
        final List<Type> types = bindingGenerator.generateTypes(context, context.getModules());
        final BuildContext buildContext = new DefaultBuildContext();
        final GeneratorJavaFile gjf = new GeneratorJavaFile(buildContext, types);
        final File persistentSourcesDirectory =
                new File(GeneratorJavaFileTest.class.getResource("/base").getPath());
        final File generatedSourcesDirectory =
                new File(GeneratorJavaFileTest.class.getResource("/base").getPath());
        final List<File> generateToFile = gjf.generateToFile(generatedSourcesDirectory, persistentSourcesDirectory);
        for (final File f : generateToFile) {
            Assert.assertNotNull(f);
        }
        final List<String> files = new ArrayList<>();
        final RuntimeException runtimeException = new RuntimeException();
        for (final File file : generateToFile) {
            BufferedReader br = null;
            FileReader fr = null;
            try {
                fr = new FileReader(file.getAbsolutePath());
                br = new BufferedReader(fr);
                final StringBuilder sb = new StringBuilder();
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    sb.append(currentLine).append('\n');
                }
                files.add(sb.toString());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (fr != null) {
                        fr.close();
                    }
                } catch (final IOException ex) {
                    runtimeException.addSuppressed(ex);
                }
            }
        }

        for (final String s : files) {
            Assert.assertNotNull(s);
            Assert.assertTrue(!s.isEmpty());
        }

        for (final File file2 : generateToFile) {
            file2.delete();
        }
    }
}
