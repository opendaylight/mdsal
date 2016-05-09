/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.maven.api.gen.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.mdsal.binding2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding2.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

public final class CodeGeneratorImpl implements BasicCodeGenerator, BuildContextAware, MavenProjectAware {
    private static final Logger LOG = LoggerFactory.getLogger(CodeGeneratorImpl.class);
    private static final String FS = File.separator;
    private BuildContext buildContext;
    private File projectBaseDir;
    private Map<String, String> additionalConfig;
    private MavenProject mavenProject;
    private File resourceBaseDir;

    @Override
    public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> yangModules) throws IOException {
        outputBaseDir = outputBaseDir == null ? getDefaultOutputBaseDir() : outputBaseDir;

        final BindingGenerator bindingGenerator = new BindingGeneratorImpl(true);
        final List<Type> types = bindingGenerator.generateTypes(context, yangModules);
        final GeneratorJavaFile generator = new GeneratorJavaFile(buildContext, types);

        File persistentSourcesDir = null;
        if (additionalConfig != null) {
            String persistenSourcesPath = additionalConfig.get("persistentSourcesDir");
            if (persistenSourcesPath != null) {
                persistentSourcesDir = new File(persistenSourcesPath);
            }
        }
        if (persistentSourcesDir == null) {
            persistentSourcesDir = new File(projectBaseDir, "src" + FS + "main" + FS + "java");
        }

        List<File> result = generator.generateToFile(outputBaseDir, persistentSourcesDir);

        result.addAll(generateModuleInfos(outputBaseDir, yangModules, context));
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

    private File getDefaultOutputBaseDir() {
        File outputBaseDir;
        outputBaseDir = new File(DEFAULT_OUTPUT_BASE_DIR_PATH);
        setOutputBaseDirAsSourceFolder(outputBaseDir, mavenProject);
        LOG.debug("Adding " + outputBaseDir.getPath() + " as compile source root");
        return outputBaseDir;
    }

    private static final String DEFAULT_OUTPUT_BASE_DIR_PATH = "target" + File.separator + "generated-sources"
            + File.separator + "maven-binding2-sal-api-gen";

    private static void setOutputBaseDirAsSourceFolder(final File outputBaseDir, final MavenProject mavenProject) {
        Preconditions.checkNotNull(mavenProject, "Maven project needs to be set in this phase");
        mavenProject.addCompileSourceRoot(outputBaseDir.getPath());
    }

    private Collection<? extends File> generateModuleInfos(final File outputBaseDir, final Set<Module> yangModules,
                                                           final SchemaContext context) {
        Builder<File> result = ImmutableSet.builder();
//        Builder<String> bindingProviders = ImmutableSet.builder();
//        for (Module module : yangModules) {
//            Builder<String> currentProvidersBuilder = ImmutableSet.builder();
//            // TODO: do not mutate parameters, output of a method is defined by its return value
//            Set<File> moduleInfoProviders = generateYangModuleInfo(outputBaseDir, module, context, currentProvidersBuilder);
//            ImmutableSet<String> currentProviders = currentProvidersBuilder.build();
//            LOG.info("Adding ModuleInfo providers {}", currentProviders);
//            bindingProviders.addAll(currentProviders);
//            result.addAll(moduleInfoProviders);
//        }
//
//        result.add(writeMetaInfServices(resourceBaseDir, YangModelBindingProvider.class, bindingProviders.build()));
        return result.build();
    }

}
