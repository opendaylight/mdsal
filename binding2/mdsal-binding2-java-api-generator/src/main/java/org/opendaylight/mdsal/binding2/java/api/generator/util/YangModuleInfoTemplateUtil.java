/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.java.api.generator.util;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;

public final class YangModuleInfoTemplateUtil {
    private static Map<String, String> importMap = new LinkedHashMap<>();
    private YangModuleInfoTemplateUtil() {
        throw new UnsupportedOperationException("Util class");
    }

    public static Map<String, String> getImportMap() {
        return importMap;
    }

    public static String getFormattedRevision(Date revision) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(revision);
    }

    public static String getImportedNameString() {
        return importedName(String.class);
    }

    public static String getImportedNameSet() {
        return importedName(Set.class);
    }

    public static String getImportedNameHashSet() {
        return importedName(HashSet.class);
    }

    public static String getImportedNameImmutableSet() {
        return importedName(ImmutableSet.class);
    }

    public static String getImportedNameStringBuilder() {
        return importedName(StringBuilder.class);
    }

    public static String getImportedNameCollections() {
        return importedName(Collections.class);
    }

    public static String getImportedNameYangModuleInfo() {
        return importedName(YangModuleInfo.class);
    }

    public static String getImportedNameInputStream() {
        return importedName(InputStream.class);
    }

    public static String getImportedNameIOException() {
        return importedName(IOException.class);
    }

    public static QNameModule getSortedQName(Set<Module> modules, String name) {
        final TreeMap<Date, Module> sorted = new TreeMap<>();
        for (Module module : modules) {
            if (name.equals(module.getName())) {
                sorted.put(module.getRevision(), module);
            }
        }
        return sorted.lastEntry().getValue().getQNameModule();
    }

    public static String getSourcePath(Module module) {
        return "/".concat(module.getModuleSourcePath().replace(java.io.File.separatorChar, '/'));
    }

    public static String importedName(Class<?> cls) {
        final Type inType = Types.typeForClass(cls);
        putTypeIntoImports(inType);
        return getExplicitType(inType);
    }

    private static void putTypeIntoImports(Type type) {
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

    private static String getExplicitType(Type type) {
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

    private static StringBuilder addActualTypeParameters(StringBuilder builder, Type type) {
        if (type instanceof ParameterizedType) {
            final Type[] pTypes = ((ParameterizedType) type).getActualTypeArguments();
            builder.append('<');
            builder.append(getParameters(pTypes));
            builder.append('>');
        }
        return builder;
    }

    private static String getParameters(Type[] pTypes) {
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
}