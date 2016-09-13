/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.java.api.generator.util;

import com.google.common.collect.ImmutableSortedSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding2.generator.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.spec.Augmentable;

public final class BuilderTemplateUtil extends BaseTemplateUtil{
    private static GeneratedType globalGentType;
    private static GeneratedProperty augmentField;
    private static Set<GeneratedProperty> properties;

    private BuilderTemplateUtil() {
        throw new UnsupportedOperationException("Util class");
    }


    /**
     * Creates set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces. Then creates set of generated property instances.
     *
     * @param genType GeneratedType
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    public static Set<GeneratedProperty> generateProperties(GeneratedType genType) {
        properties = propertiesFromMethods(createMethods(genType));
        return properties;
    }

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code>
     * and recursively their implemented interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    private static void collectImplementedMethods(Set<MethodSignature> methods, List<Type> implementedIfcs) {
        if (implementedIfcs != null && !implementedIfcs.isEmpty()) {
            for (Type implementedIfc : implementedIfcs) {
                if ((implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))) {
                    final GeneratedType ifc = (GeneratedType) implementedIfc;
                    methods.addAll((ifc).getMethodDefinitions());
                    collectImplementedMethods(methods, ifc.getImplements());
                } else if (implementedIfc.getFullyQualifiedName().equals(Augmentable.class.getName())) {
                    for (Method method : Augmentable.class.getMethods()) {
                        if (method.getName().equals("getAugmentation")) {
                            final String fullyQualifiedName = method.getReturnType().getName();
                            final String aPackage = getPackage(fullyQualifiedName);
                            final String name = getName(fullyQualifiedName);
                            final GeneratedTOBuilderImpl generatedTOBuilder = new GeneratedTOBuilderImpl(aPackage, name);
                            final ReferencedTypeImpl referencedType = new ReferencedTypeImpl(aPackage, name);
                            final ReferencedTypeImpl generic = new ReferencedTypeImpl(globalGentType.getPackageName(),
                                    globalGentType.getName());
                            final ParameterizedType parametrizedReturnType = Types.parameterizedTypeFor(referencedType, generic);
                            generatedTOBuilder.addMethod(method.getName()).setReturnType(parametrizedReturnType);
                            augmentField = propertyFromGetter(generatedTOBuilder.toInstance().getMethodDefinitions().get(0));
                        }
                    }
                }
            }
        }
    }

    public static GeneratedProperty getAugmentField() {
        return augmentField;
    }

    /**
     * Returns the name of the package from <code>fullyQualifiedName</code>.
     *
     * @param fullyQualifiedName string with fully qualified type name (package + type)
     * @return string with the package name
     */
    private static String getPackage(String fullyQualifiedName) {
        final int lastDotIndex = fullyQualifiedName.lastIndexOf(BaseTemplateUtil.DOT);
        return (lastDotIndex == -1) ? "" : fullyQualifiedName.substring(0, lastDotIndex);
    }


    /**
     * Returns the name of tye type from <code>fullyQualifiedName</code>
     *
     * @param fullyQualifiedName string with fully qualified type name (package + type)
     * @return string with the name of the type
     */
    private static String getName(String fullyQualifiedName) {
        final int lastDotIndex = fullyQualifiedName.lastIndexOf(BaseTemplateUtil.DOT);
        return (lastDotIndex == -1) ? fullyQualifiedName : fullyQualifiedName.substring(lastDotIndex + 1);
    }

    /**
     * Creates generated property instance from the getter <code>method</code> name and return type.
     *
     * @param method method signature from which is the method name and return type obtained
     * @return generated property instance for the getter <code>method</code>
     * @throws IllegalArgumentException
     *  <li>if the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> is empty</li>
     *  <li>if the return type of the <code>method</code> equals <code>null</code></li>
     */
    private static GeneratedProperty propertyFromGetter(MethodSignature method) {
        if (method == null || method.getName() == null || method.getName().isEmpty() || method.getReturnType() == null) {
            throw new IllegalArgumentException("Method, method name, method return type reference cannot be NULL or " +
                    "empty!");
        }
        final String prefix = Types.BOOLEAN.equals(method.getReturnType()) ? "is" : "get";
        if (method.getName().startsWith(prefix)) {
            final String fieldName = toFirstLower(method.getName().substring(prefix.length()));
            final GeneratedTOBuilderImpl tmpGenTO = new GeneratedTOBuilderImpl("foo", "foo");
            tmpGenTO.addProperty(fieldName)
                    .setReturnType(method.getReturnType());
            return tmpGenTO.toInstance().getProperties().get(0);
        }
        return null;
    }

    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param methods set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    private static Set propertiesFromMethods(Collection<MethodSignature> methods) {
        if (methods == null || methods.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<GeneratedProperty> result = new LinkedHashSet<>();
        for (MethodSignature method : methods) {
            final GeneratedProperty createdField = propertyFromGetter(method);
            if (createdField != null) {
                result.add(createdField);
            }
        }
        return result;
    }

    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     *
     * @returns set of method signature instances
     */
    private static Set<MethodSignature> createMethods(GeneratedType genType) {
        final Set<MethodSignature> methods = new LinkedHashSet<>();
        methods.addAll(genType.getMethodDefinitions());
        collectImplementedMethods(methods, genType.getImplements());
        final Set<MethodSignature> sortedMethods = ImmutableSortedSet.orderedBy(
                new AlphabeticallyTypeMemberComparator<MethodSignature>())
                .addAll(methods)
                .build();
        return sortedMethods;
    }

    public static String generateCopyConstructorForTemplate(GeneratedType genType) {
        final List allProps = new ArrayList<>(properties);
        final boolean isList = true;
        final Type keyType = getKey(genType);
        return null;
    }

    private static Type getKey(GeneratedType genType) {
        for (MethodSignature methodSignature : genType.getMethodDefinitions()) {
            if ("getKey".equals(methodSignature.getName())) {
                return methodSignature.getReturnType();
            }
        }
        return null;
    }

    private static boolean implementsIfc(GeneratedType type, Type impl) {
        for (Type ifc : type.getImplements()) {
            if (ifc.equals(impl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method is used to find out if given type implements any interface from uses.
     */
    public static boolean hasImplementsFromUses(GeneratedType type) {
        int i = 0;
        for (Type impl : getAllIfcs(type)) {
            if ((impl instanceof GeneratedType) && !(((GeneratedType)impl).getMethodDefinitions().isEmpty())) {
                i = i + 1;
            }
        }
        return i > 0;
    }

    public static Set<Type> getAllIfcs(Type type) {
        final Set<Type> baseIfcs = new HashSet<>();
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            for (Type impl : ((GeneratedType)type).getImplements()) {
                if (impl instanceof GeneratedType && !(((GeneratedType)impl).getMethodDefinitions().isEmpty())) {
                    baseIfcs.add(impl);
                }
                baseIfcs.addAll(getAllIfcs(impl));
            }
        }
        return baseIfcs;
    }

    public static List<String> toListOfNames(Collection<Type> types) {
        final List<String> names = new ArrayList<>();
        for (Type currentType : types) {
            names.add(currentType.getFullyQualifiedName());
        }
        return names;
    }

    private static void removeProperty(Collection<GeneratedProperty> properties, String name) {
        for (final GeneratedProperty property : properties) {
            if (name.equals(property.getName())) {
                properties.remove(property);
                break;
            }
        }
    }
}