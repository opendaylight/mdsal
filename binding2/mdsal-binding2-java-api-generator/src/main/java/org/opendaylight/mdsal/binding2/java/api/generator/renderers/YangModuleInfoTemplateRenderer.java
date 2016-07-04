/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding2.generator.util.Binding2Mapping.MODEL_BINDING_PROVIDER_CLASS_NAME;
import static org.opendaylight.mdsal.binding2.generator.util.Binding2Mapping.getRootPackageName;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.WildcardType;
import org.opendaylight.mdsal.binding2.spec.YangModuleInfo;
import org.opendaylight.mdsal.binding2.txt.modelProviderTemplate;
import org.opendaylight.mdsal.binding2.txt.yangModuleInfoTemplate;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class YangModuleInfoTemplateRenderer {
    /**
     * list of all imported names for template
     */
    private final Map<String, String> importedNames = new HashMap<>();
    private final Module module;
    private final SchemaContext ctx;
    private final Map<String, String> importMap = new LinkedHashMap<String, String>();
    private final String packageName;
    private final String modelBindingProviderName;

    public YangModuleInfoTemplateRenderer(final Module module, final SchemaContext ctx) {
        Preconditions.checkArgument(module != null, "Module must not be null.");
        this.module = module;
        this.ctx = ctx;
        this.packageName = getRootPackageName(module);
        this.modelBindingProviderName = packageName.concat(".").concat(MODEL_BINDING_PROVIDER_CLASS_NAME);
    }

    protected String body() {
        importedNames.put("string", importedName(String.class));
        importedNames.put("stringBuilder", importedName(StringBuilder.class));
        importedNames.put("set", importedName(Set.class));
        importedNames.put("hashSet", importedName(HashSet.class));
        importedNames.put("collections", importedName(Collections.class));
        importedNames.put("immutableSet", importedName(ImmutableSet.class));
        importedNames.put("inputStream", importedName(InputStream.class));
        importedNames.put("iOException", importedName(IOException.class));
        importedNames.put("yangModuleInfo", importedName(YangModuleInfo.class));
        return yangModuleInfoTemplate.render(module, ctx, importedNames).body();
    }

    /**
     * builds template
     * @return generated final template
     */
    public String generateTemplate() {
        final StringBuilder body = new StringBuilder();
        /* body must be filled before imports method call */
        final String templateBody = body();
        body.append("package ")
                .append(packageName)
                .append(";\n")
                .append(imports())
                .append(templateBody);
        return body.toString();
    }

    public String generateModelProvider() {
//        return modelProviderTemplate.render(packageName, MODEL_BINDING_PROVIDER_CLASS_NAME, YangModelBindingProvider
//                .class.getName(), YangModuleInfo.class.getName(), MODULE_INFO_CLASS_NAME).body();
//        TO DO missing YangModelBindingProvider in template
        return modelProviderTemplate.render(packageName, "YangModelBindingProvider.class.getName()", YangModuleInfo.class
        .getName()).body();
    }

    /**
     * walks through map of imports
     * @return string of imports for template
     */
    private String imports() {
        final StringBuilder importBuilder = new StringBuilder();
        if (!importMap.isEmpty()) {
            for (Map.Entry<String, String> entry : importMap.entrySet()) {
                if (!getRootPackageName(module).equals(entry.getValue())) {
                    importBuilder.append("import ")
                            .append(entry.getValue())
                            .append(".")
                            .append(entry.getKey())
                            .append(";\n");
                }
            }
        }
        return importBuilder.toString();
    }

    private String importedName(Class<?> cls) {
        final Type inType = Types.typeForClass(cls);
        putTypeIntoImports(inType);
        return getExplicitType(inType);
    }

    private void putTypeIntoImports(Type type) {
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

    private String getExplicitType(Type type) {
        final String typePackageName = type.getPackageName();
        final String typeName = type.getName();
        final String importedPackageName = importMap.get(typeName);
        final StringBuilder builder;
        if (typePackageName.equals(importedPackageName)) {
            builder = new StringBuilder(type.getName());
            if (builder.toString().equals("Void")) {
                return "void";
            }
            addActualTypeParameters(builder, type);
        } else {
            if (type.equals(Types.voidType())) {
                return "void";
            }
            builder = new StringBuilder();
            if (!typePackageName.isEmpty()) {
                builder.append(typePackageName)
                        .append(".")
                        .append(type.getName());
            } else {
                builder.append(type.getName());
            }
            addActualTypeParameters(builder, type);
        }
        return builder.toString();
    }

    private StringBuilder addActualTypeParameters(StringBuilder builder, Type type) {
        if (type instanceof ParameterizedType) {
            final Type[] pTypes = ((ParameterizedType) type).getActualTypeArguments();
            builder.append('<');
            builder.append(getParameters(pTypes));
            builder.append('>');
        }
        return builder;
    }

    private String getParameters(Type[] pTypes) {
        if (pTypes == null || pTypes.length == 0) {
            return "?";
        }
        final StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Type pType : pTypes) {
            final Type type = pTypes[i];
            String separator = ",";
            if (i == (pTypes.length - 1)) {
                separator = "";
            }
            String wildcardParam = "";
            if (type.equals(Types.voidType())) {
                builder.append("java.lang.Void" + separator);
            } else {
                if (type instanceof WildcardType) {
                    wildcardParam = "? extends ";
                }
                builder.append(wildcardParam + getExplicitType(type) + separator);
                i = i + 1;
            }
        }
        return builder.toString();
    }

    public static Module getSortedQName(Set<Module> modules, String name) {
        final TreeMap<Date, Module> sorted = new TreeMap<>();
        for (Module module : modules) {
            if (name.equals(module.getName())) {
                sorted.put(module.getRevision(), module);
            }
        }
        return sorted.lastEntry().getValue();
    }
}