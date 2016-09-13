/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.WildcardType;

public abstract class BaseRenderer {
    private static final String COMMA = ",";
    private static final String DOT = ".";

    protected final GeneratedType type;
    protected final Map<String, String> importMap;

    protected BaseRenderer(final GeneratedType type) {
        this.type = type;
        this.importMap = new HashMap<>();
    }

    protected abstract String body();

    /**
     * @param intype type to format and add to imports
     * @return formatted type
     */
    protected String importedName(Type intype) {
        putTypeIntoImports(type, intype, importMap);
        return getExplicitType(type, intype, importMap);
    }

    protected String importedName(Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    /**
     * @return package definition for template
     */
    public String packageDefinition() {
        final StringBuilder packageDefinition = new StringBuilder();
        packageDefinition.append("package ")
                .append(type.getPackageName())
                .append(";\n\n");
        return packageDefinition.toString();
    }

    /**
     * walks through map of imports
     * @return string of imports for template
     */
    private String imports() {
        final StringBuilder importBuilder = new StringBuilder();
        if (!importMap.isEmpty()) {
            for (Map.Entry<String, String> entry : importMap.entrySet()) {
                if (!hasSamePackage(entry.getValue())) {
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

    /**
     * Checks if package of generated type and imported type is the same
     * @param importedTypePackageName imported types package name
     * @return equals packages
     */
    private boolean hasSamePackage(String importedTypePackageName) {
        return type.getPackageName().equals(importedTypePackageName);
    }

    /**
     * Evaluates if it is necessary to add the package name for type to the map of imports for parentGenType
     * If it is so the package name is saved to the map imports.
     *
     * @param parentGenType generated type for which is the map of necessary imports build
     * @param type JAVA type for which is the necessary of the package import evaluated
     * @param imports map of the imports for parentGenType
     */
    private void putTypeIntoImports(final GeneratedType parentGenType, final Type type,
                                    final Map<String, String> imports) {
        checkArgument(parentGenType != null, "Parent Generated Type parameter MUST be specified and cannot be NULL!");
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(parentGenType.getPackageName() != null,
                "Parent Generated Type cannot have Package Name referenced as NULL!");

        final String typeName = Preconditions.checkNotNull(type.getName());
        final String typePackageName = Preconditions.checkNotNull(type.getPackageName());
        final String parentTypeName = Preconditions.checkNotNull(parentGenType.getName());
        if (typeName.equals(parentTypeName) || typePackageName.startsWith("java.lang") || typePackageName.isEmpty()) {
            return;
        }
        if (!imports.containsKey(typeName)) {
            imports.put(typeName, typePackageName);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            final Type[] params = paramType.getActualTypeArguments();
            if (params != null) {
                for (Type param : params) {
                    putTypeIntoImports(parentGenType, param, imports);
                }
            }
        }
    }

    /**
     * Builds the string which contains either the full path to the type (package name with type) or only type name
     * if the package is among imports.
     *
     * @param parentGenType generated type which contains type
     * @param type JAVA type for which is the string with type info generated
     * @param imports map of necessary imports for parentGenType
     * @return string with type name for type in the full format or in the short format
     */
    public String getExplicitType(final GeneratedType parentGenType, final Type type,
                                         final Map<String, String> imports) {
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(imports != null, "Imports Map cannot be NULL!");

        final String typePackageName = Preconditions.checkNotNull(type.getPackageName());
        final String typeName = Preconditions.checkNotNull(type.getName());
        final String importedPackageName = imports.get(typeName);
        final StringBuilder builder;
        if (typePackageName.equals(importedPackageName)) {
            builder = new StringBuilder(typeName);
            addActualTypeParameters(builder, type, parentGenType, imports);
            if (builder.toString().equals("Void")) {
                return "void";
            }
        } else {
            builder = new StringBuilder();
            if (!typePackageName.isEmpty()) {
                builder.append(typePackageName + DOT + typeName);
            } else {
                builder.append(type.getName());
            }
            if (type.equals(Types.voidType())) {
                return "void";
            }
            addActualTypeParameters(builder, type, parentGenType, imports);
        }
        return builder.toString();
    }

    /**
     * Adds actual type parameters from type to builder if type is ParametrizedType.
     *
     * @param builder string builder which contains type name
     * @param type JAVA Type for which is the string with type info generated
     * @param parentGenType generated type which contains type
     * @param imports map of necessary imports for parentGenType
     * @return adds actual type parameters to builder
     */
    private StringBuilder addActualTypeParameters(final StringBuilder builder, final Type type,
                                                         final GeneratedType parentGenType, final Map<String, String> imports) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) type;
            final Type[] pTypes = pType.getActualTypeArguments();
            builder.append("<");
            builder.append(getParameters(parentGenType, pTypes, imports));
            builder.append(">");
        }
        return builder;
    }

    /**
     * Generates the string with all actual type parameters from
     *
     * @param parentGenType generated type for which is the JAVA code generated
     * @param pTypes array of Type instances = actual type parameters
     * @param availableImports map of imports for parentGenType
     * @return string with all actual type parameters from pTypes
     */
    private String getParameters(final GeneratedType parentGenType, final Type[] pTypes,
                                        final Map<String, String> availableImports) {
        if (pTypes == null || pTypes.length == 0) {
            return "?";
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pTypes.length; i++) {
            final Type t = pTypes[i];

            String separator = COMMA;
            if (i == (pTypes.length - 1)) {
                separator = "";
            }

            String wildcardParam = "";
            if (t.equals(Types.voidType())) {
                builder.append("java.lang.Void")
                        .append(separator);
                continue;
            } else {

                if (t instanceof WildcardType) {
                    wildcardParam = "? extends ";
                }

                builder.append(wildcardParam)
                        .append(getExplicitType(parentGenType, t, availableImports) + separator);
            }
        }
        return builder.toString();
    }

    /**
     * builds template
     * @return generated final template
     */
    public String generateTemplate() {
        final StringBuilder body = new StringBuilder();
        /*body must be filled before imports method call*/
        final String templateBody = body();
        body.append(packageDefinition())
                .append(imports())
                .append(templateBody);
        return body.toString();
    }
}