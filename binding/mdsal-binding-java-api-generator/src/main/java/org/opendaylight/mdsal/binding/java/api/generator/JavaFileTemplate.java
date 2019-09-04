/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME;

import com.google.common.collect.ImmutableSortedSet;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.Augmentable;

/**
 * Base Java file template. Contains a non-null type and imports which the generated code refers to.
 */
class JavaFileTemplate {
    static final JavaTypeName NONNULL = JavaTypeName.create("org.eclipse.jdt.annotation", "NonNull");
    static final JavaTypeName NULLABLE = JavaTypeName.create("org.eclipse.jdt.annotation", "Nullable");
    static final JavaTypeName DEPRECATED = JavaTypeName.create(Deprecated.class);
    static final JavaTypeName OVERRIDE = JavaTypeName.create(Override.class);
    static final JavaTypeName SUPPRESS_WARNINGS = JavaTypeName.create(SuppressWarnings.class);

    private static final Comparator<MethodSignature> METHOD_COMPARATOR = new AlphabeticallyTypeMemberComparator<>();
    private static final Type AUGMENTATION_RET_TYPE;

    static {
        final Method m;
        try {
            m = Augmentable.class.getDeclaredMethod(AUGMENTABLE_AUGMENTATION_NAME, Class.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }

        AUGMENTATION_RET_TYPE = new ReferencedTypeImpl(JavaTypeName.create(m.getReturnType()));
    }

    private final AbstractJavaGeneratedType javaType;
    private final GeneratedType type;

    JavaFileTemplate(final GeneratedType type) {
        this(new TopLevelJavaGeneratedType(type), type);
    }

    JavaFileTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type) {
        this.javaType = requireNonNull(javaType);
        this.type = requireNonNull(type);
    }

    final AbstractJavaGeneratedType javaType() {
        return javaType;
    }

    final GeneratedType type() {
        return type;
    }

    final GeneratedProperty findProperty(final GeneratedTransferObject gto, final String name) {
        final Optional<GeneratedProperty> optProp = gto.getProperties().stream()
                .filter(prop -> prop.getName().equals(name)).findFirst();
        if (optProp.isPresent()) {
            return optProp.get();
        }

        final GeneratedTransferObject parent = gto.getSuperType();
        return parent != null ? findProperty(parent, name) : null;
    }

    final String generateImportBlock() {
        verify(javaType instanceof TopLevelJavaGeneratedType);
        return ((TopLevelJavaGeneratedType) javaType).imports().map(name -> "import " + name + ";\n")
                .collect(Collectors.joining());
    }

    final String importedJavadocName(final Type intype) {
        return importedName(intype instanceof ParameterizedType ? ((ParameterizedType) intype).getRawType() : intype);
    }

    final String importedName(final Type intype) {
        return javaType.getReferenceString(intype);
    }

    final String importedName(final Type intype, final String... annotations) {
        return javaType.getReferenceString(intype, annotations);
    }

    final String importedName(final Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    final String importedName(final JavaTypeName intype) {
        return javaType.getReferenceString(intype);
    }

    final String importedNonNull(final Type intype) {
        return importedName(intype, importedName(NONNULL));
    }

    final String importedNullable(final Type intype) {
        return importedName(intype, importedName(NULLABLE));
    }

    final void addImport(final Class<?> cls) {
        javaType.getReferenceString(JavaTypeName.create(cls));
    }

    // Exposed for BuilderTemplate
    boolean isLocalInnerClass(final JavaTypeName name) {
        final Optional<JavaTypeName> optEnc = name.immediatelyEnclosingClass();
        return optEnc.isPresent() && type.getIdentifier().equals(optEnc.get());
    }

    final CharSequence generateInnerClass(final GeneratedType innerClass) {
        if (!(innerClass instanceof GeneratedTransferObject)) {
            return "";
        }

        final GeneratedTransferObject gto = (GeneratedTransferObject) innerClass;
        final NestedJavaGeneratedType innerJavaType = javaType.getEnclosedType(innerClass.getIdentifier());
        return gto.isUnionType() ? new UnionTemplate(innerJavaType, gto).generateAsInnerClass()
                : new ClassTemplate(innerJavaType, gto).generateAsInnerClass();
    }

    /**
     * Return imported name of java.util class, whose hashCode/equals methods we want to invoke on the property. Returns
     * {@link Arrays} if the property is an array, {@link Objects} otherwise.
     *
     * @param property Generated property
     * @return Imported class name
     */
    final String importedUtilClass(final GeneratedProperty property) {
        return importedName(property.getReturnType().getName().indexOf('[') != -1 ? Arrays.class : Objects.class);
    }

    /**
     * Run type analysis, which results in identification of the augmentable type, as well as all methods available
     * to the type, expressed as properties.
     */
    static Entry<Type, Set<GeneratedProperty>> analyzeTypeHierarchy(final GeneratedType type) {
        final Set<MethodSignature> methods = new LinkedHashSet<>();
        final Type augmentType = createMethods(type, methods);
        final Set<MethodSignature> sortedMethods = ImmutableSortedSet.orderedBy(METHOD_COMPARATOR).addAll(methods)
                .build();

        return new SimpleImmutableEntry<>(augmentType, propertiesFromMethods(sortedMethods));
    }

    static final Restrictions restrictionsForSetter(final Type actualType) {
        return actualType instanceof GeneratedType ? null : getRestrictions(actualType);
    }

    static final Restrictions getRestrictions(final Type type) {
        if (type instanceof ConcreteType) {
            return ((ConcreteType) type).getRestrictions();
        }
        if (type instanceof GeneratedTransferObject) {
            return ((GeneratedTransferObject) type).getRestrictions();
        }
        return null;
    }

    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     *
     * @returns set of method signature instances
     */
    private static ParameterizedType createMethods(final GeneratedType type, final Set<MethodSignature> methods) {
        methods.addAll(type.getMethodDefinitions());
        return collectImplementedMethods(type, methods, type.getImplements());
    }

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code>
     * and recursively their implemented interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    private static ParameterizedType collectImplementedMethods(final GeneratedType type,
            final Set<MethodSignature> methods, final List<Type> implementedIfcs) {
        if (implementedIfcs == null || implementedIfcs.isEmpty()) {
            return null;
        }

        ParameterizedType augmentType = null;
        for (Type implementedIfc : implementedIfcs) {
            if (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject)) {
                final GeneratedType ifc = (GeneratedType) implementedIfc;
                methods.addAll(ifc.getMethodDefinitions());

                final ParameterizedType t = collectImplementedMethods(type, methods, ifc.getImplements());
                if (t != null && augmentType == null) {
                    augmentType = t;
                }
            } else if (Augmentable.class.getName().equals(implementedIfc.getFullyQualifiedName())) {
                augmentType = Types.parameterizedTypeFor(AUGMENTATION_RET_TYPE,
                    new ReferencedTypeImpl(type.getIdentifier()));
            }
        }

        return augmentType;
    }

    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    private static Set<GeneratedProperty> propertiesFromMethods(final Collection<MethodSignature> methods) {
        if (methods == null || methods.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<GeneratedProperty> result = new LinkedHashSet<>();
        for (MethodSignature m : methods) {
            final GeneratedProperty createdField = propertyFromGetter(m);
            if (createdField != null) {
                result.add(createdField);
            }
        }
        return result;
    }

    /**
     * Creates generated property instance from the getter <code>method</code> name and return type.
     *
     * @param method method signature from which is the method name and return type obtained
     * @return generated property instance for the getter <code>method</code>
     * @throws IllegalArgumentException <ul>
     *  <li>if the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> is empty</li>
     *  <li>if the return type of the <code>method</code> equals <code>null</code></li>
     * </ul>
     */
    private static GeneratedProperty propertyFromGetter(final MethodSignature method) {
        checkArgument(method != null);
        checkArgument(method.getReturnType() != null);
        checkArgument(method.getName() != null);
        checkArgument(!method.getName().isEmpty());
        if (method.isDefault()) {
            return null;
        }
        final String prefix = BindingMapping.getGetterPrefix(Types.BOOLEAN.equals(method.getReturnType()));
        if (!method.getName().startsWith(prefix)) {
            return null;
        }

        final String fieldName = StringExtensions.toFirstLower(method.getName().substring(prefix.length()));
        final GeneratedTOBuilder tmpGenTO = new CodegenGeneratedTOBuilder(JavaTypeName.create("foo", "foo"));
        tmpGenTO.addProperty(fieldName).setReturnType(method.getReturnType());
        return tmpGenTO.build().getProperties().get(0);
    }
}
