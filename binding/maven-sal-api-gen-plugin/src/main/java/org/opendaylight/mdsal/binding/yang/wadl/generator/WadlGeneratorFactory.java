/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.wadl.generator;

import java.util.Map;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.plugin.generator.api.AbstractFileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;

@MetaInfServices(value = FileGeneratorFactory.class)
public final class WadlGeneratorFactory extends AbstractFileGeneratorFactory {
    public WadlGeneratorFactory() {
        super(WadlGenerator.class.getName());
    }

    @Override
    public FileGenerator newFileGenerator(final Map<String, String> configuration) {
        return new WadlGenerator();
    }
}
