/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.BOOLEAN;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.unionTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

public class UnionRenderer extends ClassRenderer {
    public UnionRenderer(final GeneratedTransferObject type) {
        super(type);
    }

    @Override
    protected String generateConstructors() {
        // list of all imported names for template
        final Map<String, String> importedNames = new HashMap<>();

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
        final String name = TextTemplateUtil.fieldName(generatedProperty);
        sb.append("this.")
            .append(name)
            .append(" = source.")
            .append(name);
        if (!"value".equals(name) && importedName(generatedProperty.getReturnType()).contains("[]")) {
            sb.append(" == null ? null : source.")
                .append(name)
                .append(".clone()");
        }
        sb.append(";\n");
        return sb.toString();
    }

    @Override
    protected String getterMethod(final GeneratedProperty field) {
        if (!"value".equals(field.getName())) {
            return super.getterMethod(field);
        }

        final StringBuilder sb1 = new StringBuilder();
        final String name = TextTemplateUtil.fieldName(field);
        final String importedName = Preconditions.checkNotNull(importedName(field.getReturnType()));
        sb1.append("public ")
                .append(importedName)
                .append(' ')
                .append(TextTemplateUtil.getterMethodName(field))
                .append("() {\n");

        Predicate<GeneratedProperty> predicate = input -> !"value".equals(input.getName());
        final List<GeneratedProperty> filtered = new ArrayList<>(Collections2.filter(this.getFinalProperties(),
            predicate));

        final List<CharSequence> strings = new ArrayList<>(filtered.size());

        for (GeneratedProperty property : filtered) {
            final Type propertyReturnType = property.getReturnType();
            //string builder for current property
            final StringBuilder sb = new StringBuilder();
            sb.append("if (")
                .append(TextTemplateUtil.fieldName(property))
                .append(" != null) {")
                .append(TextTemplateUtil.fieldName(field))
                .append(" = ");

            // generated type String
            if ("java.lang.String".equals(propertyReturnType.getFullyQualifiedName())) {
                sb.append(TextTemplateUtil.fieldName(property)).append(".toCharArray();");
            // generated type InstanceIdentifier
            } else if ("org.opendaylight.mdsal.binding2.spec.base.InstanceIdentifier".equals(propertyReturnType
                    .getFullyQualifiedName())) {
                sb.append(TextTemplateUtil.fieldName(field))
                    .append(" = ")
                    .append(TextTemplateUtil.fieldName(property))
                    .append(".toString().toCharArray();");
            //generated type binary
            } else if ("byte[]".equals(propertyReturnType.getName())) {
                sb.append("new ")
                    .append(importedName(String.class))
                    .append('(')
                    .append(TextTemplateUtil.fieldName(property))
                    .append(").toCharArray();");
            //generated type int*, uint, decimal64 or enumeration*
            } else if (propertyReturnType.getFullyQualifiedName().startsWith("java.lang") ||
                    propertyReturnType instanceof Enumeration ||
                    propertyReturnType.getFullyQualifiedName().startsWith("java.math")) {
                sb.append(TextTemplateUtil.fieldName(property)).append(".toString().toCharArray();");

            } else if (propertyReturnType instanceof GeneratedTransferObject) {
                final GeneratedTransferObject propRetTypeCast = (GeneratedTransferObject) propertyReturnType;
                final List<GeneratedProperty> retTypeCastProperties = propRetTypeCast.getProperties();

                // generated union type
                if (propRetTypeCast.isUnionType()) {
                    sb.append(TextTemplateUtil.fieldName(property)).append(".getValue();");

                // generated boolean typedef
                } else if (propRetTypeCast.isTypedef() && retTypeCastProperties != null &&
                        !retTypeCastProperties.isEmpty() && retTypeCastProperties.size() == 1 &&
                        retTypeCastProperties.get(0).getName().equals("value") &&
                        BOOLEAN.equals(retTypeCastProperties.get(0).getReturnType())) {
                    sb.append(TextTemplateUtil.fieldName(property)).append(".isValue().toString().toCharArray();");

                //generated byte[] typedef
                } else if (propRetTypeCast.isTypedef() && retTypeCastProperties != null &&
                        !retTypeCastProperties.isEmpty() && retTypeCastProperties.size() == 1 &&
                        retTypeCastProperties.get(0).getName().equals("value") &&
                        "byte[]".equals(retTypeCastProperties.get(0).getReturnType().getName())) {
                    sb.append("BaseEncoding.base64().encode(").append(TextTemplateUtil.fieldName(property))
                        .append(".getValue()).toCharArray();");
                }
            } else {
                sb.append(TextTemplateUtil.fieldName(property))
                        .append(".getValue().toString().toCharArray();");
            }
            sb.append("}");
            strings.add(sb);
        }

        sb1.append(String.join(" else ", strings))
                .append("\n");

        sb1.append("return ")
                .append(name);
        if (importedName.contains("[]")) {
            sb1.append(" == null ? null : ")
                    .append(name)
                    .append(".clone()");
        }
        sb1.append(";\n}\n");

        return sb1.toString();
    }

    @Override
    protected String generateInnerClassBody(GeneratedTransferObject innerClass) {
        return new UnionRenderer(innerClass).generateAsInnerClass();
    }
}