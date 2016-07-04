/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.asJavadoc;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.TextTemplateUtil.getJavaDocForInterface;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.txt.enumTemplate;
import org.opendaylight.mdsal.binding2.txt.interfaceTemplate;

public class InterfaceRenderer extends BaseRenderer {
    /**
     * Creates the instance of this class which is used for generating the interface file source
     * code from <code>type</code>.
     * @param type generated type
     */
    public InterfaceRenderer(final GeneratedType type) {
        super(type);
        if (type == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!");
        }
    }

    @Override
    protected String body() {
        // mainAnnotations string with annotations for whole interface
        final String mainAnnotations = generateAnnotations(type.getAnnotations());
        // methodListBuilder string with the declaration of methods source code in JAVA format
        final StringBuilder methodListBuilder = new StringBuilder();
        for (MethodSignature method : type.getMethodDefinitions()) {
            if(isAccessor(method)) {
                methodListBuilder.append(asJavadoc(method.getComment()));
            } else {
                methodListBuilder.append(getJavaDocForInterface(method));
            }
            methodListBuilder.append(generateAnnotations(method.getAnnotations()))
                    .append(importedName(method.getReturnType()))
                    .append(" ")
                    .append(method.getName())
                    .append("(")
                    .append(generateParameters(method.getParameters()))
                    .append(");");
        }
        final String methodList = methodListBuilder.toString();
        // enums string with rendered enums from template
        final StringBuilder enumsBuilder = new StringBuilder();
        for (Enumeration enumeration : type.getEnumerations()) {
            final String importedName = importedName(String.class);
            final String enumBody = enumTemplate.render(enumeration, importedName).body();
            enumsBuilder.append(enumBody);
        }
        final String enums = enumsBuilder.toString();

        final String generatedImports = generateImports(type.getImplements());
        // generatedConstants list of constants
        final List<String> strings = new LinkedList<>();
        if (!type.getConstantDefinitions().isEmpty()) {
            for (Constant constant : type.getConstantDefinitions()) {
                strings.add(emitConstant(constant));
            }
        }
        final String generatedConstants = String.join("\n", strings);

        final List<String> innerClassesBuilder = new LinkedList<>();
        if (type.getEnclosedTypes().isEmpty()) {
            for (GeneratedType innerClass : type.getEnclosedTypes()) {
                if (innerClass instanceof GeneratedTransferObject) {
                    if (((GeneratedTransferObject) innerClass).isUnionType()) {
                        final UnionRenderer unionRenderer = new UnionRenderer((GeneratedTransferObject) innerClass);
                        innerClassesBuilder.add(unionRenderer.body());
                        this.importMap.putAll(unionRenderer.importMap);
                    } else {
                        final ClassRenderer classRenderer = new ClassRenderer((GeneratedTransferObject) innerClass);
                        innerClassesBuilder.add(classRenderer.generateAsInnerClass());
                        this.importMap.putAll(classRenderer.importMap);
                    }
                }
            }
        }
        final String innerClasses = String.join("\n", strings);

        return interfaceTemplate.render(type, enums, mainAnnotations, methodList, generatedImports,
                generatedConstants, innerClasses).body();
    }

    private boolean isAccessor (final MethodSignature maybeGetter) {
        return maybeGetter.getName().startsWith("is") || maybeGetter.getName().startsWith("get");
    }

    /**
     * @param annotationTypeList list of annotations
     * @return String of annotations in format:
     * "@"annotation
     * (parameterName1=ParameterSingleValue1,...)
     *
     */
    private String generateAnnotations(final List<AnnotationType> annotationTypeList) {
        final StringBuilder annotationList = new StringBuilder();
        if (annotationTypeList != null && !annotationTypeList.isEmpty()) {
            for (AnnotationType annotationType : annotationTypeList) {
                annotationList.append("@")
                        .append(importedName(annotationType));
                if (annotationType.getParameters() != null && !annotationType.getParameters().isEmpty()) {
                    annotationList.append("(");
                    final List<String> parameterList = new LinkedList<>();
                    for (AnnotationType.Parameter parameter : annotationType.getParameters()) {
                        final StringBuilder parameterString = new StringBuilder();
                        parameterString.append(parameter.getName())
                                .append("=")
                                .append(parameter.getSingleValue());
                        parameterList.add(parameterString.toString());
                    }
                    annotationList.append(String.join(",", parameterList))
                            .append(")");
                }
            }
        }
        return annotationList.toString();
    }

    /**
     * @param parameters list of parameters
     * @return list of parameters separated with ","
     */
    private String generateImports(final List<Type> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!parameters.isEmpty()) {
            for (Type parameter : parameters) {
                strings.add(importedName(parameter));
            }
        }
        return String.join(",", strings);
    }
}