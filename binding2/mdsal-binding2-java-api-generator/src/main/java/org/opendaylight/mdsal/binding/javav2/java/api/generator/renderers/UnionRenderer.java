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
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

public class UnionRenderer extends ClassRenderer {
    public UnionRenderer(final GeneratedTransferObject type) {
        super(type);
    }

    @Override
    protected String generateConstructors() {
        // list of all imported names for template
        final Map<String, String> importedNames = new HashMap<>();

        if (isBaseEncodingImportRequired()) {
            this.putToImportMap("BaseEncoding","com.google.common.io");
        }
        for (GeneratedProperty finalProperty : getFinalProperties()) {
            importedNames.put("constructorProperties", importedName(ConstructorProperties.class));
            importedNames.put("string", importedName(String.class));
            importedNames.put(finalProperty.getName(), importedName(finalProperty.getReturnType()));
        }

        for (GeneratedProperty property : getProperties()) {
            if ("char[]".equals(property.getReturnType().getName())) {
                importedNames.put("constructorProperties", importedName(ConstructorProperties.class));
                importedNames.put("string", importedName(String.class));
                importedNames.put(property.getName(), importedName(property.getReturnType()));
            }
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
                if (returnTypeGto.isTypedef() && returnTypeGto.getProperties() != null
                    && !returnTypeGto.getProperties().isEmpty() && returnTypeGto.getProperties().size() == 1
                    && "value".equals(returnTypeGto.getProperties().get(0).getName())
                    && "byte[]".equals(returnTypeGto.getProperties().get(0).getReturnType().getName())) {
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

    private String generateCharArrayFieldForTypedef(final String fieldName, GeneratedTransferObject typedefType) {
        Preconditions.checkState(typedefType.isTypedef(),"Not a typedef type!");

        final StringBuilder sb = new StringBuilder();
        final List<GeneratedProperty> retTypeCastProperties = typedefType.getProperties();

        if (retTypeCastProperties != null
            && !retTypeCastProperties.isEmpty() && retTypeCastProperties.size() == 1
            && retTypeCastProperties.get(0).getName().equals("value")) {

            final StringBuilder sb1 = new StringBuilder(fieldName);
            sb1.append(".")
                    .append(TextTemplateUtil.getterMethodName(retTypeCastProperties.get(0)))
                    .append("()");

            sb.append(generateCharArrayField(sb1.toString(), retTypeCastProperties.get(0)));
            // generated bits typedef
        } else if (retTypeCastProperties != null && !retTypeCastProperties.isEmpty()
                && typedefType.getBaseType() instanceof BitsTypeDefinition) {
            sb.append("java.util.Arrays.toString(")
                    .append(fieldName)
                    .append(".getValue()).toCharArray();");

            //generated typedef typedef
        } else if ((retTypeCastProperties == null || retTypeCastProperties.isEmpty())) {
            Preconditions.checkState(typedefType.getSuperType() != null);

            sb.append(generateCharArrayFieldForTypedef(fieldName, typedefType.getSuperType()));
        }

        return sb.toString();
    }

    private String generateCharArrayField(final String fieldName, final GeneratedProperty generatedProperty) {
        final StringBuilder sb = new StringBuilder();
        final Type propertyReturnType = generatedProperty.getReturnType();

        // generated type String
        if ("java.lang.String".equals(propertyReturnType.getFullyQualifiedName())) {
            sb.append(fieldName).append(".toCharArray();");
            // generated type InstanceIdentifier
        } else if ("org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier"
                .equals(propertyReturnType.getFullyQualifiedName())) {
            sb.append(fieldName).append(".toString().toCharArray();");
            //generated type binary, boolean, empty
        } else if (BOOLEAN.equals(propertyReturnType)) {
            sb.append(fieldName).append(".toString().toCharArray();");
            //generated type byte[]
        } else if ("byte[]".equals(propertyReturnType.getName())) {
            sb.append("BaseEncoding.base64().encode(").append(fieldName)
                    .append(").toCharArray();");
            //generated type int*, uint, decimal64 or enumeration*
        } else if (propertyReturnType.getFullyQualifiedName().startsWith("java.lang")
                || propertyReturnType instanceof Enumeration
                || propertyReturnType.getFullyQualifiedName().startsWith("java.math")
                || propertyReturnType.getFullyQualifiedName().startsWith("org.opendaylight.yangtools.yang.common")) {
            sb.append(fieldName).append(".toString().toCharArray();");

        } else if (propertyReturnType instanceof GeneratedTransferObject) {
            final GeneratedTransferObject propRetTypeCast = (GeneratedTransferObject) propertyReturnType;

            // generated union type
            if (propRetTypeCast.isUnionType()) {
                sb.append(fieldName).append(".getValue();");

                // generated  typedef type
            } else if (propRetTypeCast.isTypedef()) {
                sb.append(generateCharArrayFieldForTypedef(fieldName, propRetTypeCast));
            }
            // generated type
        } else {
            sb.append(fieldName)
                    .append(".getValue().toString().toCharArray();");
        }

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

        final List<GeneratedProperty> filtered = new ArrayList<>(Collections2.filter(this.getFinalProperties(),
            input -> !"value".equals(input.getName())));

        final List<CharSequence> strings = new ArrayList<>(filtered.size());

        for (GeneratedProperty property : filtered) {
            final Type propertyReturnType = property.getReturnType();
            //string builder for current property
            final StringBuilder sb = new StringBuilder();
            sb.append("if (")
                .append(TextTemplateUtil.fieldName(property))
                .append(" != null) {")
                .append(TextTemplateUtil.fieldName(field))
                .append(" = ")
                .append(generateCharArrayField(TextTemplateUtil.fieldName(property), property))
                .append("}\n");
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
        final UnionRenderer unionRenderer = new UnionRenderer(innerClass);
        final String body = unionRenderer.generateAsInnerClass();
        this.putAllToImportMap(unionRenderer.getImportMap());
        return body;
    }
}