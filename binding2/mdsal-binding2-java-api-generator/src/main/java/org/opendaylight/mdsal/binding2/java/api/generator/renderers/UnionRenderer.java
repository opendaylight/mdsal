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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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

    @Override
    protected String generateConstructors() {
        if(isBaseEncodingImportRequired()) {
            this.putToImportMap("BaseEncoding","com.google.common.io");
        }
        for (GeneratedProperty finalProperty : getFinalProperties()) {
            importedNames.put("constructorProperties", importedName(ConstructorProperties.class));
            importedNames.put("string", importedName(String.class));
            importedNames.put(finalProperty.getName(), importedName(finalProperty.getReturnType()));
        }

        final StringBuilder sb = new StringBuilder();
        if (!getProperties().isEmpty()) {
            for (GeneratedProperty property : getProperties()) {
                sb.append(generateField(property));
            }
        }

        if (getProperties().isEmpty() && !getParentProperties().isEmpty()) {
            importedNames.put("superType", importedName(genTO.getSuperType()));
        }

        for (GeneratedProperty parentProperty : getParentProperties()) {
            importedNames.put(parentProperty.getName(), importedName(parentProperty.getReturnType()));
        }

        return unionTemplate.render(getType(), importedNames, getFinalProperties(), getParentProperties(),
                getProperties(), sb.toString()).body();
    }

    private boolean isBaseEncodingImportRequired() {
        for (GeneratedProperty property : getFinalProperties()) {
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

    private String generateField(final GeneratedProperty generatedProperty) {
        final StringBuilder sb = new StringBuilder();
        final String name = fieldName(generatedProperty);
        sb.append("this.")
            .append(name)
            .append(" = source.")
            .append(name);
        if (!"value".equals(name) && importedName(generatedProperty.getReturnType()).contains("[]")) {
            sb.append(" == null ? null : source._")
                .append(name)
                .append(".clone()");
        }
        sb.append(';');
        return sb.toString();
    }

    @Override
    protected String getterMethod(final GeneratedProperty field) {
        if (!"value".equals(field.getName())) {
            return super.getterMethod(field);
        }

        final List<CharSequence> strings = new LinkedList<>();
        final Function<GeneratedProperty, Boolean> tempFunction = (GeneratedProperty p) -> {
            String name = p.getName();
            return !"value".equals(name);
        };
        Iterable<GeneratedProperty> filtered = Iterables.<GeneratedProperty>filter(this.getFinalProperties(),
                (Predicate<? super GeneratedProperty>) tempFunction);

        for (GeneratedProperty property : filtered) {
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
                    .append('(')
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
                    ((GeneratedTransferObject) propertyReturnType).isTypedef() &&
                    ((GeneratedTransferObject) propertyReturnType).getProperties() != null &&
                    ((GeneratedTransferObject) propertyReturnType).getProperties().isEmpty() &&
                    (((GeneratedTransferObject) propertyReturnType).getProperties().size() == 1) &&
                    ((GeneratedTransferObject) propertyReturnType).getProperties().get(0).getName().equals("value") &&
                    BOOLEAN.equals(((GeneratedTransferObject) propertyReturnType).getProperties().get(0)
                            .getReturnType())) {
                currentProperty.append(fieldName(property))
                        .append(".isValue().toString().toCharArray();");
            } else if (propertyReturnType instanceof GeneratedTransferObject &&
                    ((GeneratedTransferObject) propertyReturnType).isTypedef() &&
                    ((GeneratedTransferObject) propertyReturnType).getProperties() != null &&
                    ((GeneratedTransferObject) propertyReturnType).getProperties().isEmpty() &&
                    (((GeneratedTransferObject) propertyReturnType).getProperties().size() == 1) &&
                    ((GeneratedTransferObject) propertyReturnType).getProperties().get(0).getName().equals("value") &&
                    "byte[]".equals(((GeneratedTransferObject) propertyReturnType).getProperties().get(0)
                            .getReturnType().getName())) {
                currentProperty.append("BaseEncoding.base64().encode(")
                        .append(fieldName(property))
                        .append(".getValue()).toCharArray();");
            } else {
                currentProperty.append(fieldName(property))
                        .append(".getValue().toString().toCharArray();");
            }
            currentProperty.append("}");
            strings.add(currentProperty);
        }
        return String.join(" else ", strings);
    }
}