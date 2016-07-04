/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding2.generator.util.Binding2Mapping.MEMBER_PATTERN_LIST;
import static org.opendaylight.mdsal.binding2.generator.util.Binding2Mapping.PATTERN_CONSTANT_NAME;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.fieldName;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.setterMethod;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding2.java.api.generator.renderers.rangeGenerators.AbstractRangeGenerator;
import org.opendaylight.mdsal.binding2.java.api.generator.renderers.rangeGenerators.LengthGenerator;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.txt.classTemplate;
import org.opendaylight.mdsal.binding2.txt.classTemplateConstructors;
import org.opendaylight.mdsal.binding2.txt.classTemplateInitBlock;
import org.opendaylight.mdsal.binding2.txt.classTemplateRestrictions;
import org.opendaylight.mdsal.binding2.txt.classTemplateUnionConstr;

public class ClassRenderer extends BaseRenderer {
    protected final List<GeneratedProperty> properties;
    protected final List<GeneratedProperty> finalProperties;
    protected final List<GeneratedProperty> parentProperties;
    protected final List<Enumeration> enums;
    protected final List<Constant> consts;
    protected final List<GeneratedType> enclosedGeneratedTypes;
    protected final Iterable<GeneratedProperty> allProperties;
    protected final GeneratedTransferObject genTO;
    protected final Restrictions restrictions;
    private final Map<String, String> importedNames = new HashMap<>();
    private final AbstractRangeGenerator<?> rangeGenerator;

    public ClassRenderer(final GeneratedTransferObject genType) {
        super(genType);
        genTO = genType;
        properties = genTO.getProperties();
        finalProperties = resolveReadOnlyPropertiesFromTO(genTO.getProperties());
        parentProperties = getPropertiesOfAllParents(genTO);
        restrictions = genTO.getRestrictions();
        enums = genTO.getEnumerations();
        consts = genTO.getConstantDefinitions();
        enclosedGeneratedTypes = genTO.getEnclosedTypes();

        final List<GeneratedProperty> sorted = new ArrayList<GeneratedProperty>();
        sorted.addAll(properties);
        sorted.addAll(parentProperties);
        final Comparator<GeneratedProperty> _function = (GeneratedProperty p1, GeneratedProperty p2) -> {
            final String name = p1.getName();
            final String name_1 = p2.getName();
            return name.compareTo(name_1);
        };
        Collections.<GeneratedProperty>sort(sorted, _function);
        allProperties = sorted;

        if (restrictions != null && !(restrictions.getRangeConstraints() != null && !restrictions.getLengthConstraints
                ().isEmpty())) {
            rangeGenerator = AbstractRangeGenerator.forType(findProperty(genType, "value").getReturnType());
            Preconditions.checkNotNull(rangeGenerator);
        } else {
            rangeGenerator = null;
        }
    }

    protected String generateAsInnerClass() {
        return generateBody(true);
    }

    @Override
    protected String body() {
        return generateBody(false);
    }

    protected String generateBody(boolean isInnerClass) {
        importedNames.put("genTypeSuper", importedName(genTO.getSuperType()));
        importedNames.put("type", importedName(type));
        importedNames.put("arrays", importedName(Arrays.class));
        importedNames.put("objects", importedName(Objects.class));
        importedNames.put("string", importedName(String.class));
        importedNames.put("byte", importedName(Byte.class));
        importedNames.put("short", importedName(Short.class));
        importedNames.put("integer", importedName(Integer.class));
        importedNames.put("long", importedName(Long.class));
        importedNames.put("stringBuilder", importedName(StringBuilder.class));
        importedNames.put("list", importedName(List.class));
        importedNames.put("lists", importedName(Lists.class));
        importedNames.put("illegalArgumentException", importedName(IllegalArgumentException.class));
        importedNames.put("boolean", importedName(Boolean.class));

        final List<String> implementsListBuilder = new LinkedList<>();
        if (!type.getImplements().isEmpty()) {
            for (Type impl : type.getImplements()) {
                implementsListBuilder.add((importedName(impl)));
            }
        }
        final String implementsList = String.join(", ", implementsListBuilder);

//        TO DO implement innner clases
        final String innerClasses = null;

        final List<String> enumList = new LinkedList<>();
        if (!enums.isEmpty()) {
            for (Enumeration enumeration : enums) {
                enumList.add(new EnumRenderer(enumeration).body());
            }
        }
        final String enumerations = String.join("\n", enumList);

        final StringBuilder constantsBuilder = new StringBuilder();
        final String initBlock = classTemplateInitBlock.render(importedName(Pattern.class)).body();
        if (!consts.isEmpty()) {
            for (Constant constant : consts) {
                if (PATTERN_CONSTANT_NAME.equals(constant.getName())) {
                    if (constant.getValue() instanceof List<?>) {
                        constantsBuilder.append("private static final ")
                                .append(importedName(Pattern.class))
                                .append("[] ")
                                .append(MEMBER_PATTERN_LIST)
                                .append(";\npublic static final ")
                                .append(importedName(List.class))
                                .append("<String> ")
                                .append(PATTERN_CONSTANT_NAME)
                                .append(" = ")
                                .append(importedName(ImmutableList.class))
                                .append(".of(");
                        final List<String> constantList = new LinkedList<>();
                        for (Object item : (List)constant.getValue()) {
                            if (item instanceof String) {
                                constantList.add("\"" + item + "\"");
                            }
                        }
                        constantsBuilder.append(String.join(", ", constantList));
                        constantsBuilder.append(");")
                                .append(initBlock);
                    }
                } else {
                    constantsBuilder.append(emitConstant(constant));
                }
            }
        }
        final String constants = constantsBuilder.toString();

        if (genTO.getSuperType() != null) {
            importedNames.put("superType", importedName(genTO.getSuperType()));
        }
        for (GeneratedProperty property : properties) {
            importedNames.put(property.getReturnType().toString(), importedName(property.getReturnType()));
        }

        importedNames.put("constructorProperties", importedName(ConstructorProperties.class));
        importedNames.put("preconditions", importedName(Preconditions.class));

        final StringBuilder restrictionsBuilder = new StringBuilder();
        for (GeneratedProperty allProperty : allProperties) {
            restrictionsBuilder.append(classTemplateRestrictions.render(type, fieldName(allProperty), allProperty
                    .getReturnType(), rangeGenerator).body());
        }
        final String genRestrictions = restrictionsBuilder.toString();

        final StringBuilder unionConstructorBuilder = new StringBuilder();
        if (genTO.isUnionType()) {
            for (GeneratedProperty allProperty : allProperties) {
                final List other = new ArrayList<>(properties);
                if (other.remove(allProperty)) {
                    unionConstructorBuilder.append(classTemplateUnionConstr.render(type, parentProperties, allProperty, other,
                            importedName(allProperty.getReturnType()), genRestrictions).body());
                }
            }
        }
        final String unionConstructor = unionConstructorBuilder.toString();

        final String argumentsDeclaration = asArgumentsDeclaration(allProperties);
        final String constructors = classTemplateConstructors.render(genTO, (List)allProperties, properties,
                parentProperties, importedNames, argumentsDeclaration, unionConstructor, genRestrictions).body();

        final StringBuilder lengthRangeCheckerBuilder = new StringBuilder();
        if (restrictions != null) {
            if (restrictions.getLengthConstraints() != null && !restrictions.getLengthConstraints().isEmpty()) {
                lengthRangeCheckerBuilder.append(LengthGenerator.generateLengthChecker("_value", findProperty(genTO,
                        "value").getReturnType(), restrictions.getLengthConstraints()))
                    .append("\n");
            }
            if (restrictions.getRangeConstraints() != null && !restrictions.getRangeConstraints().isEmpty()) {
                lengthRangeCheckerBuilder.append(rangeGenerator.generateRangeChecker("_value", restrictions
                        .getRangeConstraints()))
                        .append("\n");
            }
        }
        final String lengthRangeChecker = lengthRangeCheckerBuilder.toString();

        final StringBuilder fieldsBuilder = new StringBuilder();
        if (!properties.isEmpty()) {
            for (GeneratedProperty property : properties) {
                final String isFinal = property.isReadOnly() ? " final " : " ";
                fieldsBuilder.append("private")
                        .append(isFinal)
                        .append(importedName(property.getReturnType()))
                        .append(" ")
                        .append(fieldName(property))
                        .append(";");
            }
        }
        final String fields = fieldsBuilder.toString();
        importedNames.put("baseEncoding", importedName(BaseEncoding.class));
        importedNames.put("defProp", importedName(((GeneratedProperty)((List) allProperties).get(0)).getReturnType()));

        final StringBuilder propertyMethodBuilder = new StringBuilder();
        for (GeneratedProperty property : properties) {
            propertyMethodBuilder.append(getterMethod(property));
            if (!property.isReadOnly()) {
                propertyMethodBuilder.append(setterMethod(property, type.getName(), importedName(property.getReturnType())));
            }
        }
        final String propertyMethod = propertyMethodBuilder.toString();

        return classTemplate.render(type, genTO, importedNames, implementsList, innerClasses, enumerations,
                constants, constructors, lengthRangeChecker, fields, (List)allProperties, propertyMethod).body();
    }

    /**
     * Selects from input list of properties only those which have read only
     * attribute set to true.
     *
     * @param properties
     *            list of properties of generated transfer object
     * @return subset of <code>properties</code> which have read only attribute
     *         set to true
     */
    private List<GeneratedProperty> resolveReadOnlyPropertiesFromTO(final List<GeneratedProperty> properties) {
        final List<GeneratedProperty> readOnlyProperties = new ArrayList<GeneratedProperty>();
        if (properties != null) {
            for (final GeneratedProperty property : properties) {
                if (property.isReadOnly()) {
                    readOnlyProperties.add(property);
                }
            }
        }
        return readOnlyProperties;
    }

    /**
     * Returns the list of the read only properties of all extending generated
     * transfer object from <code>genTO</code> to highest parent generated
     * transfer object
     *
     * @param genTO
     *            generated transfer object for which is the list of read only
     *            properties generated
     * @return list of all read only properties from actual to highest parent
     *         generated transfer object. In case when extension exists the
     *         method is recursive called.
     */
    private List<GeneratedProperty> getPropertiesOfAllParents(final GeneratedTransferObject genTO) {
        final List<GeneratedProperty> propertiesOfAllParents = new ArrayList<GeneratedProperty>();
        if (genTO.getSuperType() != null) {
            final List<GeneratedProperty> allPropertiesOfTO = genTO.getSuperType().getProperties();
            List<GeneratedProperty> readOnlyPropertiesOfTO = resolveReadOnlyPropertiesFromTO(allPropertiesOfTO);
            propertiesOfAllParents.addAll(readOnlyPropertiesOfTO);
            propertiesOfAllParents.addAll(getPropertiesOfAllParents(genTO.getSuperType()));
        }
        return propertiesOfAllParents;
    }
}