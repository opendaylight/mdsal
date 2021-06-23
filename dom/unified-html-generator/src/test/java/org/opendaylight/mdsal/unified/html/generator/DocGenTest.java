/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.unified.html.generator;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Table;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@Deprecated(forRemoval = true)
public class DocGenTest {
    @Test
    public void testListGeneration() throws Exception {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/doc-gen");
        final DocumentationGenerator generator = new DocumentationGenerator();
        Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generatedFiles = generator.generateFiles(context,
            Set.copyOf(context.getModules()), (module, representation) -> Optional.empty());
        assertEquals(4, generatedFiles.size());
    }
}
