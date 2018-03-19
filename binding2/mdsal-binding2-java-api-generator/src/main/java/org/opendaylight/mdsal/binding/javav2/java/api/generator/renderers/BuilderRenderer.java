/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.DOT;
import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.getPropertyList;
import static org.opendaylight.mdsal.binding.javav2.java.api.generator.util.TextTemplateUtil.toFirstLower;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.builderConstructorHelperTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.builderTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.constantsTemplate;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.util.AlphabeticallyTypeMemberComparator;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTypeForBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.CodeHelpers;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.AugmentationHolder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;

public class BuilderRenderer extends BaseRenderer {

    /**
     * Set of class attributes (fields) which are derived from the getter methods names
     */
    private final Set<GeneratedProperty> properties;

    /**
     * Set of name from properties
     */
    private final Map<GeneratedProperty, String> importedNamesForProperties = new HashMap<>();

    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME
     */
    private GeneratedProperty augmentField;

    boolean instantiable = false;

    public BuilderRenderer(final GeneratedType type) {
        super(type);
        this.properties = propertiesFromMethods(createMethods());
        putToImportMap(Builder.class.getSimpleName(), Builder.class.getPackage().getName());
        putToImportMap(type.getName(), type.getPackageName());
    }

    @Override
    protected String packageDefinition() {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ")
                .append(((GeneratedTypeForBuilder)getType()).getPackageNameForBuilder())
                .append(";\n\n");
        return sb.toString();
    }

    @Override
    protected boolean hasSamePackage(final String importedTypePackageName) {
        return ((GeneratedTypeForBuilder)getType()).getPackageNameForBuilder()
                .equals(importedTypePackageName);
    }

    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param methods set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    private Set<GeneratedProperty> propertiesFromMethods(final Collection<MethodSignature> methods) {
        if (methods == null || methods.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<GeneratedProperty> result = new LinkedHashSet<>();
        for (MethodSignature method : methods) {
            final GeneratedProperty createdField = propertyFromGetter(method);
            if (createdField != null) {
                result.add(createdField);
                importedNamesForProperties.put(createdField, importedName(createdField.getReturnType()));
            }
        }
        return result;
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
    private GeneratedProperty propertyFromGetter(final MethodSignature method) {
        Preconditions.checkArgument(method != null, "Method cannot be NULL");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(method.getName()),
            "Method name cannot be NULL or empty");
        Preconditions.checkArgument(method.getReturnType() != null,
            "Method return type reference cannot be NULL");
        final String prefix = Types.BOOLEAN.equals(method.getReturnType()) ? "is" : "get";
        if (method.getName().startsWith(prefix)) {
            final String fieldName = toFirstLower(method.getName().substring(prefix.length()));
            final GeneratedTOBuilderImpl tmpGenTO =
                new GeneratedTOBuilderImpl("foo", "foo", true);
            tmpGenTO.addProperty(fieldName).setReturnType(method.getReturnType());
            return tmpGenTO.toInstance().getProperties().get(0);
        }
        return null;
    }

    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     *
     * @returns set of method signature instances
     */
    private Set<MethodSignature> createMethods() {
        final Set<MethodSignature> methods = new LinkedHashSet<>();
        methods.addAll(getType().getMethodDefinitions());
        collectImplementedMethods(methods, getType().getImplements());
        final Set<MethodSignature> sortedMethods = ImmutableSortedSet.orderedBy(
                new AlphabeticallyTypeMemberComparator<MethodSignature>())
                .addAll(methods)
                .build();
        return sortedMethods;
    }

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code>
     * and recursively their implemented interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    private void collectImplementedMethods(final Set<MethodSignature> methods, List<Type> implementedIfcs) {
        if (implementedIfcs != null && !implementedIfcs.isEmpty()) {
            for (Type implementedIfc : implementedIfcs) {
                if ((implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject))) {
                    final GeneratedType ifc = (GeneratedType) implementedIfc;
                    if (implementedIfc instanceof GeneratedTypeForBuilder) {
                        methods.addAll(ifc.getMethodDefinitions());
                    }
                    collectImplementedMethods(methods, ifc.getImplements());
                } else if (Augmentable.class.getName().equals(implementedIfc.getFullyQualifiedName())) {
                    for (Method method : Augmentable.class.getMethods()) {
                        if ("getAugmentation".equals(method.getName())) {
                            final String fullyQualifiedName = method.getReturnType().getName();
                            final String aPackage = getPackage(fullyQualifiedName);
                            final String name = getName(fullyQualifiedName);
                            final GeneratedTOBuilderImpl generatedTOBuilder = new GeneratedTOBuilderImpl(aPackage,
                                    name, true);
                            final ReferencedTypeImpl referencedType = new ReferencedTypeImpl(aPackage, name,
                                true, null);
                            final ReferencedTypeImpl generic = new ReferencedTypeImpl(getType().getPackageName(),
                                    getType().getName(), true, null);
                            final ParameterizedType parametrizedReturnType =
                                Types.parameterizedTypeFor(referencedType, generic);
                            generatedTOBuilder.addMethod(method.getName()).setReturnType(parametrizedReturnType);
                            augmentField = propertyFromGetter(generatedTOBuilder.toInstance().getMethodDefinitions()
                                .get(0));
                            getImportedNames().put("map", importedName(Map.class));
                            getImportedNames().put("hashMap", importedName(HashMap.class));
                            getImportedNames().put("class", importedName(Class.class));
//                            To do This is for third party, is it needed ?
                            getImportedNames().put("augmentationHolder", importedName(AugmentationHolder.class));
                            getImportedNames().put("collections", importedName(Collections.class));
                            getImportedNames().put("augmentFieldReturnType", importedName(augmentField.getReturnType()));
                        }
                    }
                } else if (Instantiable.class.getName().equals(implementedIfc.getFullyQualifiedName())) {
                    getImportedNames().put("class", importedName(Class.class));
                    instantiable = true;
                }
            }
        }
    }

    /**
     * Returns the name of the package from <code>fullyQualifiedName</code>.
     *
     * @param fullyQualifiedName string with fully qualified type name (package + type)
     * @return string with the package name
     */
    private String getPackage(final String fullyQualifiedName) {
        final int lastDotIndex = fullyQualifiedName.lastIndexOf(DOT);
        return (lastDotIndex == -1) ? "" : fullyQualifiedName.substring(0, lastDotIndex);
    }

    /**
     * Returns the name of tye type from <code>fullyQualifiedName</code>
     *
     * @param fullyQualifiedName string with fully qualified type name (package + type)
     * @return string with the name of the type
     */
    private String getName(final String fullyQualifiedName) {
        final int lastDotIndex = fullyQualifiedName.lastIndexOf(DOT);
        return (lastDotIndex == -1) ? fullyQualifiedName : fullyQualifiedName.substring(lastDotIndex + 1);
    }

    public static Set<Type> getAllIfcs(final Type type) {
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

    /**
     * Method is used to find out if given type implements any interface from uses.
     *
     * @param type type to inspect
     * @return has implements from uses-stmt true/false
     */
    public static boolean hasImplementsFromUses(GeneratedType type) {
        for (Type impl : getAllIfcs(type)) {
            if ((impl instanceof GeneratedType) && !(((GeneratedType)impl).getMethodDefinitions().isEmpty())) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> toListOfNames(final Set<Type> types) {
        final Set<String> names = new HashSet<>();
        for (Type currentType : types) {
            names.add(currentType.getFullyQualifiedName());
        }
        return names;
    }

    @Override
    protected String body() {
        final String parentTypeForBuilderName;
        getImportedNames().put("genType", importedName(getType()));
        getImportedNames().put("objects", importedName(Objects.class));
        getImportedNames().put("object", importedName(Object.class));
        getImportedNames().put("string", importedName(String.class));
        getImportedNames().put("arrays", importedName(Arrays.class));
        getImportedNames().put("stringBuilder", importedName(StringBuilder.class));
        getImportedNames().put("treeNode", importedName(TreeNode.class));
        getImportedNames().put("instantiable", importedName(Instantiable.class));
        getImportedNames().put("item", importedName(Item.class));
        getImportedNames().put("identifiableItem", importedName(IdentifiableItem.class));
        getImportedNames().put("qname", importedName(QName.class));
        getImportedNames().put("codeHelpers", importedName(CodeHelpers.class));
        getImportedNames().put("list", importedName(List.class));
        getImportedNames().put("immutableList", importedName(ImmutableList.class));
        getImportedNames().put("pattern", importedName(Pattern.class));

        if (getType().getParentType() != null) {
            getImportedNames().put("parent", importedName(getType().getParentType()));
            parentTypeForBuilderName = getType().getParentType().getFullyQualifiedName();
        } else if (getType().getParentTypeForBuilder() != null) {
            getImportedNames().put("parentTypeForBuilder", importedName(getType().getParentTypeForBuilder()));
            parentTypeForBuilderName = getType().getParentTypeForBuilder().getFullyQualifiedName();
        } else {
            parentTypeForBuilderName = null;
        }

        boolean childTreeNode = false;
        boolean childTreeNodeIdent = false;
        String keyTypeName = null;
        if (getType().getImplements().contains(BindingTypes.TREE_CHILD_NODE)) {
            childTreeNode = true;
            if (getType().getImplements().contains(BindingTypes.IDENTIFIABLE)) {
                childTreeNodeIdent = true;
                final ParameterizedType pType = (ParameterizedType) getType().getImplements().get(getType()
                    .getImplements().indexOf(BindingTypes.IDENTIFIABLE));
                keyTypeName = pType.getActualTypeArguments()[0].getName();
            }
        }

        getImportedNames().put("augmentation", importedName(Augmentation.class));
        getImportedNames().put("classInstMap", importedName(ClassToInstanceMap.class));

        final String constants = constantsTemplate.render(getType(), getImportedNames(), this::importedName, false).body();

        // list for generate copy constructor
        final String copyConstructorHelper = generateListForCopyConstructor();
        List<String> getterMethods = new ArrayList<>(Collections2.transform(properties, this::getterMethod));

        return builderTemplate.render(getType(), properties, getImportedNames(), importedNamesForProperties, augmentField,
            copyConstructorHelper, getterMethods, parentTypeForBuilderName, childTreeNode, childTreeNodeIdent,
            keyTypeName, instantiable, constants).body();
    }

    private String generateListForCopyConstructor() {
        final List allProps = new ArrayList<>(properties);
        final boolean isList = implementsIfc(getType(),
            Types.parameterizedTypeFor(Types.typeForClass(Identifiable.class), getType()));
        final Type keyType = getKey(getType());
        if (isList && keyType != null) {
            final List<GeneratedProperty> keyProps = ((GeneratedTransferObject) keyType).getProperties();
            for (GeneratedProperty keyProp : keyProps) {
                removeProperty(allProps, keyProp.getName());
            }
            removeProperty(allProps, "key");
            getImportedNames().put("keyTypeConstructor", importedName(keyType));
            return builderConstructorHelperTemplate.render(allProps, keyProps, getImportedNames(), getPropertyList(keyProps))
                    .body();
        }
        return builderConstructorHelperTemplate.render(allProps, null, getImportedNames(), null).body();
    }

    private Type getKey(final GeneratedType genType) {
        for (MethodSignature methodSignature : genType.getMethodDefinitions()) {
            if ("getKey".equals(methodSignature.getName())) {
                return methodSignature.getReturnType();
            }
        }
        return null;
    }

    private boolean implementsIfc(final GeneratedType type, final Type impl) {
        return type.getImplements().contains(impl);
    }

    private void removeProperty(final Collection<GeneratedProperty> properties, final String name) {
        for (final GeneratedProperty property : properties) {
            if (name.equals(property.getName())) {
                properties.remove(property);
                break;
            }
        }
    }
}