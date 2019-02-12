/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTypeForBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.TypeComment;
import org.opendaylight.mdsal.binding.javav2.model.api.TypeMember;
import org.opendaylight.mdsal.binding.javav2.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.javav2.model.api.YangSourceDefinition.Multiple;
import org.opendaylight.mdsal.binding.javav2.model.api.YangSourceDefinition.Single;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter;

public final class TextTemplateUtil {

    public static final String DOT = ".";

    private static final char NEW_LINE = '\n';
    private static final String UNDERSCORE = "_";
    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");
    private static final CharMatcher NL_MATCHER = CharMatcher.is(NEW_LINE);
    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');
    private static final Splitter NL_SPLITTER = Splitter.on(NL_MATCHER);
    private static final Splitter BSDOT_SPLITTER = Splitter.on(".");

    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile(" +");

    private static DeclaredStatementFormatter YANG_FORMATTER = DeclaredStatementFormatter.builder()
            .addIgnoredStatement(YangStmtMapping.CONTACT)
            .addIgnoredStatement(YangStmtMapping.DESCRIPTION)
            .addIgnoredStatement(YangStmtMapping.REFERENCE)
            .addIgnoredStatement(YangStmtMapping.ORGANIZATION)
            .build();

    private TextTemplateUtil() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Makes start of getter name LowerCase.
     *
     * @param str getter name without prefix
     * @return getter name starting in LowerCase
     */
    public static String toFirstLower(final String str) {
        return str != null && str.length() != 0 ? Character.isLowerCase(str.charAt(0)) ? str : str.length() == 1
                ? str.toLowerCase() : str.substring(0, 1).toLowerCase() + str.substring(1) : str;
    }

    /**
     * Wraps text as documentation, used in enum description.
     *
     * @param text text for wrapping
     * @return wrapped text
     */
    public static String wrapToDocumentation(final String text) {
        if (text.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("/**");
        sb.append(NEW_LINE);
        for (final String t : NL_SPLITTER.split(text)) {
            if (!t.isEmpty()) {
                sb.append(" * ");
                sb.append(t);
                sb.append(NEW_LINE);
            }
        }
        sb.append(" */");
        sb.append(NEW_LINE);
        return sb.toString();
    }

    /**
     * Returns formatted Javadoc, based on type.
     *
     * @param typeName given type name
     * @return formatted Javadoc, based on type
     */
    public static String formatDataForJavaDocBuilder(final String typeName) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Class that builds {@link ")
                .append(typeName)
                .append("} instances.")
                .append(NEW_LINE)
                .append("@see ")
                .append(typeName);
        return stringBuilder.toString();
    }

    /**
     * Returns formatted Javadoc with possible additional comment, based on type.
     *
     * @param type              given type
     * @param additionalComment additional comment to format
     * @return formatted Javadoc with possible additional comment, based on type
     */
    public static String formatDataForJavaDoc(final GeneratedType type, final String additionalComment) {
        final StringBuilder javaDoc = new StringBuilder();
        javaDoc.append(formatDataForJavaDoc(type)).append(additionalComment);
        return javaDoc.toString();
    }

    /**
     * Returns formatted type description.
     *
     * @param type given type
     * @return formatted type description
     */
    public static String formatDataForJavaDoc(final GeneratedType type) {
        final StringBuilder javaDoc = new StringBuilder();
        final TypeComment comment = type.getComment();
        if (comment != null) {
            javaDoc.append(comment.getJavadoc())
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }

        appendSnippet(javaDoc, type);

        return javaDoc.toString();
    }

    private static String formatDataForJavaDoc(final TypeMember type, final String additionalComment) {
        final StringBuilder javaDoc = new StringBuilder();
        if (type.getComment() != null && !type.getComment().isEmpty()) {
            javaDoc.append(formatToParagraph(type.getComment(), 0))
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }
        javaDoc.append(additionalComment);
        return wrapToDocumentation(javaDoc.toString());
    }

    private static void appendSnippet(final StringBuilder sb, final GeneratedType type) {
        Optional<YangSourceDefinition> optDef = type.getYangSourceDefinition();
        if (optDef.isPresent()) {
            YangSourceDefinition def = optDef.get();
            sb.append(NEW_LINE);

            if (def instanceof Single) {
                DocumentedNode node = ((Single) def).getNode();
                sb.append("<p>\n")
                        .append("This class represents the following YANG schema fragment defined in module <b>")
                        .append(def.getModule().argument()).append("</b>\n")
                        .append("<pre>\n");
                appendYangSnippet(sb, def.getModule(), ((EffectiveStatement<?, ?>) node).getDeclared());
                sb.append("</pre>");

                if (node instanceof SchemaNode) {
                    sb.append("The schema path to identify an instance is\n")
                            .append("<i>")
                            .append(formatSchemaPath(def.getModule().argument(), ((SchemaNode) node).getPath()
                                    .getPathFromRoot()))
                            .append("</i>\n");

                    if (hasBuilderClass(type)) {
                        final String builderName = new StringBuilder()
                                .append(((GeneratedTypeForBuilder) type).getPackageNameForBuilder())
                                .append(".").append(type.getName()).append("Builder").toString();

                        sb.append("\n<p>To create instances of this class use {@link ").append(builderName)
                                .append("}.\n")
                                .append("@see ").append(builderName).append('\n');
                        if (node instanceof ListSchemaNode) {
                            final StringBuilder linkToKeyClass = new StringBuilder();

                            final String[] namespace = Iterables.toArray(
                                    BSDOT_SPLITTER.split(type.getFullyQualifiedName()), String.class);
                            final String className = namespace[namespace.length - 1];

                            linkToKeyClass.append(BindingGeneratorUtil.packageNameForSubGeneratedType(
                                    ((GeneratedTypeForBuilder) type).getBasePackageName(), (SchemaNode) node,
                                    BindingNamespaceType.Key)).append('.').append(className).append("Key");

                            List<QName> keyDef = ((ListSchemaNode) node).getKeyDefinition();
                            if (keyDef != null && !keyDef.isEmpty()) {
                                sb.append("@see ").append(linkToKeyClass);
                            }
                            sb.append('\n');
                        }
                    }
                }
            } else if (def instanceof Multiple) {
                sb.append("<pre>\n");
                for (SchemaNode node : ((Multiple) def).getNodes()) {
                    appendYangSnippet(sb, def.getModule(), ((EffectiveStatement<?, ?>) node).getDeclared());
                }
                sb.append("</pre>\n");
            }
        }
    }

    private static void appendYangSnippet(StringBuilder sb, ModuleEffectiveStatement module,
                                          DeclaredStatement<?> stmt) {
        for (String str : YANG_FORMATTER.toYangTextSnippet(module, stmt)) {
            sb.append(encodeAngleBrackets(encodeJavadocSymbols(str)));
        }
    }

    public static boolean hasBuilderClass(final GeneratedType type) {
        return type instanceof GeneratedTypeForBuilder;
    }

    public static String formatSchemaPath(final String moduleName, final Iterable<QName> schemaPath) {
        final StringBuilder sb = new StringBuilder();
        sb.append(moduleName);

        QName currentElement = Iterables.getFirst(schemaPath, null);
        for (final QName pathElement : schemaPath) {
            sb.append('/');
            if (!currentElement.getNamespace().equals(pathElement.getNamespace())) {
                currentElement = pathElement;
                sb.append(pathElement);
            } else {
                sb.append(pathElement.getLocalName());
            }
        }
        return sb.toString();
    }

    /**
     * Returns properties names in formatted string.
     *
     * @param properties list of given properties
     * @return properties names in formatted string
     */
    //FIXME: this needs further clarification in future patch
    public static String valueForBits(final List<GeneratedProperty> properties) {
        return String.join(",", Lists.transform(properties, TextTemplateUtil::fieldName));
    }

    /**
     * Returns parameter name, based on given Type.
     *
     * @param returnType given type
     * @param paramName  parameter name
     * @return parameter name, based on given Type
     */
    public static String paramValue(final Type returnType, final String paramName) {
        return returnType instanceof ConcreteType ? paramName : paramName + ".getValue()";
    }

    /**
     * Template method which generates JAVA comments. InterfaceTemplate
     *
     * @param comment comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    public static String asJavadoc(final String comment) {
        return comment == null ? "" : wrapToDocumentation(formatToParagraph(comment.trim(), 0));
    }

    /**
     * Returns related Javadoc.
     *
     * @param methodSignature method signature
     * @return related Javadoc
     */
    public static String getJavaDocForInterface(final MethodSignature methodSignature) {
        if (methodSignature.getReturnType() == Types.VOID) {
            return "";
        }
        final StringBuilder javaDoc = new StringBuilder();
        javaDoc.append("@return ")
                .append(asCode(methodSignature.getReturnType().getFullyQualifiedName()))
                .append(" ")
                .append(asCode(propertyNameFromGetter(methodSignature)))
                .append(", or ")
                .append(asCode("null"))
                .append(" if not present");
        return formatDataForJavaDoc(methodSignature, javaDoc.toString());
    }

    private static String asCode(final String text) {
        return "<code>" + text + "</code>";
    }

    /**
     * Encodes angle brackets in yang statement description.
     *
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

    /**
     * Returns collection of properties as formatted String.
     *
     * @param properties list of given properties
     * @return generated properties as formatted String
     */
    public static String propsAsArgs(final Iterable<GeneratedProperty> properties) {
        return String.join(",", Iterables.transform(properties, prop -> "\"" + prop.getName() + "\""));
    }

    /**
     * Returns properties as formatted String.
     *
     * @param properties  list of given properties
     * @param booleanName Java Boolean type name
     * @return Properties as formatted String
     */
    public static String propsAsList(final Iterable<GeneratedProperty> properties, final String booleanName) {
        return String.join(",", Iterables.transform(properties,
            prop -> "properties.get(i++).equals(defaultValue) ? " + booleanName + ".TRUE : null"));
    }

    /**
     * Extracts available restrictions from given type.
     *
     * @param currentType given type
     * @return restrictions from given type
     */
    public static Restrictions getRestrictions(final Type currentType) {
        Restrictions restrictions = null;
        if (currentType instanceof ConcreteType) {
            restrictions = ((ConcreteType) currentType).getRestrictions();
        } else if (currentType instanceof GeneratedTransferObject) {
            restrictions = ((GeneratedTransferObject) currentType).getRestrictions();
        }
        return restrictions;
    }

    /**
     * sets fieldname according to property for return type.
     * method(type fieldname)
     *
     * @param property type from getter
     * @return underscored string form
     */
    public static String fieldName(final GeneratedProperty property) {
        final String name = requireNonNull(property.getName());
        return UNDERSCORE.concat(name);
    }

    /**
     * Template method which generates sequence of the names of the class attributes.
     *
     * @param parameters group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names
     */
    public static String asArguments(final List<GeneratedProperty> parameters) {
        return String.join(", ", Lists.transform(parameters, TextTemplateUtil::fieldName));
    }

    /**
     * Helper method for building getter.
     *
     * @param field property name
     * @return getter for property
     */
    public static String getterMethodName(final GeneratedProperty field) {
        final Type type = requireNonNull(field.getReturnType());
        final String name = requireNonNull(field.getName());
        final String prefix = Types.BOOLEAN.equals(type) ? "is" : "get";
        return prefix.concat(toFirstUpper(name));
    }

    /**
     * Returns built setter method body from input parameters.
     *
     * @param field          generated property
     * @param typeName       type name
     * @param returnTypeName return type name
     * @return built setter method body
     */
    public static String setterMethod(final GeneratedProperty field, final String typeName, final String
            returnTypeName) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public ")
                .append(typeName)
                .append(" set")
                .append(toFirstUpper(field.getName()))
                .append('(')
                .append(returnTypeName)
                .append(" value) {\n    this.")
                .append(fieldName(field))
                .append(" = value;\n    return this;\n}\n");
        return stringBuilder.toString();
    }

    /**
     * Returns simple name of underlying class.
     *
     * @return Simple name of underlying class
     */
    public static String getSimpleNameForBuilder() {
        return Builder.class.getSimpleName();
    }

    /**
     * Makes start of getter name uppercase.
     *
     * @param str getter name without prefix
     * @return getter name starting in uppercase
     */
    public static String toFirstUpper(final String str) {
        return str != null && str.length() != 0 ? Character.isUpperCase(str.charAt(0)) ? str : str.length() == 1
                ? str.toUpperCase() : str.substring(0, 1).toUpperCase() + str.substring(1) : str;
    }

    /**
     * Cuts prefix from getter name.
     *
     * @param getter getter name
     * @return getter name without prefix
     */
    public static String propertyNameFromGetter(final MethodSignature getter) {
        final String name = requireNonNull(getter.getName());
        int prefix;
        if (name.startsWith("is")) {
            prefix = 2;
        } else if (name.startsWith("get")) {
            prefix = 3;
        } else {
            prefix = 0;
        }
        return toFirstLower(name.substring(prefix));
    }

    /**
     * Returns list of properties as formatted String.
     *
     * @param properties input list of generated properties
     * @return formatted property list as String
     */
    public static String getPropertyList(final List<GeneratedProperty> properties) {
        return String.join(", ", Lists.transform(properties, prop -> "base." + getterMethodName(prop) + "()"));
    }

    /**
     * util method for unionTemplateBuilderTemplate.
     *
     * @return string with clarification for javadoc
     */
    public static String getClarification() {
        final StringBuilder clarification = new StringBuilder();
        clarification.append("The purpose of generated class in src/main/java for Union types is to create new "
                + "instances of unions from a string representation.\n")
                .append("In some cases it is very difficult to automate it since there can be unions such as (uint32 "
                        + "- uint16), or (string - uint32).\n")
                .append("\n")
                .append("The reason behind putting it under src/main/java is:\n")
                .append("This class is generated in form of a stub and needs to be finished by the user. This class "
                        + "is generated only once to prevent\n")
                .append("loss of user code.\n")
                .append("\n");
        return clarification.toString();
    }

    /**
     * Returns source path as String.
     *
     * @param module                 module
     * @param moduleFilePathResolver function module to module file path
     * @return formatted String source path
     */
    public static String getSourcePath(final Module module, final Function<Module, Optional<String>>
            moduleFilePathResolver) {
        final Optional<String> moduleFilePath = moduleFilePathResolver.apply(module);
        Preconditions.checkArgument(moduleFilePath.isPresent(), "Module file path for %s is not present", module);

        return moduleFilePath.get();
    }

    /**
     * Util method for unionTemplateBuilderTemplate.
     *
     * @param modifier enum representing Java access modifier
     * @return needed access modifier
     */
    public static String getAccessModifier(final AccessModifier modifier) {
        switch (modifier) {
            case PUBLIC:
                return "public ";
            case PROTECTED:
                return "protected ";
            case PRIVATE:
                return "private ";
            default:
                return "";
        }
    }

    /**
     * Return formatted description.
     * @param text           Content of tag description
     * @param nextLineIndent Number of spaces from left side default is 12
     * @return formatted description
     */
    private static String formatToParagraph(final String text, final int nextLineIndent) {
        if (Strings.isNullOrEmpty(text)) {
            return "";
        }
        boolean isFirstElementOnNewLineEmptyChar = false;
        final StringBuilder sb = new StringBuilder();
        final StringBuilder lineBuilder = new StringBuilder();
        final String lineIndent = Strings.repeat(" ", nextLineIndent);
        final String textToFormat = NEWLINE_OR_TAB.removeFrom(encodeJavadocSymbols(text));
        final String formattedText = MULTIPLE_SPACES_PATTERN.matcher(textToFormat).replaceAll(" ");
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

    private static String encodeJavadocSymbols(final String description) {
        return Strings.isNullOrEmpty(description) ? description
                : AMP_MATCHER.replaceFrom(TAIL_COMMENT_PATTERN.matcher(description).replaceAll("&#42;&#47;"), "&amp;");
    }
}
