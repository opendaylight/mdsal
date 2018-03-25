/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map.Entry;
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
    // Effectively imported names.
    private final BiMap<JavaTypeName, String> importedTypes = HashBiMap.create();

    // Types defined in this compilation unit. They are obscuring default imports and hence should never attempt
    // to import a simple name which would conflict.
    private final Multimap<String, JavaTypeName> localTypes;
    private final String packageName;

    private JavaImportTracker(final String packageName, final ListMultimap<String, JavaTypeName> localTypes) {
        this.packageName = requireNonNull(packageName);
        this.localTypes = ImmutableMultimap.copyOf(localTypes);

        // Seed importedTypes with types which we know are going to exist in this compilation unit.
        // TODO: we could improve this a bit by understanding the scope from which we are accessing the type, in terms
        //       of class nesting. That will further complicate the code structure, as we need to accurately model
        //       package/compilation unit type presence.
        localTypes.values().forEach(type -> importedTypes.put(type, type.localName()));
    }

    static JavaImportTracker forBuilderOf(final GeneratedType type) {
        return new JavaImportTracker(type.getIdentifier().packageName(), ImmutableListMultimap.of());
    }

    static JavaImportTracker forType(final GeneratedType type) {
        final JavaTypeName topLevel = type.getIdentifier();
        final ListMultimap<String, JavaTypeName> typeMap = ArrayListMultimap.create();
        typeMap.put(topLevel.simpleName(), topLevel);
        addEnclosedTypes(typeMap, type.getEnclosedTypes());
        addEnclosedTypes(typeMap, type.getEnumerations());
        return new JavaImportTracker(topLevel.packageName(), typeMap);
    }

    private static void addEnclosedTypes(final Multimap<String, JavaTypeName> typeMap,
            final List<? extends GeneratedType> enclosedTypes) {
        for (GeneratedType type : enclosedTypes) {
            final JavaTypeName name = type.getIdentifier();
            typeMap.put(name.simpleName(), name);
            addEnclosedTypes(typeMap, type.getEnclosedTypes());
            addEnclosedTypes(typeMap, type.getEnumerations());
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
        // Fast path: we have already resolved how to refer to this type.
        final String existing = importedTypes.get(typeName);
        if (existing != null) {
            return existing;
        }

        final String typePackage = typeName.packageName();
        final String simpleName = typeName.simpleName();
        if (typePackage.isEmpty()) {
            // This is a packageless primitive type, refer to it directly
            return simpleName;
        }

        // The type exists in the local package, but not in this compilation unit. This is tricky, as we may have
        // already introduced an import which shadows it either directly or its containing top-level type.
        if (packageName.equals(typePackage)) {
            // Try to anchor the top-level type and use a local reference
            final JavaTypeName toplevel = typeName.topLevelClass();
            if (importType(toplevel)) {
                if (toplevel.equals(typeName)) {
                    return simpleName;
                }

                final String localName = typeName.localName();
                importedTypes.put(typeName, localName);
                return localName;
            }
        }

        if (importType(typeName)) {
            return simpleName;
        }

        // We have to use FQCN, so cache it for reuse
        final String fqcn = typeName.toString();
        importedTypes.put(typeName, fqcn);
        return fqcn;
    }

    Stream<JavaTypeName> imports() {
        return importedTypes.entrySet().stream().filter(this::needsExplicitImport).map(Entry::getKey)
                .sorted((e1, e2) -> e1.toString().compareTo(e2.toString()));
    }

    private boolean needsExplicitImport(final Entry<JavaTypeName, String> entry) {
        final JavaTypeName name = entry.getKey();
        if (!packageName.equals(name.packageName())) {
            // Different package: need to import it
            return true;
        }
        if (!name.immediatelyEnclosingClass().isPresent()) {
            // This a top-level class import, we can skip it
            return false;
        }

        // This is a nested class, we need to spell it out if the import entry points to the simple name
        return entry.getValue().equals(name.simpleName());
    }

    private boolean importType(final JavaTypeName name) {
        if (importedTypes.containsKey(name)) {
            return true;
        }
        final String simpleName = name.simpleName();
        if (localTypes.containsKey(simpleName) || importedTypes.containsValue(simpleName)) {
            return false;
        }
        importedTypes.put(name, simpleName);
        return true;
    }
}
