/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.maven.api.gen.plugin;

import java.util.Map;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.maven.spi.generator.FileGenerator;
import org.opendaylight.yangtools.yang.maven.spi.generator.FileGeneratorFactory;

@MetaInfServices
public class JavaFileGeneratorFactory implements FileGeneratorFactory {
    @Override
    public FileGenerator newFileGenerator(final Map<String, String> additionalConfiguration) {
        return new JavaFileGenerator(additionalConfiguration);
    }
}
