/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.java.api.generator.util;

import static org.opendaylight.mdsal.binding2.generator.util.Types.BOOLEAN;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.Type;

public final class UnionTemplateUtil extends ClassTemplateUtil{
    private UnionTemplateUtil() {
        throw new UnsupportedOperationException("Util class");
    }

    public static String getClarification() {
        final StringBuilder clarification = new StringBuilder();
        clarification.append("The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.\n ")
                .append("In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).\n")
                .append("\n")
                .append("The reason behind putting it under src/main/java is:\n")
                .append("This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent\n")
                .append("loss of user code.\n")
                .append(";\n");
        return clarification.toString();
    }

    public static String getAccessModifier(AccessModifier modifier) {
        switch (modifier) {
            case PUBLIC: return "public ";
            case PROTECTED: return "protected ";
            case PRIVATE: return "private ";
            case DEFAULT: return "";
        }
        return "";
    }

    public static String generateField(GeneratedProperty generatedProperty) {
        final StringBuilder fieldName = new StringBuilder();
        fieldName.append("this._")
                .append(generatedProperty.getName())
                .append(" = source._")
                .append(generatedProperty.getName());
        if (!"value".equals(generatedProperty.getName()) &&
                importedName(generatedProperty.getReturnType()).contains("[]")) {
            fieldName.append(" == null ? null : source._")
                    .append(generatedProperty.getName())
                    .append(".clone()");
        }
        fieldName.append(";");
        return fieldName.toString();
    }

    private static boolean isBaseEncodingImportRequired() {
        for (GeneratedProperty property : finalProperties) {
            final Type returnType = property.getReturnType();
            if (returnType instanceof GeneratedTransferObject) {
                final GeneratedTransferObject returnTypeGto = (GeneratedTransferObject)returnType;
                if (returnTypeGto.isTypedef() && returnTypeGto.getProperties() != null &&
                        !returnTypeGto.getProperties().isEmpty() && (returnTypeGto.getProperties().size() == 1) &&
                        returnTypeGto.getProperties().get(0).getName().equals("value") &&
                        "byte[]".equals(returnTypeGto.getProperties().get(0).getReturnType().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isReadOnly(GeneratedProperty field) {
        return !"value".equals(field.getName()) && (field.isReadOnly());
    }

    public static boolean isParentProperties() {
        return parentProperties.isEmpty();
    }

    public static boolean isProperties() {
        return properties.isEmpty();
    }

    public static List<GeneratedProperty> getProperties() {
        return properties;
    }

    public static List<GeneratedProperty> getParentProperties() {
        return parentProperties;
    }

    public static List<GeneratedProperty> getFinalProperties() {
        return finalProperties;
    }

    public static String generateGetterHelper(GeneratedProperty field) {
        final List<String> strings = new LinkedList<>();
        /*FIX ME finalProperties should be sorted */
        for (GeneratedProperty property : finalProperties) {
            final Type propertyReturnType = property.getReturnType();
            final StringBuilder currentProperty = new StringBuilder();
            currentProperty.append("if (")
                    .append(fieldName(property))
                    .append(" != null) {")
                    .append(fieldName(field))
                    .append(" = ");
            if ("java.lang.String".equals(propertyReturnType.getFullyQualifiedName())) {
                currentProperty.append(fieldName(property))
                        .append(".toCharArray();");
            } else if ("byte[]".equals(propertyReturnType.getName())) {
                currentProperty.append("new ")
                        .append(importedName(String.class))
                        .append("(")
                        .append(fieldName(property))
                        .append(").toCharArray();");
            } else if (propertyReturnType.getFullyQualifiedName().startsWith("java.lang") ||
                    propertyReturnType instanceof Enumeration ||
                    propertyReturnType.getFullyQualifiedName().startsWith("java.math")) {
                currentProperty.append(fieldName(property))
                        .append(".toString().toCharArray();");
            } else if (propertyReturnType instanceof GeneratedTransferObject &&
                    ((GeneratedTransferObject)propertyReturnType).isUnionType()) {
                currentProperty.append(fieldName(property))
                        .append(".getValue();");
            } else if (propertyReturnType instanceof GeneratedTransferObject &&
                    ((GeneratedTransferObject)propertyReturnType).isTypedef() &&
                    ((GeneratedTransferObject)propertyReturnType).getProperties() != null &&
                    ((GeneratedTransferObject)propertyReturnType).getProperties().isEmpty() &&
                    (((GeneratedTransferObject)propertyReturnType).getProperties().size() == 1) &&
                    ((GeneratedTransferObject)propertyReturnType).getProperties().get(0).getName().equals("value") &&
                    BOOLEAN.equals(((GeneratedTransferObject)propertyReturnType).getProperties().get(0).getReturnType())) {
                currentProperty.append(fieldName(property))
                        .append(".isValue().toString().toCharArray();");
            } else if (propertyReturnType instanceof GeneratedTransferObject &&
                    ((GeneratedTransferObject)propertyReturnType).isTypedef() &&
                    ((GeneratedTransferObject)propertyReturnType).getProperties() != null &&
                    ((GeneratedTransferObject)propertyReturnType).getProperties().isEmpty() &&
                    (((GeneratedTransferObject)propertyReturnType).getProperties().size() == 1) &&
                    ((GeneratedTransferObject)propertyReturnType).getProperties().get(0).getName().equals("value") &&
                    "byte[]".equals(((GeneratedTransferObject)propertyReturnType).getProperties().get(0).getReturnType().getName())) {
                currentProperty.append("BaseEncoding.base64().encode(")
                        .append(fieldName(property))
                        .append(".getValue()).toCharArray();");
            } else {
                currentProperty.append(fieldName(property))
                        .append(".getValue().toString().toCharArray();");
            }
            currentProperty.append("}");
            strings.add(currentProperty.toString());
        }
        return String.join(" else ", strings);
    }
}