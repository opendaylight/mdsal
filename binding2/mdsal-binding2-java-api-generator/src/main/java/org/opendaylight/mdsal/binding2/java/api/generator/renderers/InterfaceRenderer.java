/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding2.java.api.generator.util.BaseTemplateUtil.asJavadoc;
import static org.opendaylight.mdsal.binding2.java.api.generator.util.BaseTemplateUtil.getJavaDocForInterface;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.txt.enumTemplate;
import org.opendaylight.mdsal.binding2.txt.interfaceTemplate;
import org.opendaylight.yangtools.yang.common.QName;

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
        // methodList string with the declaration of methods source code in JAVA format
        final StringBuilder methodList = new StringBuilder();
        for (MethodSignature method : type.getMethodDefinitions()) {
            if(isAccessor(method)) {
                methodList.append(asJavadoc(method.getComment()));
            } else {
                methodList.append(getJavaDocForInterface(method));
            }
            methodList.append(generateAnnotations(method.getAnnotations()))
                    .append(importedName(method.getReturnType()))
                    .append(" ")
                    .append(method.getName())
                    .append("(")
                    .append(generateParameters(method.getParameters()))
                    .append(");");
        }
        // enums string with rendered enums from template
        final StringBuilder enums = new StringBuilder();
        for (Enumeration enumeration : type.getEnumerations()) {
            final String importedName = importedName(String.class);
            final String enumBody = enumTemplate.render(enumeration, importedName).body();
            enums.append(enumBody);
        }
        final String generatedImports = generateImports(type.getImplements());
        // generatedConstants list of constants
        final List<String> strings = new LinkedList<>();
        if (!type.getConstantDefinitions().isEmpty()) {
            for (Constant constant : type.getConstantDefinitions()) {
                strings.add(emitConstant(constant));
            }
        }
        final String generatedConstants = String.join("\n", strings);
        return interfaceTemplate.render(type, enums.toString(), mainAnnotations, methodList.toString(),
                generatedImports, generatedConstants).body();
    }

    private boolean isAccessor (MethodSignature maybeGetter) {
        return maybeGetter.getName().startsWith("is") || maybeGetter.getName().startsWith("get");
    }

    /**
     * @param annotationTypeList list of annotations
     * @return String of annotations in format:
     * "@"annotation
     * (parameterName1=ParameterSingleValue1,...)
     *
     */
    private String generateAnnotations(List<AnnotationType> annotationTypeList) {
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
    private String generateImports(List<Type> parameters) {
        final List<String> strings = new LinkedList<>();
        if (!parameters.isEmpty()) {
            for (Type parameter : parameters) {
                strings.add(importedName(parameter));
            }
        }
        return String.join(",", strings);
    }


    /**
     * @param constant
     * @return string with constant wrapped in code
     */
    private String emitConstant(Constant constant) {
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
}