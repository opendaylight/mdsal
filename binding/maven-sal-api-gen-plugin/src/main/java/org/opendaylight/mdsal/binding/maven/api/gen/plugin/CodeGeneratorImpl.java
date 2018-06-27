/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.maven.api.gen.plugin;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.maven.project.MavenProject;
import org.opendaylight.mdsal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile.FileKind;
import org.opendaylight.mdsal.binding.java.api.generator.YangModuleInfoTemplate;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

public final class CodeGeneratorImpl implements BasicCodeGenerator, BuildContextAware, MavenProjectAware {
    public static final String CONFIG_PERSISTENT_SOURCES_DIR = "persistentSourcesDir";
    public static final String CONFIG_IGNORE_DUPLICATE_FILES = "ignoreDuplicateFiles";

    private static final Logger LOG = LoggerFactory.getLogger(CodeGeneratorImpl.class);
    private static final String FS = File.separator;
    private BuildContext buildContext;
    private File projectBaseDir;
    private Map<String, String> additionalConfig;
    private MavenProject mavenProject;
    private File resourceBaseDir;

    @Override
    public Collection<File> generateSources(final SchemaContext context, final File outputDir,
            final Set<Module> yangModules, final Function<Module, Optional<String>> moduleResourcePathResolver)
                    throws IOException {
        final File outputBaseDir;

        outputBaseDir = outputDir == null ? getDefaultOutputBaseDir() : outputDir;

        final List<Type> types = new BindingGeneratorImpl().generateTypes(context, yangModules);
        final GeneratorJavaFile generator = new GeneratorJavaFile(types);

        File persistentSourcesDir = null;
        boolean ignoreDuplicateFiles = false;
        if (additionalConfig != null) {
            String persistenSourcesPath = additionalConfig.get(CONFIG_PERSISTENT_SOURCES_DIR);
            if (persistenSourcesPath != null) {
                persistentSourcesDir = new File(persistenSourcesPath);
            }
            String ignoreDuplicateFilesString = additionalConfig.get(CONFIG_IGNORE_DUPLICATE_FILES);
            if (ignoreDuplicateFilesString != null) {
                ignoreDuplicateFiles = Boolean.parseBoolean(ignoreDuplicateFilesString);
            }
        }
        if (persistentSourcesDir == null) {
            persistentSourcesDir = new File(projectBaseDir, "src" + FS + "main" + FS + "java");
        }

        final Table<FileKind, String, Supplier<String>> generatedFiles = generator.generateFileContent(
            ignoreDuplicateFiles);
        final List<File> result = new ArrayList<>(generatedFiles.size());
        for (Cell<FileKind, String, Supplier<String>> cell : generatedFiles.cellSet()) {
            final File target;
            switch (cell.getRowKey()) {
                case PERSISTENT:
                    target = new File(persistentSourcesDir, cell.getColumnKey());
                    if (target.exists()) {
                        LOG.debug("Skipping existing persistent {}", target);
                        continue;
                    }
                    break;
                case TRANSIENT:
                    target = new File(outputBaseDir, cell.getColumnKey());
                    break;
                default:
                    throw new IllegalStateException("Unsupported file type in " + cell);
            }

            Files.createParentDirs(target);
            try (final OutputStream stream = buildContext.newFileOutputStream(target)) {
                try (final Writer fw = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                    try (final BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write(cell.getValue().get());
                    }
                } catch (IOException e) {
                    LOG.error("Failed to write generate output into {}", target.getPath(), e);
                    throw e;
                }
            }

            result.add(target);
        }

        result.addAll(generateModuleInfos(outputBaseDir, yangModules, context, moduleResourcePathResolver));
        return result;
    }

    private Collection<? extends File> generateModuleInfos(final File outputBaseDir, final Set<Module> yangModules,
            final SchemaContext context, final Function<Module, Optional<String>> moduleResourcePathResolver) {
        Builder<File> result = ImmutableSet.builder();
        Builder<String> bindingProviders = ImmutableSet.builder();
        for (Module module : yangModules) {
            Builder<String> currentProvidersBuilder = ImmutableSet.builder();
            // TODO: do not mutate parameters, output of a method is defined by its return value
            Set<File> moduleInfoProviders = generateYangModuleInfo(outputBaseDir, module, context,
                moduleResourcePathResolver, currentProvidersBuilder);
            ImmutableSet<String> currentProviders = currentProvidersBuilder.build();
            LOG.debug("Adding ModuleInfo providers {}", currentProviders);
            bindingProviders.addAll(currentProviders);
            result.addAll(moduleInfoProviders);
        }

        result.add(writeMetaInfServices(resourceBaseDir, YangModelBindingProvider.class, bindingProviders.build()));
        return result.build();
    }

    private File writeMetaInfServices(final File outputBaseDir, final Class<YangModelBindingProvider> serviceClass,
            final ImmutableSet<String> services) {
        File metainfServicesFolder = new File(outputBaseDir, "META-INF" + File.separator + "services");
        metainfServicesFolder.mkdirs();
        File serviceFile = new File(metainfServicesFolder, serviceClass.getName());

        String src = Joiner.on('\n').join(services);

        return writeFile(serviceFile, src);
    }

    public static final String DEFAULT_OUTPUT_BASE_DIR_PATH = "target" + File.separator + "generated-sources"
            + File.separator + "maven-sal-api-gen";

    private File getDefaultOutputBaseDir() {
        File outputBaseDir;
        outputBaseDir = new File(DEFAULT_OUTPUT_BASE_DIR_PATH);
        setOutputBaseDirAsSourceFolder(outputBaseDir, mavenProject);
        LOG.debug("Adding " + outputBaseDir.getPath() + " as compile source root");
        return outputBaseDir;
    }

    private static void setOutputBaseDirAsSourceFolder(final File outputBaseDir, final MavenProject mavenProject) {
        Preconditions.checkNotNull(mavenProject, "Maven project needs to be set in this phase");
        mavenProject.addCompileSourceRoot(outputBaseDir.getPath());
    }

    @Override
    public void setAdditionalConfig(final Map<String, String> additionalConfiguration) {
        this.additionalConfig = additionalConfiguration;
    }

    @Override
    public void setResourceBaseDir(final File resourceBaseDir) {
        this.resourceBaseDir = resourceBaseDir;
    }

    @Override
    public void setMavenProject(final MavenProject project) {
        this.mavenProject = project;
        this.projectBaseDir = project.getBasedir();
    }

    @Override
    public void setBuildContext(final BuildContext buildContext) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
    }

    private Set<File> generateYangModuleInfo(final File outputBaseDir, final Module module, final SchemaContext ctx,
            final Function<Module, Optional<String>> moduleResourcePathResolver,
            final Builder<String> providerSourceSet) {
        Builder<File> generatedFiles = ImmutableSet.builder();

        final YangModuleInfoTemplate template = new YangModuleInfoTemplate(module, ctx, moduleResourcePathResolver);
        String moduleInfoSource = template.generate();
        if (moduleInfoSource.isEmpty()) {
            throw new IllegalStateException("Generated code should not be empty!");
        }
        String providerSource = template.generateModelProvider();

        final File packageDir = GeneratorJavaFile.packageToDirectory(outputBaseDir,
                BindingGeneratorUtil.moduleNamespaceToPackageName(module));

        generatedFiles.add(writeJavaSource(packageDir, BindingMapping.MODULE_INFO_CLASS_NAME, moduleInfoSource));
        generatedFiles
                .add(writeJavaSource(packageDir, BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME, providerSource));
        providerSourceSet.add(template.getModelBindingProviderName());

        return generatedFiles.build();

    }

    private File writeJavaSource(final File packageDir, final String className, final String source) {
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
        final File file = new File(packageDir, className + ".java");
        writeFile(file, source);
        return file;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private File writeFile(final File file, final String source) {
        try (OutputStream stream = buildContext.newFileOutputStream(file)) {
            try (Writer fw = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(source);
                }
            } catch (Exception e) {
                LOG.error("Could not write file: {}",file,e);
            }
        } catch (Exception e) {
            LOG.error("Could not create file: {}",file,e);
        }
        return file;
    }
}
