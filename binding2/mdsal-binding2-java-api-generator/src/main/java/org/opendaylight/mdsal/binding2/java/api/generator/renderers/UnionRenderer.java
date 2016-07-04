/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding2.generator.util.Types.BOOLEAN;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.fieldName;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.txt.unionTemplate;

public class UnionRenderer extends ClassRenderer {
    /**
     * list of all imported names for template
     */
    private final Map<String, String> importedNames = new HashMap<>();

    public UnionRenderer(final GeneratedTransferObject type) {
        super(type);
    }

    protected String body() {
        if(isBaseEncodingImportRequired()) {
            importMap.put("BaseEncoding","com.google.common.io");
        }
        for (GeneratedProperty finalProperty : finalProperties) {
            importedNames.put("constructorProperties", importedName(ConstructorProperties.class));
            importedNames.put("string", importedName(String.class));
            importedNames.put(finalProperty.getName(), importedName(finalProperty.getReturnType()));
        }

        final StringBuilder propertyList = new StringBuilder();
        if (!properties.isEmpty()) {
            for (GeneratedProperty property : properties) {
                propertyList.append(generateField(property));
            }
        }
        return unionTemplate.render(type, importedNames, finalProperties, parentProperties, properties, propertyList.toString())
                .body();
    }

    private boolean isBaseEncodingImportRequired() {
        for (GeneratedProperty property : finalProperties) {
            final Type returnType = property.getReturnType();
            if (returnType instanceof GeneratedTransferObject) {
                final GeneratedTransferObject returnTypeGto = (GeneratedTransferObject)returnType;
                if (returnTypeGto.isTypedef() && returnTypeGto.getProperties() != null &&
                        !returnTypeGto.getProperties().isEmpty() && returnTypeGto.getProperties().size() == 1 &&
                        "value".equals(returnTypeGto.getProperties().get(0).getName()) &&
                        "byte[]".equals(returnTypeGto.getProperties().get(0).getReturnType().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String generateField(GeneratedProperty generatedProperty) {
        final StringBuilder fieldName = new StringBuilder();
        final String name = fieldName(generatedProperty);
        fieldName.append("this.")
                .append(name)
                .append(" = source.")
                .append(name);
        if (!"value".equals(name) && importedName(generatedProperty.getReturnType()).contains("[]")) {
            fieldName.append(" == null ? null : source._")
                    .append(name)
                    .append(".clone()");
        }
        fieldName.append(";");
        return fieldName.toString();
    }

//    TO DO Implement after class renderer
    private String generateGetterHelper(GeneratedProperty field) {
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