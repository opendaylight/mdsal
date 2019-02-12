/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.constantsTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.enumTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.interfaceTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.yang.common.QName;

public class InterfaceRenderer extends BaseRenderer {

    private static final char NEW_LINE = '\n';

    /**
     * Creates the instance of this class which is used for generating the interface file source
     * code from <code>type</code>.
     * @param type generated type
     */
    public InterfaceRenderer(final GeneratedType type) {
        super(type);
        requireNonNull(type, "Generated type reference cannot be NULL!");
    }

    @Override
    protected String body() {
        // mainAnnotations string with annotations for whole interface
        final String mainAnnotations = generateAnnotations(getType().getAnnotations());
        // StringBuilder string with the declaration of methods source code in JAVA format
        final StringBuilder sb1 = new StringBuilder();
        for (MethodSignature method : getType().getMethodDefinitions()) {
            if (isAccessor(method)) {
                sb1.append(TextTemplateUtil.asJavadoc(method.getComment()));
            } else {
                sb1.append(TextTemplateUtil.getJavaDocForInterface(method));
            }
            sb1.append(generateAnnotations(method.getAnnotations()))
                .append(importedName(method.getReturnType()))
                .append(' ')
                .append(method.getName())
                .append('(')
                .append(generateParameters(method.getParameters()))
                .append(");")
                .append(NEW_LINE);
        }
        final String methodList = sb1.toString();

        // enums string with rendered enums from template
        final StringBuilder sb2 = new StringBuilder();
        for (Enumeration enumeration : getType().getEnumerations()) {
            final String importedName = importedName(String.class);
            final String enumBody = enumTemplate.render(enumeration, importedName).body();
            sb2.append(enumBody);
        }
        final String enums = sb2.toString();

        final String generatedImports = generateImports(getType().getImplements());

        getImportedNames().put("qname", importedName(QName.class));
        final String generatedConstants = constantsTemplate.render(getType(), getImportedNames(),
            this::importedName, true).body();

        final Entry<String, String> identifier = generateInstanceIdentifier();

        final List<String> innerClasses = new ArrayList<>(getType().getEnclosedTypes().size());
        for (GeneratedType innerClass : getType().getEnclosedTypes()) {
            if (innerClass instanceof GeneratedTransferObject) {
                if (((GeneratedTransferObject) innerClass).isUnionType()) {
                    final UnionRenderer unionRenderer = new UnionRenderer((GeneratedTransferObject) innerClass);
                    innerClasses.add(unionRenderer.generateAsInnerClass());
                    this.putAllToImportMap(unionRenderer.getImportMap());
                } else {
                    final ClassRenderer classRenderer = new ClassRenderer((GeneratedTransferObject) innerClass);
                    innerClasses.add(classRenderer.generateAsInnerClass());
                    this.putAllToImportMap(classRenderer.getImportMap());
                }
            }
        }
        final String generatedInnerClasses = String.join("\n", innerClasses);

        return interfaceTemplate.render(getType(), enums, mainAnnotations, methodList, generatedImports,
                generatedConstants, generatedInnerClasses, identifier.getKey(), identifier.getValue()).body();
    }

    private static boolean isAccessor(final MethodSignature maybeGetter) {
        return maybeGetter.getName().startsWith("is") || maybeGetter.getName().startsWith("get");
    }

    /**
     * Return string of annotations.
     * @param annotationTypeList list of annotations
     * @return String of annotations in format:
     *     "@"annotation
     *     (parameterName1=ParameterSingleValue1,...)
     *
     */
    private String generateAnnotations(final List<AnnotationType> annotationTypeList) {
        final StringBuilder sb1 = new StringBuilder();
        for (AnnotationType annotationType : annotationTypeList) {
            sb1.append('@').append(importedName(annotationType));
            if (!annotationType.getParameters().isEmpty()) {
                sb1.append('(');
            }
            final List<String> parameterList = new ArrayList<>(annotationType.getParameters().size());
            for (AnnotationType.Parameter parameter : annotationType.getParameters()) {
                final StringBuilder sb2 = new StringBuilder();
                sb2.append(parameter.getName()).append('=').append(parameter.getSingleValue());
                parameterList.add(sb2.toString());
            }
            sb1.append(String.join(",", parameterList));
            if (!annotationType.getParameters().isEmpty()) {
                sb1.append(')');
            }
            sb1.append(NEW_LINE);
        }
        return sb1.toString();
    }

    /**
     * Generate default method getInstanceIdentifier.
     * @return  string pair of instance identifier and key parameters
     */
    private Entry<String, String> generateInstanceIdentifier() {
        //Only tree data nodes need to generate the method.
        if (null == getType().getBindingNamespaceType()
                || !BindingNamespaceType.isTreeData(getType().getBindingNamespaceType())
                || !getType().getImplements().contains(BindingTypes.TREE_CHILD_NODE)) {
            return new SimpleEntry<>(null, null);
        }

        final Deque<GeneratedType> dataPath = new ArrayDeque<>();
        GeneratedType type = getType();
        GeneratedTypeBuilder parentTypeBuilder;

        while (type != null) {
            dataPath.push(type);
            importedName(type);
            parentTypeBuilder = (GeneratedTypeBuilder) type.getParentTypeForBuilder();
            type = parentTypeBuilder != null ? parentTypeBuilder.toInstance() : null;
        }

        dataPath.pop();

        final StringBuilder iiBuidler = new StringBuilder();
        type = dataPath.pop();
        iiBuidler.append("InstanceIdentifier.builder(").append(type.getName()).append(".class)");
        importedName(InstanceIdentifier.class);
        final List<String> keys = new ArrayList<>();
        while (dataPath.peek() != null) {
            type = dataPath.pop();
            if (type.getImplements().contains(BindingTypes.AUGMENTATION)) {
                iiBuidler.append(".augmentation(").append(type.getName()).append(".class)");
            } else {
                Optional<MethodSignature> method = type.getMethodDefinitions().stream().filter(m ->
                    m.getName().equals("getIdentifier")).findFirst();
                if (method.isPresent()) {
                    importedName(method.get().getReturnType());
                    final String keyName = method.get().getReturnType().getName();
                    final String normalizedKeyName = JavaIdentifierNormalizer.normalizeSpecificIdentifier(keyName,
                        JavaIdentifier.METHOD);
                    keys.add(new StringBuilder().append("final ").append(keyName).append(" _")
                        .append(normalizedKeyName).toString());
                    iiBuidler.append(".child(").append(type.getFullyQualifiedName()).append(".class, _")
                        .append(normalizedKeyName).append(")");
                } else {
                    iiBuidler.append(".child(").append(type.getFullyQualifiedName()).append(".class)");
                }
            }
        }
        iiBuidler.append(".build()");
        return new SimpleEntry<>(iiBuidler.toString(), String.join(", ", keys));
    }


    /**
     * Return list of parameters.
     * @param parameters list of parameters
     * @return list of parameters separated with ","
     */
    private String generateImports(final List<Type> parameters) {
        final List<String> strings = new ArrayList<>(parameters.size());
        for (Type parameter : parameters) {
            strings.add(importedName(parameter));
        }

        return String.join(", ", strings);
    }
}