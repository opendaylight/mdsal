/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.maven.api.gen.plugin;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

@Beta
public final class CodeGeneratorImpl implements BasicCodeGenerator, BuildContextAware, MavenProjectAware {

    private static final Logger LOG = LoggerFactory.getLogger(CodeGeneratorImpl.class);
    private BuildContext buildContext;
    private File projectBaseDir;
    private Map<String, String> additionalConfig;
    private MavenProject mavenProject;
    private File resourceBaseDir;

    //TODO: bind this all together with BindingGeneratorImpl and GeneratorJavaFile

    /**
     * This method is called from mojo (maven goal)
     *
     * @param context
     *            parsed from YANG files
     * @param outputBaseDir
     *            expected output directory for generated sources configured by
     *            user
     * @param currentModules
     *            YANG modules parsed from yangFilesRootDir
     * @return
     * @throws IOException
     */
    @Override
    public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules) throws IOException {

        List<File> result = new ArrayList<>();
        return result;
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
        this.additionalConfig = additionalConfiguration;
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
        this.resourceBaseDir = resourceBaseDir;
    }

    @Override
    public void setBuildContext(BuildContext buildContext) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
    }

    @Override
    public void setMavenProject(MavenProject project) {
        this.mavenProject = project;
        this.projectBaseDir = project.getBasedir();
    }
}
