/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.maven.api.gen.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.sonatype.plexus.build.incremental.BuildContext;

public final class CodeGeneratorImpl implements BasicCodeGenerator, BuildContextAware, MavenProjectAware {

    @Override
    public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules) throws IOException {
        //TODO implement
        return null;
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
        //TODO implement
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
        //TODO implement
    }

    @Override
    public void setBuildContext(BuildContext buildContext) {
        //TODO implement
    }

    @Override
    public void setMavenProject(MavenProject project) {
        //TODO implement
    }
}
