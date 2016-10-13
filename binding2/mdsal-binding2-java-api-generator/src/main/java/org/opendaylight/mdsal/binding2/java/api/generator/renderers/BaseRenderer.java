/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.fieldName;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.getterMethodName;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.WildcardType;
import org.opendaylight.yangtools.yang.common.QName;

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
    protected String importedName(final Type intype) {
        putTypeIntoImports(type, intype, importMap);
        return getExplicitType(type, intype, importMap);
    }

    protected String importedName(final Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    /**
     * @return package definition for template
     */
    private String packageDefinition() {
        final StringBuilder packageDefinition = new StringBuilder();
        packageDefinition.append("package ")
                .append(type.getPackageName())
                .append(";\n");
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
     * Template method which generates method parameters with their types from <code>parameters</code>.
     *
     * @param parameters
     * group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    protected String asArgumentsDeclaration(final Iterable<GeneratedProperty> parameters) {
        final List<String> strings = new LinkedList<>();
        if (parameters.iterator().hasNext()) {
            for (GeneratedProperty parameter : parameters) {
                final StringBuilder parameterWithName = new StringBuilder();
                parameterWithName.append(importedName(parameter.getReturnType()));
                parameterWithName.append(" ");
                parameterWithName.append(fieldName(parameter));
                strings.add(parameterWithName.toString());
            }
        }
        return String.join(", ", strings);
    }

    /**
     * Checks if package of generated type and imported type is the same
     * @param importedTypePackageName imported types package name
     * @return equals packages
     */
    private boolean hasSamePackage(final String importedTypePackageName) {
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
    private void putTypeIntoImports(final GeneratedType parentGenType, final Type type, final Map<String, String> imports) {
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
    private String getExplicitType(final GeneratedType parentGenType, final Type type, final Map<String, String> imports) {
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
    private StringBuilder addActualTypeParameters(final StringBuilder builder, final Type type, final GeneratedType parentGenType,
                                                  final Map<String, String> imports) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) type;
            final Type[] pTypes = pType.getActualTypeArguments();
            builder.append("<");
            builder.append(getParameters(parentGenType, pTypes, imports));
            builder.append(">");
        }
        return builder;
    }

    protected GeneratedProperty findProperty(final GeneratedTransferObject gto, String name) {
        final List<GeneratedProperty> props = gto.getProperties();
        for (GeneratedProperty prop : props) {
            if (name.equals(prop.getName())) {
                return prop;
            }
        }
        final GeneratedTransferObject parent = gto.getSuperType();
        if (parent != null) {
            return findProperty(parent, name);
        }
        return null;
    }

    /**
     * @param constant
     * @return string with constant wrapped in code
     */
    protected String emitConstant(final Constant constant) {
        final StringBuilder constantBuilder = new StringBuilder();
        final Object qname = constant.getValue();
        constantBuilder.append("public static final ")
                .append(importedName(constant.getType()))
                .append(" ")
                .append(constant.getName())
                .append(" = ");
        if (qname instanceof QName) {
            constantBuilder.append(QName.class.getName())
                    .append(".create(\"")
                    .append(((QName) qname).getNamespace().toString())
                    .append("\", \"")
                    .append(((QName) qname).getFormattedRevision())
                    .append("\", \"")
                    .append(((QName) qname).getLocalName())
                    .append("\").intern()");
        } else {
            constantBuilder.append(qname);
        }
        constantBuilder.append(";\n");
        return constantBuilder.toString();
    }

    /**
     * Generates the string with all actual type parameters from
     *
     * @param parentGenType generated type for which is the JAVA code generated
     * @param pTypes array of Type instances = actual type parameters
     * @param availableImports map of imports for parentGenType
     * @return string with all actual type parameters from pTypes
     */
    private String getParameters(final GeneratedType parentGenType, final Type[] pTypes, final Map<String, String> availableImports) {
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
     * Template method which generates method parameters with their types from <code>parameters</code>.
     * InterfaceTemplate / UnionTemaplate
     *
     * @param parameters list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    protected String generateParameters(final List<MethodSignature.Parameter> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!parameters.isEmpty()) {
            for (MethodSignature.Parameter parameter : parameters) {
                final StringBuilder parameterWithType = new StringBuilder();
                parameterWithType.append(importedName(parameter.getType()));
                parameterWithType.append(" ");
                parameterWithType.append(parameter.getName());
                strings.add(parameterWithType.toString());
            }
        }
        return String.join(", ", strings);
    }

    /**
     * Template method which generates the getter method for field
     *
     * @param field generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    protected String getterMethod(final GeneratedProperty field) {
        final StringBuilder getterMethod = new StringBuilder();
        final String name = fieldName(field);
        final String importedName = Preconditions.checkNotNull(importedName(field.getReturnType()));
        getterMethod.append("public ")
                .append(importedName)
                .append(" ")
                .append(getterMethodName(field))
                .append("() {")
                .append("return ")
                .append(name);
        if (importedName.contains("[]")) {
            getterMethod.append(" == null ? null : ")
                    .append(name)
                    .append(".clone()");
        }
        getterMethod.append(";}");
        return getterMethod.toString();
    }

    /**
     * builds template
     * @return generated final template
     */
    public String generateTemplate() {
        final StringBuilder body = new StringBuilder();
        /* body must be filled before imports method call */
        final String templateBody = body();
        body.append(packageDefinition())
                .append(imports())
                .append(templateBody);
        return body.toString();
    }
}