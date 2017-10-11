/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.junit.Assert.assertTrue;

import com.google.common.annotations.Beta;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.util.YangSnippetCleaner;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@Beta
public class YangTemplateTest {

    private Set<Module> modules;

    @Before
    public void setup() {
        this.modules = YangParserTestUtils.parseYangResourceDirectory("/yang-template").getModules();
    }

    @Test
    public void printYangSnippetForModule() throws Exception{
        Iterator<Module> iterator = this.modules.iterator();
        String moduleBody = yangTemplateForModule.render(iterator.next()).body().trim();
        String cleanedModuleBody = YangSnippetCleaner.clean(moduleBody);
        assertTrue(cleanedModuleBody.contains("yang-template-import"));
        assertTrue(cleanedModuleBody.contains("    extension ext;"));
        assertTrue(cleanedModuleBody.contains("    yti:ext;"));
        assertTrue(cleanedModuleBody.contains("    yti:ext-arg \"arg\""));

        moduleBody = yangTemplateForModule.render(iterator.next()).body().trim();
        cleanedModuleBody = YangSnippetCleaner.clean(moduleBody);
        System.out.println(cleanedModuleBody);
        assertTrue(cleanedModuleBody.contains("module yang-template-test {"));
        assertTrue(cleanedModuleBody.contains("    import yang-template-import {"));
        assertTrue(cleanedModuleBody.contains("        prefix \"yti\";"));
        assertTrue(cleanedModuleBody.contains("        revision-date 2016-06-23;"));
        assertTrue(cleanedModuleBody.contains("    }"));
        assertTrue(cleanedModuleBody.contains("    anydata simple-anydata;"));
        assertTrue(cleanedModuleBody.contains("    container simple-container-with-action {"));
        assertTrue(cleanedModuleBody.contains("    leaf-list simple-leaf-list {"));
        assertTrue(cleanedModuleBody.contains("    leaf-list simple-leaf-list-userordered {"));
        assertTrue(cleanedModuleBody.contains("    list simple-list {"));
        assertTrue(cleanedModuleBody.contains("        key \"simple-list-leaf-1\";"));
        assertTrue(cleanedModuleBody.contains("        unique \"simple-list-leaf-2\";"));
        assertTrue(cleanedModuleBody.contains("        action act {"));
        assertTrue(cleanedModuleBody.contains("    list simple-list-more-arg-in-unique {"));
        assertTrue(cleanedModuleBody.contains("        ordered-by user;"));
        assertTrue(cleanedModuleBody.contains("    choice simple-choice {"));
        assertTrue(cleanedModuleBody.contains("    anyxml simple-anyxml;"));
        assertTrue(cleanedModuleBody.contains("        uses simple-grouping {"));
        assertTrue(cleanedModuleBody.contains("            refine simple-grouping-leaf {"));
        assertTrue(cleanedModuleBody.contains("    grouping simple-grouping {"));
        assertTrue(cleanedModuleBody.contains("    augment \"/simple-container-uses\" {"));
        assertTrue(cleanedModuleBody.contains("    extension simple-extension {"));
        assertTrue(cleanedModuleBody.contains("    feature simple-feature {"));
        assertTrue(cleanedModuleBody.contains("    identity simple-identity {"));
        assertTrue(cleanedModuleBody.contains("    notification simple-notification {"));
        assertTrue(cleanedModuleBody.contains("    rpc simple-rpc {"));
    }
}
