/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer.normalizeFullPackageName;
import static org.opendaylight.mdsal.binding.javav2.util.BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME;
import static org.opendaylight.mdsal.binding.javav2.util.BindingMapping.getRootPackageName;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.modelProviderTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.yangModuleInfoTemplate;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.WildcardType;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModelBindingProvider;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

public class YangModuleInfoTemplateRenderer {

    private final Module module;
    private final SchemaContext ctx;
    private final Map<String, String> importMap = new HashMap<>();
    private final String packageName;
    private final String modelBindingProviderName;
    private final Function<Module, java.util.Optional<String>> moduleFilePathResolver;

    public YangModuleInfoTemplateRenderer(final Module module, final SchemaContext ctx, final Function<Module,
            java.util.Optional<String>> moduleFilePathResolver) {

        Preconditions.checkArgument(module != null, "Module must not be null.");
        this.module = module;
        this.ctx = ctx;
        this.packageName = normalizeFullPackageName(getRootPackageName(module));
        this.moduleFilePathResolver = moduleFilePathResolver;

        final StringBuilder sb = new StringBuilder();
        sb.append(packageName)
            .append('.')
            .append(MODEL_BINDING_PROVIDER_CLASS_NAME);
        this.modelBindingProviderName = sb.toString();
    }

    protected String body() {
        /**
         * list of all imported names for template.
         */
        final Map<String, String> importedNames = new HashMap<>();

        importedNames.put("string", importedName(String.class));
        importedNames.put("stringBuilder", importedName(StringBuilder.class));
        importedNames.put("set", importedName(Set.class));
        importedNames.put("hashSet", importedName(HashSet.class));
        importedNames.put("collections", importedName(Collections.class));
        importedNames.put("immutableSet", importedName(ImmutableSet.class));
        importedNames.put("inputStream", importedName(InputStream.class));
        importedNames.put("iOException", importedName(IOException.class));
        importedNames.put("yangModuleInfo", importedName(YangModuleInfo.class));
        importedNames.put("optional", importedName(Optional.class));
        importedNames.put("semVer", importedName(SemVer.class));
        importedNames.put("schemaSourceRepresentation", importedName(SchemaSourceRepresentation.class));

        return yangModuleInfoTemplate.render(module, ctx, importedNames, moduleFilePathResolver).body();
    }

    /**
     * Builds template.
     * @return generated final template
     */
    public String generateTemplate() {
        final StringBuilder sb = new StringBuilder();
        /* body must be filled before imports method call */
        final String templateBody = body();
        sb.append("package ")
            .append(packageName)
            .append(";\n\n")
            .append(imports())
            .append(templateBody);
        return sb.toString();
    }

    public String generateModelProvider() {
        return modelProviderTemplate.render(packageName, YangModelBindingProvider.class.getName(), YangModuleInfo.class
        .getName()).body();
    }

    /**
     * Walks through map of imports.
     * @return string of imports for template
     */
    private String imports() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : importMap.entrySet()) {
            if (!getRootPackageName(module).equals(entry.getValue())) {
                sb.append("import ")
                    .append(entry.getValue())
                    .append('.')
                    .append(entry.getKey())
                    .append(";\n");
            }
        }
        return sb.toString();
    }

    private String importedName(final Class<?> cls) {
        final Type inType = Types.typeForClass(cls);
        putTypeIntoImports(inType);
        return getExplicitType(inType);
    }

    private void putTypeIntoImports(final Type type) {
        final String typeName = type.getName();
        final String typePackageName = type.getPackageName();
        if (typePackageName.startsWith("java.lang") || typePackageName.isEmpty()) {
            return;
        }
        if (!importMap.containsKey(typeName)) {
            importMap.put(typeName, typePackageName);
        }
        if (type instanceof ParameterizedType) {
            final Type[] params = ((ParameterizedType) type).getActualTypeArguments();
            if (params != null) {
                for (Type param : params) {
                    putTypeIntoImports(param);
                }
            }
        }
    }

    private String getExplicitType(final Type type) {
        final String typePackageName = type.getPackageName();
        final String typeName = type.getName();
        final String importedPackageName = importMap.get(typeName);
        final StringBuilder sb;
        if (typePackageName.equals(importedPackageName)) {
            sb = new StringBuilder(type.getName());
            if (sb.toString().equals("Void")) {
                return "void";
            }
            addActualTypeParameters(sb, type);
        } else {
            if (type.equals(Types.voidType())) {
                return "void";
            }
            sb = new StringBuilder();
            if (!typePackageName.isEmpty()) {
                sb.append(typePackageName)
                    .append('.')
                    .append(type.getName());
            } else {
                sb.append(type.getName());
            }
            addActualTypeParameters(sb, type);
        }
        return sb.toString();
    }

    private StringBuilder addActualTypeParameters(final StringBuilder sb, final Type type) {
        if (type instanceof ParameterizedType) {
            final Type[] pTypes = ((ParameterizedType) type).getActualTypeArguments();
            sb.append('<');
            sb.append(getParameters(pTypes));
            sb.append('>');
        }
        return sb;
    }

    private String getParameters(final Type[] ptypes) {
        if (ptypes == null || ptypes.length == 0) {
            return "?";
        }
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Type ptype : ptypes) {
            final Type type = ptypes[count];
            String separator = ",";
            if (count == (ptypes.length - 1)) {
                separator = "";
            }
            String wildcardParam = "";
            if (type.equals(Types.voidType())) {
                sb.append("java.lang.Void" + separator);
            } else {
                if (type instanceof WildcardType) {
                    wildcardParam = "? extends ";
                }
                sb.append(wildcardParam + getExplicitType(type) + separator);
                count = count + 1;
            }
        }
        return sb.toString();
    }

    public static Module getSortedQName(final Set<Module> modules, final String name) {
        final TreeMap<Date, Module> sorted = new TreeMap<>();
        for (Module module : modules) {
            if (name.equals(module.getName())) {
                sorted.put(module.getRevision(), module);
            }
        }
        return sorted.lastEntry().getValue();
    }

    public String getModelBindingProviderName() {
        return modelBindingProviderName;
    }
}