/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.WildcardType;
import org.opendaylight.yangtools.yang.common.QName;

public abstract class BaseRenderer {
    private static final String COMMA = ",";
    private static final String DOT = ".";

    private final GeneratedType type;
    private final Map<String, String> importMap;

    protected BaseRenderer(final GeneratedType type) {
        this.type = Preconditions.checkNotNull(type);
        this.importMap = new HashMap<>();
    }

    /**
     * Implementation needs to call Scala template render() method to generate string body
     * @return rendered body
     */
    protected abstract String body();

    protected GeneratedType getType() {
        return type;
    }

    protected String getFromImportMap(@NonNull String typeName) {
        return importMap.get(typeName);
    }

    protected void putToImportMap(@NonNull String typeName, String typePackageName) {
        importMap.put(typeName, typePackageName);
    }

    protected void putAllToImportMap(@NonNull Map<String, String> imports) {
        importMap.putAll(imports);
    }

    protected Map<String, String> getImportMap() {
        return ImmutableMap.copyOf(importMap);
    }

    /**
     * @param intype type to format and add to imports
     * @return formatted type
     */
    protected String importedName(final Type intype) {
        putTypeIntoImports(type, intype);
        return getExplicitType(type, intype);
    }

    protected String importedName(final Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    /**
     * @return package definition for template
     */
    protected String packageDefinition() {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ")
                .append(type.getPackageName())
                .append(";\n\n");
        return sb.toString();
    }

    /**
     * walks through map of imports
     * @return string of imports for template
     */
    private String imports() {
        final StringBuilder sb = new StringBuilder();
        if (!importMap.isEmpty()) {
            for (Map.Entry<String, String> entry : importMap.entrySet()) {
                if (!hasSamePackage(entry.getValue())) {
                    sb.append("import ")
                            .append(entry.getValue())
                            .append('.')
                            .append(entry.getKey())
                            .append(";\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     *
     * @param parameters
     * group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    protected String asArgumentsDeclaration(final Collection<GeneratedProperty> parameters) {
        final List<CharSequence> strings = new LinkedList<>();
        if (parameters.iterator().hasNext()) {
            for (GeneratedProperty parameter : parameters) {
                final StringBuilder sb = new StringBuilder();
                sb.append(importedName(parameter.getReturnType()));
                sb.append(' ');
                sb.append(TextTemplateUtil.fieldName(parameter));
                strings.add(sb);
            }
        }
        return String.join(", ", strings);
    }

    /**
     * Checks if package of generated type and imported type is the same
     * @param importedTypePackageName imported types package name
     * @return equals packages
     */
    protected boolean hasSamePackage(final String importedTypePackageName) {
        return type.getPackageName().equals(importedTypePackageName);
    }

    /**
     * Evaluates if it is necessary to add the package name for type to the map of imports for parentGenType
     * If it is so the package name is saved to the map imports.
     *
     * @param parentGenType generated type for which is the map of necessary imports build
     * @param type JAVA type for which is the necessary of the package import evaluated
     */
    private void putTypeIntoImports(final GeneratedType parentGenType, final Type type) {
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
        if (!importMap.containsKey(typeName)) {
            importMap.put(typeName, typePackageName);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            final Type[] params = paramType.getActualTypeArguments();
            if (params != null) {
                for (Type param : params) {
                    putTypeIntoImports(parentGenType, param);
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
     * @return string with type name for type in the full format or in the short format
     */
    private String getExplicitType(final GeneratedType parentGenType, final Type type) {
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(importMap != null, "Imports Map cannot be NULL!");

        final String typePackageName = Preconditions.checkNotNull(type.getPackageName());
        final String typeName = Preconditions.checkNotNull(type.getName());
        final String importedPackageName = importMap.get(typeName);
        final StringBuilder sb;
        if (typePackageName.equals(importedPackageName)) {
            sb = new StringBuilder(typeName);
            addActualTypeParameters(sb, type, parentGenType);
            if (sb.toString().equals("Void")) {
                return "void";
            }
        } else {
            sb = new StringBuilder();
            if (!typePackageName.isEmpty()) {
                sb.append(typePackageName).append(DOT).append(typeName);
            } else {
                sb.append(type.getName());
            }
            if (type.equals(Types.voidType())) {
                return "void";
            }
            addActualTypeParameters(sb, type, parentGenType);
        }
        return sb.toString();
    }

    /**
     * Adds actual type parameters from type to builder if type is ParametrizedType.
     *
     * @param sb string builder which contains type name
     * @param type JAVA Type for which is the string with type info generated
     * @param parentGenType generated type which contains type
     * @return adds actual type parameters to builder
     */
    private StringBuilder addActualTypeParameters(final StringBuilder sb, final Type type, final GeneratedType parentGenType) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) type;
            final Type[] pTypes = pType.getActualTypeArguments();
            sb.append('<');
            sb.append(getParameters(parentGenType, pTypes));
            sb.append('>');
        }
        return sb;
    }

    protected GeneratedProperty findProperty(final GeneratedTransferObject gto, String name) {
        for (GeneratedProperty prop : gto.getProperties()) {
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
        final StringBuilder sb = new StringBuilder();
        final Object qname = constant.getValue();
        sb.append("public static final ")
                .append(importedName(constant.getType()))
                .append(' ')
                .append(constant.getName())
                .append(" = ");
        if (qname instanceof QName) {
            sb.append(QName.class.getName())
                    .append(".create(\"")
                    .append(((QName) qname).getNamespace().toString())
                    .append("\", \"")
                    .append(((QName) qname).getFormattedRevision())
                    .append("\", \"")
                    .append(((QName) qname).getLocalName())
                    .append("\").intern()");
        } else {
            sb.append(qname);
        }
        sb.append(";\n");
        return sb.toString();
    }

    /**
     * Generates the string with all actual type parameters from
     *
     * @param parentGenType generated type for which is the JAVA code generated
     * @param pTypes array of Type instances = actual type parameters
     * @return string with all actual type parameters from pTypes
     */
    private String getParameters(final GeneratedType parentGenType, final Type[] pTypes) {
        if (pTypes == null || pTypes.length == 0) {
            return "?";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pTypes.length; i++) {
            final Type t = pTypes[i];

            String separator = COMMA;
            if (i == (pTypes.length - 1)) {
                separator = "";
            }


            if (t.equals(Types.voidType())) {
                sb.append("java.lang.Void")
                        .append(separator);
                continue;
            } else {

                String wildcardParam = "";
                if (t instanceof WildcardType) {
                    wildcardParam = "? extends ";
                }

                sb.append(wildcardParam).append(getExplicitType(parentGenType, t)).append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     * InterfaceTemplate / UnionTemaplate
     *
     * @param parameters list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    protected String generateParameters(final List<MethodSignature.Parameter> parameters) {
        final List<CharSequence> strings = new LinkedList<>();
        if (!parameters.isEmpty()) {
            for (MethodSignature.Parameter parameter : parameters) {
                final StringBuilder sb = new StringBuilder();
                sb.append(importedName(parameter.getType()));
                sb.append(' ');
                sb.append(parameter.getName());
                strings.add(sb);
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
        final StringBuilder sb = new StringBuilder();
        final String name = TextTemplateUtil.fieldName(field);
        final String importedName = Preconditions.checkNotNull(importedName(field.getReturnType()));
        sb.append("public ")
                .append(importedName)
                .append(' ')
                .append(TextTemplateUtil.getterMethodName(field))
                .append("() {")
                .append("return ")
                .append(name);
        if (!(field.getReturnType() instanceof ParameterizedType)
                && importedName.contains("[]")) {
            sb.append(" == null ? null : ")
                    .append(name)
                    .append(".clone()");
        }
        sb.append(";}\n");
        return sb.toString();
    }

    /**
     * builds template
     * @return generated final template
     */
    public String generateTemplate() {
        final StringBuilder sb = new StringBuilder();
        /* sb body must be filled before imports method call */
        final String templateBody = body();
        sb.append(packageDefinition())
                .append(imports())
                .append(templateBody);
        return sb.toString();
    }
}