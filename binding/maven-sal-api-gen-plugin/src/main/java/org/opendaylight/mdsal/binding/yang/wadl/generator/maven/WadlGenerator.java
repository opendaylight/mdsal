/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.wadl.generator.maven;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.opendaylight.mdsal.binding.yang.wadl.generator.WadlRestconfGenerator;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.sonatype.plexus.build.incremental.BuildContext;

public class WadlGenerator implements BasicCodeGenerator, BuildContextAware {
    private BuildContext buildContext;

    @Override
    public void setAdditionalConfig(final Map<String, String> additionalConfiguration) {
        // No-op
    }

    @Override
    public void setBuildContext(final BuildContext buildContext) {
        this.buildContext = requireNonNull(buildContext);
    }

    @Override
    public void setResourceBaseDir(final File resourceBaseDir) {
        // No-op
    }

    @Override
    public Collection<File> generateSources(final SchemaContext context, final File outputBaseDir,
            final Set<Module> currentModules, final Function<Module, Optional<String>> moduleResourcePathResolver)
            throws IOException {
        final File outputDir;
        if (outputBaseDir == null) {
            // FIXME: this hard-codes the destination
            outputDir = new File("target" + File.separator + "generated-sources" + File.separator
                    + "maven-sal-api-gen" + File.separator + "wadl");
        } else {
            outputDir = outputBaseDir;
        }

        checkState(buildContext != null, "BuildContext should have been set");
        return new WadlRestconfGenerator(buildContext, outputDir).generate(context, currentModules);
    }
}
