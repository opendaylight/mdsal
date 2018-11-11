/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.maven.api.gen.plugin;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.opendaylight.mdsal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile.FileKind;
import org.opendaylight.mdsal.binding.java.api.generator.YangModuleInfoTemplate;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.maven.spi.generator.FileGenerator;
import org.opendaylight.yangtools.yang.maven.spi.generator.GeneratedFile;
import org.opendaylight.yangtools.yang.maven.spi.generator.GeneratedFileKind;
import org.opendaylight.yangtools.yang.maven.spi.generator.GeneratedFileLifecycle;
import org.opendaylight.yangtools.yang.maven.spi.generator.ImportResolutionMode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class JavaFileGenerator implements FileGenerator {
    public static final String CONFIG_IGNORE_DUPLICATE_FILES = "ignoreDuplicateFiles";

    private static final CharMatcher DOT_MATCHER = CharMatcher.is('.');
    private static final CharMatcher FS_MATCHER = CharMatcher.is(File.separatorChar);
    private static final String MODULE_INFO = BindingMapping.MODULE_INFO_CLASS_NAME + ".java";
    private static final String MODEL_BINDING_PROVIDER = BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME + ".java";
    private static final String MODEL_BINDING_PROVIDER_SERVICE =  "META-INF/services/"
            + YangModelBindingProvider.class.getName();

    private final boolean ignoreDuplicateFiles;

    JavaFileGenerator(final Map<String, String> additionalConfig) {
        String ignoreDuplicateFilesString = additionalConfig.get(CONFIG_IGNORE_DUPLICATE_FILES);
        if (ignoreDuplicateFilesString != null) {
            ignoreDuplicateFiles = Boolean.parseBoolean(ignoreDuplicateFilesString);
        } else {
            ignoreDuplicateFiles = true;
        }
    }
    @Override
    public String getIdentifier() {
        return JavaFileGenerator.class.getName();
    }

    @Override
    public Optional<ImportResolutionMode> suggestedImportResolutionMode() {
        return Optional.of(ImportResolutionMode.REVISION_EXACT_OR_LATEST);
    }
    @Override
    public boolean isAcceptableImportResolutionMode(final ImportResolutionMode mode) {
        return mode == ImportResolutionMode.REVISION_EXACT_OR_LATEST;
    }

    @Override
    public Table<GeneratedFileKind, String, GeneratedFile> generateFiles(final SchemaContext context,
            final Set<Module> localModules, final Function<Module, Optional<String>> moduleResourcePathResolver) {
        final ImmutableTable.Builder<GeneratedFileKind, String, GeneratedFile> result = ImmutableTable.builder();

        final Table<FileKind, String, Supplier<String>> generatedFiles = new GeneratorJavaFile(
            new BindingGeneratorImpl().generateTypes(context, localModules)).generateFileContent(ignoreDuplicateFiles);
        for (Cell<FileKind, String, Supplier<String>> cell : generatedFiles.cellSet()) {
            final GeneratedFileLifecycle lifecycle;
            switch (cell.getRowKey()) {
                case PERSISTENT:
                    lifecycle = GeneratedFileLifecycle.PERSISTENT;
                    break;
                case TRANSIENT:
                    lifecycle = GeneratedFileLifecycle.TRANSIENT;
                    break;
                default:
                    throw new IllegalStateException("Unsupported file type in " + cell);
            }

            result.put(GeneratedFileKind.SOURCE, FS_MATCHER.replaceFrom(cell.getColumnKey(), '/'),
                new GeneratedJavaFile(lifecycle, cell.getValue()));
        }

        // YangModuleInfo files
        final Builder<String> bindingProviders = ImmutableSet.builder();
        for (Module module : localModules) {
            final YangModuleInfoTemplate template = new YangModuleInfoTemplate(module, context,
                moduleResourcePathResolver);
            final String rootPackageName = template.getPackageName();
            final String path = DOT_MATCHER.replaceFrom(rootPackageName, '/') + "/";

            result.put(GeneratedFileKind.SOURCE, path + MODULE_INFO,
                new GeneratedJavaFile(GeneratedFileLifecycle.TRANSIENT, template::generate));
            result.put(GeneratedFileKind.SOURCE, path + MODEL_BINDING_PROVIDER,
                new GeneratedJavaFile(GeneratedFileLifecycle.TRANSIENT, template::generateModelProvider));

            bindingProviders.add(template.getModelBindingProviderName());
        }

        // META-INF/services entries
        final String providerBody = String.join("\n", bindingProviders.build());
        result.put(GeneratedFileKind.RESOURCE, MODEL_BINDING_PROVIDER_SERVICE,
            new GeneratedJavaFile(GeneratedFileLifecycle.TRANSIENT, () -> providerBody));

        return result.build();
    }
}
