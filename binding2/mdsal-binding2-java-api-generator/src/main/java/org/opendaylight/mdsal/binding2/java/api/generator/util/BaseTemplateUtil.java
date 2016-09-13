/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.java.api.generator.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.TypeMember;
import org.opendaylight.mdsal.binding2.model.api.WildcardType;
import org.opendaylight.mdsal.binding2.spec.YangModuleInfo;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;

public class BaseTemplateUtil {
    private GeneratedType type;
    private Map<String, String> importMap = new HashMap<String,String>();
    private Set<String> setOfImports;

    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");
    private static final String COMMA = ",";
    private static final String UNDERSCORE = "_";
    private static final String TO_STRING = ".toString";
    private static final char NEW_LINE = '\n';
    private static final CharMatcher NL_MATCHER = CharMatcher.is(NEW_LINE);
    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final Splitter NL_SPLITTER = Splitter.on(NL_MATCHER);
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');

    public static final String DOT = ".";
//    used in interfaceTemplate
    public static final String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";

    /*
     needed order
     1 packageDefinition()
     2 imports() fill set of imports
     3 body
     */

    public static String packageDefinition(GeneratedType type) {
        final StringBuilder packageDefinition = new StringBuilder();
        packageDefinition.append("package ")
                .append(type.getPackageName())
                .append(";\n");
        return packageDefinition.toString();
    }

    public static String getImportedNameString() {
        return importedName(String.class);
    }

    public static String getImportedNameArrays() {
        return importedName(Arrays.class);
    }

    public static String getImportedNameForMap() {
        return importedName(Map.class);
    }

    public static String getImportedNameObject() {
        return importedName(Object.class);
    }

    public static String getImportedNameForCollections() {
        return importedName(Collections.class);
    }

    public static String getImportedNameStringBuilder() {
        return importedName(StringBuilder.class);
    }

    public static String getImportedNameHashMap() {
        return importedName(HashMap.class);
    }

    public static String getImportedNameObjects() {
        return importedName(Objects.class);
    }

    public static String getImportedNameForClass() {
        return importedName(Class.class);
    }

    public static String getImportedNameUnsupportedOperationException() {
        return BaseTemplateUtil.importedName(UnsupportedOperationException.class);
    }

    public static String YangModuleInfoGetName() {
        return YangModuleInfo.class.getName();
    }

    public static String getImportedNameConstructorProperties() {
        return BaseTemplateUtil.importedName(ConstructorProperties.class);
    }

    /**
     * fills setOfImports with imports as strings
     */
    public void imports() {
        if (!this.importMap.isEmpty()) {
            final StringBuilder importBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : this.importMap.entrySet()) {
                if (!hasSamePackage(entry.getValue())) {
                    importBuilder.append("import ")
                            .append(entry.getValue())
                            .append(".")
                            .append(entry.getKey())
                            .append(";\n");
                    this.setOfImports.add(importBuilder.toString());
                }
            }
        }
    }

    /**
     * Checks if package of generated type and imported type is the same
     *
     * @param importedTypePackageName imported types package name
     * @return equals packages
     */
    private boolean hasSamePackage(String importedTypePackageName) {
        // to do fix this is never initialized
        return type.getPackageName().equals(importedTypePackageName);
    }

    /**
     * sets fieldname according to property for return type
     * method(type fieldname)
     *
     * @param property type from getter
     */
    public static String fieldName(GeneratedProperty property) {
        final String name = Preconditions.checkNotNull(property.getName());
        return UNDERSCORE.concat(name);
    }

    /**
     * Cuts prefix from getter name
     *
     * @param getter getter name
     * @return getter name without prefix
     */
    public static String propertyNameFromGetter(MethodSignature getter) {
        final String name = Preconditions.checkNotNull(getter.getName());
        int prefix;
        if (name.startsWith("is")) {
            prefix = 2;
        } else if (name.startsWith("get")) {
            prefix = 3;
        } else {
            throw new IllegalArgumentException("Not a getter");
        }
        return toFirstLower(name.substring(prefix));
    }

    /**
     * Makes start of getter name LowerCase
     *
     * @param s getter name without prefix
     * @return getter name starting in LowerCase
     */
    public static String toFirstLower(String s) {
        return s != null && s.length() != 0?(Character.isLowerCase(s.charAt(0))?s:(s.length() == 1?s.toLowerCase():s.substring(0, 1).toLowerCase() + s.substring(1))):s;
    }

    /**
     * Template method which generates the getter method for field
     *
     * @param field generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    public static String getterMethod(GeneratedProperty field) {
        final StringBuilder getterMethod = new StringBuilder();
        final String fieldName = fieldName(field);
        final String importedName = Preconditions.checkNotNull(importedName(field.getReturnType()));
        getterMethod.append("public ")
                .append(importedName)
                .append(" ")
                .append(getterMethodName(field))
                .append("() {")
                .append("return ")
                .append(fieldName);
        if (importedName.contains("[]")) {
            getterMethod.append(" == null ? null : ")
                    .append(fieldName)
                    .append(".clone()");
        }
        getterMethod.append(";}");
        return getterMethod.toString();
    }

    /**
     * Helper method for building getter
     *
     * @param field property name
     * @return getter for propery
     */
    public static String getterMethodName (GeneratedProperty field) {
        final Type type = Preconditions.checkNotNull(field.getReturnType());
        final String name = Preconditions.checkNotNull(field.getName());
        final String prefix = Types.BOOLEAN.equals(type) ? "is" : "get";
        return prefix.concat(toFirstUpper(name));
    }

    /**
     * Makes start of getter name uppercase
     *
     * @param s getter name without prefix
     * @return getter name starting in uppercase
     */
    public static String toFirstUpper(String s) {
        return s != null && s.length() != 0?(Character.isUpperCase(s.charAt(0))?s:(s.length() == 1?s.toUpperCase():s.substring(0, 1).toUpperCase() + s.substring(1))):s;
    }

    /**
     * Template method which generates the setter method for field
     *
     * @param field generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format
     */
    public String setterMethod(GeneratedProperty field, GeneratedType type) {
        final StringBuilder getterMethod = new StringBuilder();
        getterMethod.append("public ")
                .append(type.getName())
                .append(" set")
                .append(toFirstUpper(field.getName()))
                .append("(")
                .append(importedName(field.getReturnType()))
                .append(" value) {")
                .append("this.")
                .append(fieldName(field))
                .append(" = value;")
                .append("return this;}");
        return getterMethod.toString();
    }

    /**
     * generates type
     * method(importedName parameterName)
     *
     * @param intype type to format
     * @return formated type
     */
    public static String importedName(Type intype) {
//        putTypeIntoImports(type, intype, importMap);
//        return getExplicitType(type, intype, importMap);
        return null;
    }

    public static String importedName(Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    /**
     * Template method which generates method parameters with their types
     * method(type _type, type1 _type1)
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    private String asArgumentsDeclaration(Iterable<GeneratedProperty> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!isEmpty(parameters)) {
            for (GeneratedProperty parameter : parameters) {
                final StringBuilder parameterWithType = new StringBuilder();
                parameterWithType.append(importedName(parameter.getReturnType()));
                parameterWithType.append(" ");
                parameterWithType.append(fieldName(parameter));
                strings.add(parameterWithType.toString());
            }
        }
        return String.join(", ", strings);
    }

    /**
     * Template method which generates sequence of the names of the class attributes
     *
     * @param parameters group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names
     */
    public static String asArguments(Iterable<GeneratedProperty> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!isEmpty(parameters)) {
            for (GeneratedProperty parameter : parameters) {
                strings.add((fieldName(parameter)));
            }
        }
        return String.join(", ", strings);
    }

    /**
     * Template method which generates JAVA comments. InterfaceTemplate
     *
     * @param comment comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    public static String asJavadoc(String comment) {
        if (comment == null) {
            return "";
        }
        return wrapToDocumentation(formatToParagraph(comment.trim(), 0));
    }

    /**
     * Wraps text as documentation, used in enum description
     *
     * @param text text for wrapping
     * @return wrapped text
     */
    public static String wrapToDocumentation(String text) {
        if (text.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder("/**");
        sb.append(NEW_LINE);
        Iterable<String> lineSplitText = NL_SPLITTER.split(text);
        for (final String t : lineSplitText) {
            if (!t.isEmpty()) {
                sb.append(" *");
                sb.append(" ");
                sb.append(t);
                sb.append(NEW_LINE);
            }
        }
        sb.append(" */");
        return sb.toString();
    }

    public static String formatDataForJavaDoc(GeneratedType type) {
        final String description = type.getDescription().isPresent() ? type.getDescription().get() : "";
        return encodeJavadocSymbols(description);
    }

    public static String formatDataForJavaDoc(GeneratedType type, String additionalComment) {
        StringBuilder javaDoc = new StringBuilder();
        if (!type.getDescription().isPresent()) {
            javaDoc.append(type.getDescription())
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }
        javaDoc.append(additionalComment)
                .append(TO_STRING);
        return javaDoc.toString();
    }

    public static String formatDataForJavaDoc(TypeMember type, String additionalComment) {
        StringBuilder javaDoc = new StringBuilder();
        if (!(type.getComment() == null || type.getComment().isEmpty())) {
            javaDoc.append(type.getComment())
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }
        javaDoc.append(additionalComment)
                .append(TO_STRING);
        /*FIX ME base template 258*/
        return javaDoc.toString();
    }

    public static String getJavaDocForInterface(MethodSignature methodSignature) {
        StringBuilder javaDoc = new StringBuilder();
        javaDoc.append("@return ")
                .append(asCode(methodSignature.getReturnType().getFullyQualifiedName()))
                .append(" ")
                .append(asCode(propertyNameFromGetter(methodSignature)))
                .append(", or ")
                .append(asCode("null"))
                .append(" if not present");
        return formatDataForJavaDoc(methodSignature, javaDoc.toString());
    }

    private static String asCode(String text) {
        return "<code>" + text + "</code>";
    }

    public String asLink(String text) {
        final StringBuilder link = new StringBuilder();
        String tempText = text;
        char lastChar = ' ';
        if (text.endsWith(".") || text.endsWith(":") || text.endsWith(",")) {
            tempText = text.substring(0, text.length() - 1);
            lastChar = text.charAt(text.length() - 1);
        }
        link.append("<a href = \"")
                .append(tempText)
                .append("\">")
                .append(tempText)
                .append("</a>")
                .append(lastChar);
        return link.toString();
    }

    /**
     * Used in #yangtemplateformodule.scala.txt for formating revision description
     *
     * @param text Content of tag description
     * @param nextLineIndent Number of spaces from left side default is 12
     * @return formatted description
     */
    public static String formatToParagraph(final String text, final int nextLineIndent) {
        if (Strings.isNullOrEmpty(text)) {
            return "";
        }
        boolean isFirstElementOnNewLineEmptyChar = false;
        final StringBuilder sb = new StringBuilder();
        final StringBuilder lineBuilder = new StringBuilder();
        final String lineIndent = Strings.repeat(" ", nextLineIndent);
        final String textToFormat = NEWLINE_OR_TAB.removeFrom(text);
        final String formattedText = textToFormat.replaceAll(" +", " ");
        final StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        while (tokenizer.hasMoreElements()) {
            final String nextElement = tokenizer.nextElement().toString();

            if (lineBuilder.length() + nextElement.length() > 80) {
                // Trim trailing whitespace
                for (int i = lineBuilder.length() - 1; i >= 0 && lineBuilder.charAt(i) != ' '; --i) {
                    lineBuilder.setLength(i);
                }
                // Trim leading whitespace
                while (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.deleteCharAt(0);
                }
                sb.append(lineBuilder).append('\n');
                lineBuilder.setLength(0);

                if (nextLineIndent > 0) {
                    sb.append(lineIndent);
                }

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = true;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = false;
            } else {
                lineBuilder.append(nextElement);
            }
        }
        return sb.append(lineBuilder).append('\n').toString();
    }

    /**
     * util method used in interfaceTemplate
     *
     * @param parameters list of parameters
     * @return list of parameters separated with ","
     */
    public static String generateImports(List<Type> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!isEmpty(parameters)) {
            for (Type parameter : parameters) {
                strings.add(importedName(parameter));
            }
        }
        return String.join(",", strings);
    }

    private static boolean isEmpty(Iterable<?> iterable) {
        return iterable instanceof Collection ?((Collection)iterable).isEmpty():!iterable.iterator().hasNext();
    }

    public String generateToString(Collection<GeneratedProperty> properties) {
        // transfered to template as showcase
        return null;
    }

    /**
     * util method used in interfaceTemplate
     *
     * @param maybeGetter
     * @return
     */
    public static boolean isAccessor(MethodSignature maybeGetter) {
        return maybeGetter.getName().startsWith("is") || maybeGetter.getName().startsWith("get");
    }

    public Restrictions getRestrictions(Type type) {
        Restrictions restrictions = null;
        if (type instanceof ConcreteType) {
            restrictions = ((ConcreteType)type).getRestrictions();
        } else if (type instanceof GeneratedTransferObject) {
            restrictions = ((GeneratedTransferObject)type).getRestrictions();
        }
        return restrictions;
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     * InterfaceTemplate
     *
     * @param parameters list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    public static String generateParameters(List<MethodSignature.Parameter> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!isEmpty(parameters)) {
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
     * util method used in interfaceTemplate
     *
     * @param parameters list with annotationType parameters
     * @return string with parameter name and parameter value
     */
    public static String generateParametersForAnnotation(List<AnnotationType.Parameter> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!isEmpty(parameters)) {
            for (AnnotationType.Parameter parameter : parameters) {
                final StringBuilder parameterWithType = new StringBuilder();
                parameterWithType.append(parameter.getName());
                parameterWithType.append("=");
                parameterWithType.append(parameter.getValues());
                strings.add(parameterWithType.toString());
            }
        }
        return String.join(",", strings);
    }

    public GeneratedProperty findProperty(GeneratedTransferObject gto, String name) {
        for (GeneratedProperty generatedProperty : gto.getProperties()) {
            if (generatedProperty.getName().equals(name)) {
                return generatedProperty;
            }
        }
        final GeneratedTransferObject parent = gto.getSuperType();
        if (parent != null) {
            return findProperty(parent, name);
        }
        return null;
    }

    /**
     * util method used in interfaceTemplate
     *
     * @param constant
     * @return string with constant wrapped in code
     */
    public static String emitConstant(Constant constant) {
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
        constantBuilder.append(";");
        return constantBuilder.toString();
    }

    public String addSeparator(List<Type> types) {
        final List<String> strings = new LinkedList<>();
        for (Type parameter : types) {
            final StringBuilder parameterWithType = new StringBuilder();
            parameterWithType.append(importedName(parameter));
            parameterWithType.append(" ");
            strings.add(parameterWithType.toString());
        }
        return String.join(", ", strings);
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
    public static String getExplicitType(final GeneratedType parentGenType, final Type type,
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
    private static StringBuilder addActualTypeParameters(final StringBuilder builder, final Type type,
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
    private static String getParameters(final GeneratedType parentGenType, final Type[] pTypes,
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

    public static String getSimpleNameForBuilder() {
        return Builder.class.getSimpleName();
    }

    public static String encodeJavadocSymbols(String description) {
        if (description == null || description.isEmpty()) {
            return description;
        }
        final String ret = description.replace("*/", "&#42;&#47;");
        return AMP_MATCHER.replaceFrom(ret, "&amp;");
    }


    /**
     * @param enumeration
     * @return List of enumeration pairs with javadoc
     */
    public static String writeEnumeration(final Enumeration enumeration) {
        final List<String> strings = new LinkedList<>();
        if (!isEmpty(enumeration.getValues())) {
            for (Enumeration.Pair pair : enumeration.getValues()) {
                final StringBuilder parameterWithType = new StringBuilder();
                parameterWithType.append(asJavadoc(encodeAngleBrackets(pair.getDescription())));
                parameterWithType.append("\n");
                parameterWithType.append(pair.getMappedName());
                parameterWithType.append("(");
                parameterWithType.append(pair.getValue());
                parameterWithType.append(", \"");
                parameterWithType.append(pair.getName());
                parameterWithType.append("\")");
                strings.add(parameterWithType.toString());
            }
        }
        return String.join(",\n", strings).concat(";");
    }

    /**
     * Encodes angle brackets in yang statement description
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(String description) {
        if (description != null) {
            description = LT_MATCHER.replaceFrom(description, "&lt;");
            description = GT_MATCHER.replaceFrom(description, "&gt;");
        }
        return description;
    }
}