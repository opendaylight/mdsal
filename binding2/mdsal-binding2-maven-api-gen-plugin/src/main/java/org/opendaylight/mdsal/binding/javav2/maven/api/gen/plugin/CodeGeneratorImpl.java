/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.maven.api.gen.plugin;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer.normalizeFullPackageName;
import static org.opendaylight.mdsal.binding.javav2.util.BindingMapping.getRootPackageName;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.maven.project.MavenProject;
import org.opendaylight.mdsal.binding.javav2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.javav2.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers.YangModuleInfoTemplateRenderer;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModelBindingProvider;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
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
    private static final String FS = File.separator;
    private BuildContext buildContext;
    private File projectBaseDir;
    private Map<String, String> additionalConfig;
    private MavenProject mavenProject;
    private File resourceBaseDir;

    /**
     * This method is called from mojo (maven goal)
     * @param context
     *            parsed from YANG files
     * @param outputBaseDir
     *            expected output directory for generated sources configured by
     *            user
     * @param currentModules
     *            YANG modules parsed from yangFilesRootDir
     * @param moduleResourcePathResolver
     *            Function converting a local module to the packaged resource path
     * @return
     * @throws IOException
     */
    @Override
    public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules, Function<Module, Optional<String>> moduleResourcePathResolver) throws IOException {
        final BindingGenerator bindingGenerator = new BindingGeneratorImpl(true);
        final List<Type> types = bindingGenerator.generateTypes(context, currentModules);
        final GeneratorJavaFile generator = new GeneratorJavaFile(buildContext, types);

        File persistentSourcesDir = null;
        if (additionalConfig != null) {
            String persistentSourcesPath = additionalConfig.get("persistentSourcesDir");
            if (persistentSourcesPath != null) {
                persistentSourcesDir = new File(persistentSourcesPath);
            }
        }
        if (persistentSourcesDir == null) {
            persistentSourcesDir = new File(projectBaseDir, "src" + FS + "main" + FS + "java");
        }

        List<File> result = generator.generateToFile(outputBaseDir, persistentSourcesDir);

        result.addAll(generateModuleInfos(outputBaseDir, currentModules, context, moduleResourcePathResolver));
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
        this.buildContext = requireNonNull(buildContext);
    }

    @Override
    public void setMavenProject(MavenProject project) {
        this.mavenProject = project;
        this.projectBaseDir = project.getBasedir();
    }

    private Collection<? extends File> generateModuleInfos(final File outputBaseDir, final Set<Module> yangModules,
                                                           final SchemaContext context, final Function<Module, Optional<String>> moduleResourcePathResolver) {
        Builder<File> result = ImmutableSet.builder();
        Builder<String> bindingProviders = ImmutableSet.builder();
        for (Module module : yangModules) {
            Builder<String> currentProvidersBuilder = ImmutableSet.builder();
            Set<File> moduleInfoProviders = generateYangModuleInfo(outputBaseDir, module, context,
                    moduleResourcePathResolver, currentProvidersBuilder);
            ImmutableSet<String> currentProviders = currentProvidersBuilder.build();
            LOG.info("Adding ModuleInfo providers {}", currentProviders);
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

    private Set<File> generateYangModuleInfo(final File outputBaseDir, final Module module, final SchemaContext ctx,
           final Function<Module, Optional<String>> moduleResourcePathResolver, final Builder<String> providerSourceSet) {

        Builder<File> generatedFiles = ImmutableSet.builder();
        final YangModuleInfoTemplateRenderer template = new YangModuleInfoTemplateRenderer(module, ctx,
                moduleResourcePathResolver);
        String moduleInfoSource = template.generateTemplate();
        if (moduleInfoSource.isEmpty()) {
            throw new IllegalStateException("Generated code should not be empty!");
        }
        String providerSource = template.generateModelProvider();

        final File packageDir = GeneratorJavaFile.packageToDirectory(outputBaseDir,
                normalizeFullPackageName(getRootPackageName(module)));

        generatedFiles.add(writeJavaSource(packageDir, BindingMapping.MODULE_INFO_CLASS_NAME, moduleInfoSource));
        generatedFiles
                .add(writeJavaSource(packageDir, BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME, providerSource));
        providerSourceSet.add(template.getModelBindingProviderName());

        return generatedFiles.build();

    }

    private File writeFile(final File file, final String source) {
        try (final OutputStream stream = buildContext.newFileOutputStream(file)) {
            try (final Writer fw = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                try (final BufferedWriter bw = new BufferedWriter(fw)) {
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

    private File writeJavaSource(final File packageDir, final String className, final String source) {
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
        final File file = new File(packageDir, className + ".java");
        writeFile(file, source);
        return file;
    }
}
