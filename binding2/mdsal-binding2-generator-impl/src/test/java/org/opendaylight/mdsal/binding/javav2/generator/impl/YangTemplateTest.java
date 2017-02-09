/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import com.google.common.annotations.Beta;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNode;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNodes;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@Beta
public class YangTemplateTest {

    private Set<Module> modules;

    @Before
    public void setup() throws URISyntaxException, ReactorException, FileNotFoundException {
        modules = YangParserTestUtils.parseYangSources("/yang-template").getModules();
    }

    @Test
    public void printYangSnippetForModule() {
        for (Module module : modules) {
            /**
             * We should be able to call Scala render method from Binding Generator implementation
             * for 3 different inputs:
             * 1. single SchemaNode
             * 2. set of SchemaNode
             * 3. whole Module
             */
            final String moduleBody = yangTemplateForModule.render(module).body();
            //FIXME: don't do it this way, only for very first attempt to show results
            System.out.println("module ".concat(module.getName()).concat(":").concat(moduleBody));

            //TODO: finish following sections
            for (DataSchemaNode dataSchemaNode : module.getChildNodes()) {
                final String nodeBody = yangTemplateForNode.render(dataSchemaNode).body();
            }

            final String rpcsBody = yangTemplateForNodes.render(module.getRpcs()).body();
            final String notificationsBody = yangTemplateForNodes.render(module.getNotifications()).body();

            //////////
        }

    }
}
