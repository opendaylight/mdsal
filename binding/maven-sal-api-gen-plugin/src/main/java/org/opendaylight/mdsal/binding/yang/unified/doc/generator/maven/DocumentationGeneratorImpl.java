/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.unified.doc.generator.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.yang.unified.doc.generator.GeneratorImpl;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;

public class DocumentationGeneratorImpl extends GeneratorImpl implements BasicCodeGenerator {

    @Override
    public Collection<File> generateSources(final SchemaContext arg0, final File arg1, final Set<Module> arg2)
            throws IOException {
        return generate(arg0, arg1, arg2);
    }

    @Override
    public void setAdditionalConfig(final Map<String, String> additionalConfiguration) {
        // no additional config utilized
    }

    @Override
    public void setResourceBaseDir(final File resourceBaseDir) {
        // no resource processing necessary
    }
}
