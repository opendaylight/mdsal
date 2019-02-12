/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.fieldName;
import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.setterMethod;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators.AbstractRangeGenerator;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators.LengthGenerator;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.classTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.classTemplateConstructors;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.classTemplateRestrictions;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.classTemplateUnionConstr;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.constantsTemplate;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class ClassRenderer extends BaseRenderer {
    protected final GeneratedTransferObject genTO;
    protected final Restrictions restrictions;
    private final List<GeneratedProperty> properties;
    private final List<GeneratedProperty> finalProperties;
    private final List<GeneratedProperty> parentProperties;
    private final List<Enumeration> enums;
    private final List<Constant> consts;
    private final List<GeneratedType> enclosedGeneratedTypes;
    private final List<GeneratedProperty> allProperties;

    private final AbstractRangeGenerator<?> rangeGenerator;

    public ClassRenderer(final GeneratedTransferObject genType) {
        super(genType);
        genTO = genType;
        properties = ImmutableList.copyOf(genTO.getProperties());
        finalProperties = ImmutableList.copyOf(resolveReadOnlyPropertiesFromTO(genTO.getProperties()));
        parentProperties = ImmutableList.copyOf(getPropertiesOfAllParents(genTO));
        enums = ImmutableList.copyOf(genTO.getEnumerations());
        consts = ImmutableList.copyOf(genTO.getConstantDefinitions());
        enclosedGeneratedTypes = ImmutableList.copyOf(genTO.getEnclosedTypes());
        restrictions = genTO.getRestrictions();

        final List<GeneratedProperty> sorted = new ArrayList<>();
        sorted.addAll(properties);
        sorted.addAll(parentProperties);
        final Comparator<GeneratedProperty> function = (GeneratedProperty p1, GeneratedProperty p2) -> {
            final String name = p1.getName();
            final String name1 = p2.getName();
            return name.compareTo(name1);
        };
        sorted.sort(function);
        allProperties = ImmutableList.copyOf(sorted);

        if (restrictions != null && restrictions.getRangeConstraint().isPresent()) {
            rangeGenerator = AbstractRangeGenerator.forType(findProperty(genType, "value").getReturnType());
            requireNonNull(rangeGenerator);
        } else {
            rangeGenerator = null;
        }
    }

    protected List<GeneratedProperty> getProperties() {
        return properties;
    }

    protected List<GeneratedProperty> getFinalProperties() {
        return finalProperties;
    }

    protected List<GeneratedProperty> getParentProperties() {
        return parentProperties;
    }

    protected List<Enumeration> getEnums() {
        return enums;
    }

    protected List<Constant> getConsts() {
        return consts;
    }

    protected List<GeneratedType> getEnclosedGeneratedTypes() {
        return enclosedGeneratedTypes;
    }

    protected Collection<GeneratedProperty> getAllProperties() {
        return allProperties;
    }

    protected String generateAsInnerClass() {
        return generateBody(true);
    }

    @Override
    protected String body() {
        return generateBody(false);
    }

    protected String generateInnerClassBody(final GeneratedTransferObject innerClass) {
        final ClassRenderer classRenderer = new ClassRenderer(innerClass);
        final String body = classRenderer.generateAsInnerClass();
        this.putAllToImportMap(classRenderer.getImportMap());
        return body;
    }

    protected String generateBody(final boolean isInnerClass) {
        getImportedNames().put("type", importedName(getType()));
        getImportedNames().put("arrays", importedName(Arrays.class));
        getImportedNames().put("objects", importedName(Objects.class));
        getImportedNames().put("string", importedName(String.class));
        getImportedNames().put("byte", importedName(Byte.class));
        getImportedNames().put("short", importedName(Short.class));
        getImportedNames().put("integer", importedName(Integer.class));
        getImportedNames().put("long", importedName(Long.class));
        getImportedNames().put("uint8", importedName(Uint8.class));
        getImportedNames().put("uint16", importedName(Uint16.class));
        getImportedNames().put("uint32", importedName(Uint32.class));
        getImportedNames().put("uint64", importedName(Uint64.class));
        getImportedNames().put("stringBuilder", importedName(StringBuilder.class));
        getImportedNames().put("list", importedName(List.class));
        getImportedNames().put("lists", importedName(Lists.class));
        getImportedNames().put("illegalArgumentException", importedName(IllegalArgumentException.class));
        getImportedNames().put("boolean", importedName(Boolean.class));
        getImportedNames().put("qname", importedName(QName.class));

        final List<String> implementsListBuilder = new LinkedList<>();
        if (!getType().getImplements().isEmpty()) {
            for (Type impl : getType().getImplements()) {
                implementsListBuilder.add((importedName(impl)));
            }
        }
        final String implementsList = String.join(", ", implementsListBuilder);

        final List<String> classTemplateBuilder = new LinkedList<>();
        if (!enclosedGeneratedTypes.isEmpty()) {
            for (GeneratedType innerClass : enclosedGeneratedTypes) {
                if (innerClass instanceof GeneratedTransferObject) {
                    classTemplateBuilder.add(generateInnerClassBody((GeneratedTransferObject) innerClass));
                }
            }
        }
        final String innerClasses = String.join("\n", classTemplateBuilder);

        final List<String> enumList = new LinkedList<>();
        if (!enums.isEmpty()) {
            for (Enumeration enumeration : enums) {
                enumList.add(new EnumRenderer(enumeration).body());
            }
        }
        final String enumerations = String.join("\n", enumList);

        final String constants = constantsTemplate.render(getType(), getImportedNames(), this::importedName, false)
                .body();

        if (genTO.getSuperType() != null) {
            getImportedNames().put("superType", importedName(genTO.getSuperType()));
        }

        for (GeneratedProperty property : properties) {
            getImportedNames().put(property.getReturnType().toString(), importedName(property.getReturnType()));
        }

        final String constructors = generateConstructors();

        final StringBuilder lengthRangeCheckerBuilder = new StringBuilder();
        if (restrictions != null) {
            if (restrictions.getLengthConstraint().isPresent()) {
                lengthRangeCheckerBuilder.append(LengthGenerator.generateLengthChecker("_value", findProperty(genTO,
                        "value").getReturnType(), restrictions.getLengthConstraint().get()))
                        .append("\n");
            }
            if (restrictions.getRangeConstraint().isPresent()) {
                lengthRangeCheckerBuilder.append(rangeGenerator.generateRangeChecker("_value", restrictions
                        .getRangeConstraint().get()))
                        .append("\n");
            }
        }
        final String lengthRangeChecker = lengthRangeCheckerBuilder.toString();

        final StringBuilder sb2 = new StringBuilder();
        if (!properties.isEmpty()) {
            for (GeneratedProperty property : properties) {
                final String isFinal = property.isReadOnly() ? " final " : " ";
                sb2.append("private")
                        .append(isFinal)
                        .append(importedName(property.getReturnType()))
                        .append(' ')
                        .append(fieldName(property))
                        .append(";\n");
            }
        }
        final String fields = sb2.toString();
        getImportedNames().put("baseEncoding", importedName(BaseEncoding.class));
        if (!allProperties.isEmpty()) {
            getImportedNames().put("defProp", importedName(((GeneratedProperty) ((List) allProperties).get(0))
                    .getReturnType()));
        }

        final StringBuilder sb3 = new StringBuilder();
        for (GeneratedProperty property : properties) {
            sb3.append(getterMethod(property));
            if (!property.isReadOnly()) {
                sb3.append(setterMethod(property, getType().getName(), importedName(property
                        .getReturnType())));
            }
        }
        final String propertyMethod = sb3.toString();

        return classTemplate.render(getType(), genTO, getImportedNames(), implementsList, innerClasses, enumerations,
                constants, constructors, lengthRangeChecker, fields, allProperties, propertyMethod,
                isInnerClass).body();
    }

    protected String generateConstructors() {
        getImportedNames().put("constructorProperties", importedName(ConstructorProperties.class));
        getImportedNames().put("preconditions", importedName(Preconditions.class));

        final StringBuilder sb1 = new StringBuilder();
        for (GeneratedProperty allProperty : allProperties) {
            sb1.append(classTemplateRestrictions.render(getType(), fieldName(allProperty), allProperty
                    .getReturnType(), rangeGenerator).body());
        }
        final String genRestrictions = sb1.toString();

        final StringBuilder sb2 = new StringBuilder();
        if (genTO.isUnionType()) {
            for (GeneratedProperty allProperty : allProperties) {
                final List other = new ArrayList<>(properties);
                if (other.remove(allProperty)) {
                    sb2.append(classTemplateUnionConstr.render(getType(), parentProperties, allProperty,
                            other, importedName(allProperty.getReturnType()), genRestrictions).body());
                }
            }
        }
        final String unionConstructor = sb2.toString();

        final String argumentsDeclaration = asArgumentsDeclaration(allProperties);
        return classTemplateConstructors.render(genTO, allProperties, properties, parentProperties,
                getImportedNames(), argumentsDeclaration, unionConstructor, genRestrictions).body();
    }

    /**
     * Selects from input list of properties only those which have read only
     * attribute set to true.
     *
     * @param props list of properties of generated transfer object
     * @return subset of <code>properties</code> which have read only attribute
     *      set to true
     */
    private List<GeneratedProperty> resolveReadOnlyPropertiesFromTO(final List<GeneratedProperty> props) {
        return new ArrayList<>(Collections2.filter(props, GeneratedProperty::isReadOnly));
    }

    /**
     * Returns the list of the read only properties of all extending generated
     * transfer object from <code>genTO</code> to highest parent generated
     * transfer object.
     *
     * @param transferObject generated transfer object for which is the list of read only
     *                       properties generated
     * @return list of all read only properties from actual to highest parent
     *      generated transfer object. In case when extension exists the
     *      method is recursive called.
     */
    private List<GeneratedProperty> getPropertiesOfAllParents(final GeneratedTransferObject transferObject) {
        final List<GeneratedProperty> propertiesOfAllParents = new ArrayList<>();
        if (transferObject.getSuperType() != null) {
            final List<GeneratedProperty> allPropertiesOfTO = transferObject.getSuperType().getProperties();
            List<GeneratedProperty> readOnlyPropertiesOfTO = resolveReadOnlyPropertiesFromTO(allPropertiesOfTO);
            propertiesOfAllParents.addAll(readOnlyPropertiesOfTO);
            propertiesOfAllParents.addAll(getPropertiesOfAllParents(transferObject.getSuperType()));
        }
        return propertiesOfAllParents;
    }
}
