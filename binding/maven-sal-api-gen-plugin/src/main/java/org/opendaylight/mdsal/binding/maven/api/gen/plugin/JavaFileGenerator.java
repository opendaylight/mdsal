/*
 * Copyright (c) 2020 PATHEON.tech, s.r.o. and others.  All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile.FileKind;
import org.opendaylight.mdsal.binding.java.api.generator.YangModuleInfoTemplate;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

final class JavaFileGenerator implements FileGenerator {
    public static final String CONFIG_IGNORE_DUPLICATE_FILES = "ignoreDuplicateFiles";

    private static final CharMatcher DOT_MATCHER = CharMatcher.is('.');
    private static final String MODULE_INFO = BindingMapping.MODULE_INFO_CLASS_NAME + ".java";
    private static final String MODEL_BINDING_PROVIDER = BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME + ".java";
    private static final GeneratedFilePath MODEL_BINDING_PROVIDER_SERVICE =  GeneratedFilePath.ofPath(
        "META-INF/services/" + YangModelBindingProvider.class.getName());

    private final BindingGenerator bindingGenerator;
    private final boolean ignoreDuplicateFiles;

    JavaFileGenerator(final Map<String, String> additionalConfig) {
        final String ignoreDuplicateFilesString = additionalConfig.get(CONFIG_IGNORE_DUPLICATE_FILES);
        if (ignoreDuplicateFilesString != null) {
            ignoreDuplicateFiles = Boolean.parseBoolean(ignoreDuplicateFilesString);
        } else {
            ignoreDuplicateFiles = true;
        }
        bindingGenerator = ServiceLoader.load(BindingGenerator.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("No BindingGenerator implementation found"));
    }

    @Override
    public Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(final EffectiveModelContext context,
            final Set<Module> localModules, final ModuleResourceResolver moduleResourcePathResolver)
                throws FileGeneratorException {
        final ImmutableTable.Builder<GeneratedFileType, GeneratedFilePath, GeneratedFile> result =
            ImmutableTable.builder();

        final Table<FileKind, String, Supplier<String>> generatedFiles = new GeneratorJavaFile(
            bindingGenerator.generateTypes(context, localModules)).generateFileContent(ignoreDuplicateFiles);
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

            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofFilePath(cell.getColumnKey()),
                new GeneratedJavaFile(lifecycle, cell.getValue()));
        }

        // YangModuleInfo files
        final Builder<String> bindingProviders = ImmutableSet.builder();
        for (Module module : localModules) {
            final YangModuleInfoTemplate template = new YangModuleInfoTemplate(module, context,
                mod -> moduleResourcePathResolver.findModuleResourcePath(module, YangTextSchemaSource.class));
            final String rootPackageName = template.getPackageName();
            final String path = DOT_MATCHER.replaceFrom(rootPackageName, '/') + "/";

            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofPath(path + MODULE_INFO),
                new GeneratedJavaFile(GeneratedFileLifecycle.TRANSIENT, template::generate));
            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofPath(path + MODEL_BINDING_PROVIDER),
                new GeneratedJavaFile(GeneratedFileLifecycle.TRANSIENT, template::generateModelProvider));

            bindingProviders.add(template.getModelBindingProviderName());
        }

        // META-INF/services entries, sorted to make the build predictable
        final List<String> sorted = new ArrayList<>(bindingProviders.build());
        sorted.sort(String::compareTo);

        final String providerBody = String.join("\n", sorted);
        result.put(GeneratedFileType.RESOURCE, MODEL_BINDING_PROVIDER_SERVICE,
            new GeneratedJavaFile(GeneratedFileLifecycle.TRANSIENT, () -> providerBody));

        return result.build();
    }
}
