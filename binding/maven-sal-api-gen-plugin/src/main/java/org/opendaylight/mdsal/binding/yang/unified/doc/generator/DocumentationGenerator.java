/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.unified.doc.generator;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

final class DocumentationGenerator implements FileGenerator {

    @Override
    public Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(final EffectiveModelContext context,
            final Set<Module> localModules, final ModuleResourceResolver moduleResourcePathResolver) {
        final Map<TypeDefinition<?>, SchemaPath> types = createTypes(context);

        final var result = ImmutableTable.<GeneratedFileType, GeneratedFilePath, GeneratedFile>builder();
        for (Module module : localModules) {
            result.put(GeneratedFileType.RESOURCE, GeneratedFilePath.ofPath(module.getName() + ".html"),
                GeneratedFile.of(GeneratedFileLifecycle.TRANSIENT,
                    new DocumentationTemplate(context, types, module).generate()));
        }

        return result.build();
    }

    private static Map<TypeDefinition<?>, SchemaPath> createTypes(final EffectiveModelContext context) {
        final Map<TypeDefinition<?>, SchemaPath> types = new IdentityHashMap<>();
        fillTypes(types, SchemaPath.ROOT, context.getModuleStatements().values());
        return types;
    }

    private static void fillTypes(final Map<TypeDefinition<?>, SchemaPath> types, final SchemaPath path,
            final Collection<? extends EffectiveStatement<?, ?>> stmts) {
        for (EffectiveStatement<?, ?> stmt : stmts) {
            final Object arg = stmt.argument();
            if (arg instanceof QName) {
                final SchemaPath stmtPath = path.createChild((QName) arg);
                if (stmt instanceof TypeDefinition) {
                    types.putIfAbsent((TypeDefinition<?>) stmt, stmtPath);
                } else if (stmt instanceof TypeAware) {
                    final TypeDefinition<?> type = ((TypeAware) stmt).getType();
                    types.putIfAbsent(type, stmtPath.createChild(type.getQName()));
                }

                fillTypes(types, stmtPath, stmt.effectiveSubstatements());
            }
        }
    }
}
