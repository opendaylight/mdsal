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
import org.opendaylight.yangtools.plugin.generator.api.AbstractFileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;

@MetaInfServices(value = FileGeneratorFactory.class)
public final class JavaFileGeneratorFactory extends AbstractFileGeneratorFactory {
    public JavaFileGeneratorFactory() {
        super(JavaFileGenerator.class.getName());
    }

    @Override
    public JavaFileGenerator newFileGenerator(final Map<String, String> additionalConfiguration) {
        return new JavaFileGenerator(additionalConfiguration);
    }
}
