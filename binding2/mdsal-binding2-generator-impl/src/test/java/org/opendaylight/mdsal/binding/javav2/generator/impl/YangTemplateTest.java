/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import com.google.common.annotations.Beta;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.util.YangSnippetCleaner;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@Beta
public class YangTemplateTest {

    private Set<Module> modules;

    @Before
    public void setup() throws URISyntaxException, ReactorException, FileNotFoundException {
        this.modules = YangParserTestUtils.parseYangSources("/yang-template").getModules();
    }

    @Test
    public void printYangSnippetForModule() throws Exception{
        for (final Module module : this.modules) {
            String originalFile;
            try {
                originalFile = readFile(this.getClass().getResourceAsStream(
                        new StringBuilder("/yang-template/").append(module.getName()).append(".yang").toString()));
            } catch (final Exception e) {
                throw e;
            }
            final String moduleBody = yangTemplateForModule.render(module).body().trim();
            final String cleanedModuleBody = YangSnippetCleaner.clean(moduleBody);
            System.out.println(cleanedModuleBody);
            Assert.assertEquals(originalFile, cleanedModuleBody);
        }
    }

    private String readFile(final InputStream inputStream) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try {
            final StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
