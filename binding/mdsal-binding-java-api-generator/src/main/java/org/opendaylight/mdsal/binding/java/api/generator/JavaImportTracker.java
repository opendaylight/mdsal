/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.WildcardType;

/**
 * Second phase of import resolution. This class is initialized with the local names of the top-level and nested classes
 * and afterwards maintains the import map, giving out shortened names and then exporting an import block.
 */
@NotThreadSafe
final class JavaImportTracker {
    private static final String JAVA_LANG = "java.lang";
    private static final Set<JavaTypeName> JAVA_LANG_TYPES;

    static {
        final ClassPath cp;
        try {
            cp = ClassPath.from(JavaImportTracker.class.getClassLoader());
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        JAVA_LANG_TYPES = cp.getTopLevelClasses(JAVA_LANG).stream()
                .map(info -> JavaTypeName.create(info.getPackageName(), info.getSimpleName()))
                .collect(ImmutableSet.toImmutableSet());
    }

    private final BiMap<JavaTypeName, String> typeMap;
    private final String packageName;

    private JavaImportTracker(final String packageName, final BiMap<JavaTypeName, String> initialTypeMap) {
        this.packageName = requireNonNull(packageName);
        this.typeMap = requireNonNull(initialTypeMap);
    }

    static JavaImportTracker forBuilderOf(final GeneratedType type) {
        return new JavaImportTracker(type.getIdentifier().packageName(), HashBiMap.create());
    }

    static JavaImportTracker forType(final GeneratedType type) {
        final JavaTypeName topLevel = type.getIdentifier();
        final BiMap<String, JavaTypeName> typeMap = HashBiMap.create();
        typeMap.put(topLevel.simpleName(), topLevel);

        addEnclosedTypes(typeMap, type.getEnclosedTypes());
        return new JavaImportTracker(topLevel.packageName(), typeMap.inverse());
    }

    private static void addEnclosedTypes(final BiMap<String, JavaTypeName> typeMap,
            final List<GeneratedType> enclosedTypes) {
        for (GeneratedType type : enclosedTypes) {
            final JavaTypeName name = type.getIdentifier();
            typeMap.put(name.localName(), name);
            addEnclosedTypes(typeMap, type.getEnclosedTypes());
        }
    }

    String referenceString(final Type type) {
        if (!(type instanceof ParameterizedType)) {
            return referenceString(type.getIdentifier());
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(referenceString(type.getIdentifier())).append('<');
        final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        if (types.length == 0) {
            return sb.append("?>").toString();
        }

        for (int i = 0; i < types.length; i++) {
            final Type t = types[i];
            if (t instanceof WildcardType) {
                sb.append("? extends ");
            }
            sb.append(referenceString(t));
            if (i != types.length - 1) {
                sb.append(", ");
            }
        }

        return sb.append('>').toString();
    }

    String referenceString(final JavaTypeName typeName) {
        String existing = typeMap.get(typeName);
        if (existing != null) {
            return existing;
        }

        // For types which share the same package, we want to use the local name (without package) and record it, unless
        // it conflicts with an existing import. The same is true for java.lang.* types.
        final String typePackage = typeName.packageName();
        final String simpleName = typeName.simpleName();
        if (!typePackage.isEmpty()) {
            if (typePackage.equals("java.lang") || typePackage.equals(packageName)) {
                final String localName = typeName.localName();
                if (!typeMap.containsValue(localName)) {
                    typeMap.put(typeName, localName);
                    return localName;
                }
            }

            if (typeMap.containsValue(simpleName)) {
                // Cannot import, as the short name is already taken. Use FQCN.
                return typeName.toString();
            }

            typeMap.put(typeName, simpleName);
        }
        return simpleName;
    }

    Stream<JavaTypeName> imports() {
        return typeMap.keySet().stream().filter(this::requiresImport)
                .sorted((e1, e2) -> e1.toString().compareTo(e2.toString()));
    }

    private boolean requiresImport(final JavaTypeName name) {
        return !JAVA_LANG_TYPES.contains(name) && !packageName.equals(name.packageName());
    }
}
