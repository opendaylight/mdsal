/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.generator.impl.BindingSchemaContextUtils;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime Context for Java YANG Binding classes
 *
 * <p>Runtime Context provides additional insight in Java YANG Binding,
 * binding classes and underlying YANG schema, it contains
 * runtime information, which could not be derived from generated
 * classes alone using {@link org.opendaylight.yangtools.yang.binding.util.BindingReflections}.
 *
 * <p>Some of this information are for example list of all available
 * children for cases {@link #getChoiceCaseChildren(DataNodeContainer)}, since
 * choices are augmentable and new choices may be introduced by additional models.
 *
 * <p>Same goes for all possible augmentations.
 */
public final class BindingRuntimeContext implements Immutable {

    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeContext.class);
    private static final char DOT = '.';

    private final BindingRuntimeTypes runtimeTypes;
    private final ClassLoadingStrategy strategy;
    private final SchemaContext schemaContext;

    private final LoadingCache<QName, Class<?>> identityClasses = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<QName, Class<?>>() {
            @Override
            public Class<?> load(final QName key) {
                final java.util.Optional<Type> identityType = runtimeTypes.findIdentity(key);
                checkArgument(identityType.isPresent(), "Supplied QName %s is not a valid identity", key);
                try {
                    return strategy.loadClass(identityType.get());
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + identityType + "was not found.", e);
                }
            }
        });

    private BindingRuntimeContext(final ClassLoadingStrategy strategy, final SchemaContext schema) {
        this.strategy = strategy;
        this.schemaContext = schema;
        runtimeTypes = new BindingGeneratorImpl().generateTypeMapping(schema);
    }

    /**
     * Creates Binding Runtime Context from supplied class loading strategy and schema context.
     *
     * @param strategy Class loading strategy to retrieve generated Binding classes
     * @param ctx Schema Context which describes YANG model and to which Binding classes should be mapped
     * @return Instance of BindingRuntimeContext for supplied schema context.
     */
    public static BindingRuntimeContext create(final ClassLoadingStrategy strategy, final SchemaContext ctx) {
        return new BindingRuntimeContext(strategy, ctx);
    }

    /**
     * Returns a class loading strategy associated with this binding runtime context
     * which is used to load classes.
     *
     * @return Class loading strategy.
     */
    public ClassLoadingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Returns an stable immutable view of schema context associated with this Binding runtime context.
     *
     * @return stable view of schema context
     */
    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    /**
     * Returns schema of augmentation.
     *
     * <p>Returned schema is schema definition from which augmentation class was generated.
     * This schema is isolated from other augmentations. This means it contains
     * augmentation definition as was present in original YANG module.
     *
     * <p>Children of returned schema does not contain any additional augmentations,
     * which may be present in runtime for them, thus returned schema is unsuitable
     * for use for validation of data.
     *
     * <p>For retrieving {@link AugmentationSchemaNode}, which will contains
     * full model for child nodes, you should use method
     * {@link #getResolvedAugmentationSchema(DataNodeContainer, Class)}
     * which will return augmentation schema derived from supplied augmentation target
     * schema.
     *
     * @param augClass Augmentation class
     * @return Schema of augmentation or null if augmentaiton is not known in this context
     * @throws IllegalArgumentException If supplied class is not an augmentation
     */
    public @Nullable AugmentationSchemaNode getAugmentationDefinition(final Class<?> augClass) {
        checkArgument(Augmentation.class.isAssignableFrom(augClass),
            "Class %s does not represent augmentation", augClass);
        return runtimeTypes.findAugmentation(referencedType(augClass)).orElse(null);
    }

    /**
     * Returns defining {@link DataSchemaNode} for supplied class.
     *
     * <p>Returned schema is schema definition from which class was generated.
     * This schema may be isolated from augmentations, if supplied class
     * represent node, which was child of grouping or augmentation.
     *
     * <p>For getting augmentation schema from augmentation class use
     * {@link #getAugmentationDefinition(Class)} instead.
     *
     * @param cls Class which represents list, container, choice or case.
     * @return Schema node, from which class was generated.
     */
    public DataSchemaNode getSchemaDefinition(final Class<?> cls) {
        checkArgument(!Augmentation.class.isAssignableFrom(cls), "Supplied class must not be augmentation (%s is)",
            cls);
        return (DataSchemaNode) runtimeTypes.findSchema(referencedType(cls)).orElse(null);
    }

    public Entry<AugmentationIdentifier, AugmentationSchemaNode> getResolvedAugmentationSchema(
            final DataNodeContainer target, final Class<? extends Augmentation<?>> aug) {
        final AugmentationSchemaNode origSchema = getAugmentationDefinition(aug);
        checkArgument(origSchema != null, "Augmentation %s is not known in current schema context", aug);
        /*
         * FIXME: Validate augmentation schema lookup
         *
         * Currently this algorithm, does not verify if instantiated child nodes
         * are real one derived from augmentation schema. The problem with
         * full validation is, if user used copy builders, he may use
         * augmentation which was generated for different place.
         *
         * If this augmentations have same definition, we emit same identifier
         * with data and it is up to underlying user to validate data.
         *
         */
        final Set<QName> childNames = new HashSet<>();
        final Set<DataSchemaNode> realChilds = new HashSet<>();
        for (final DataSchemaNode child : origSchema.getChildNodes()) {
            final DataSchemaNode dataChildQNname = target.getDataChildByName(child.getQName());
            final String childLocalName = child.getQName().getLocalName();
            if (dataChildQNname == null) {
                for (DataSchemaNode dataSchemaNode : target.getChildNodes()) {
                    if (childLocalName.equals(dataSchemaNode.getQName().getLocalName())) {
                        realChilds.add(dataSchemaNode);
                        childNames.add(dataSchemaNode.getQName());
                    }
                }
            } else {
                realChilds.add(dataChildQNname);
                childNames.add(child.getQName());
            }
        }

        final AugmentationIdentifier identifier = new AugmentationIdentifier(childNames);
        final AugmentationSchemaNode proxy = new EffectiveAugmentationSchema(origSchema, realChilds);
        return new SimpleEntry<>(identifier, proxy);
    }

    /**
     * Returns resolved case schema for supplied class.
     *
     * @param schema Resolved parent choice schema
     * @param childClass Class representing case.
     * @return Optionally a resolved case schema, absent if the choice is not legal in
     *         the given context.
     * @throws IllegalArgumentException If supplied class does not represent case.
     */
    public Optional<CaseSchemaNode> getCaseSchemaDefinition(final ChoiceSchemaNode schema, final Class<?> childClass) {
        final DataSchemaNode origSchema = getSchemaDefinition(childClass);
        checkArgument(origSchema instanceof CaseSchemaNode, "Supplied schema %s is not case.", origSchema);

        /* FIXME: Make sure that if there are multiple augmentations of same
         * named case, with same structure we treat it as equals
         * this is due property of Binding specification and copy builders
         * that user may be unaware that he is using incorrect case
         * which was generated for choice inside grouping.
         */
        final Optional<CaseSchemaNode> found = BindingSchemaContextUtils.findInstantiatedCase(schema,
                (CaseSchemaNode) origSchema);
        return found;
    }

    private static Type referencedType(final Class<?> type) {
        return new ReferencedTypeImpl(JavaTypeName.create(type));
    }

    /**
     * Returns schema ({@link DataSchemaNode}, {@link AugmentationSchemaNode} or {@link TypeDefinition})
     * from which supplied class was generated. Returned schema may be augmented with
     * additional information, which was not available at compile type
     * (e.g. third party augmentations).
     *
     * @param type Binding Class for which schema should be retrieved.
     * @return Instance of generated type (definition of Java API), along with
     *     {@link DataSchemaNode}, {@link AugmentationSchemaNode} or {@link TypeDefinition}
     *     which was used to generate supplied class.
     */
    public Entry<GeneratedType, WithStatus> getTypeWithSchema(final Class<?> type) {
        return getTypeWithSchema(referencedType(type));
    }

    private Entry<GeneratedType, WithStatus> getTypeWithSchema(final Type referencedType) {
        final WithStatus schema = runtimeTypes.findSchema(referencedType).orElseThrow(
            () -> new NullPointerException("Failed to find schema for type " + referencedType));
        final Type definedType = runtimeTypes.findType(schema).orElseThrow(
            () -> new NullPointerException("Failed to find defined type for " + referencedType + " schema " + schema));

        if (definedType instanceof GeneratedTypeBuilder) {
            return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).build(), schema);
        }
        checkArgument(definedType instanceof GeneratedType, "Type %s is not a GeneratedType", referencedType);
        return new SimpleEntry<>((GeneratedType) definedType, schema);
    }

    public ImmutableMap<Type, Entry<Type, Type>> getChoiceCaseChildren(final DataNodeContainer schema) {
        final Map<Type, Entry<Type, Type>> childToCase = new HashMap<>();

        for (final ChoiceSchemaNode choice :  Iterables.filter(schema.getChildNodes(), ChoiceSchemaNode.class)) {
            final ChoiceSchemaNode originalChoice = getOriginalSchema(choice);
            final java.util.Optional<Type> optType = runtimeTypes.findType(originalChoice);
            checkState(optType.isPresent(), "Failed to find generated type for choice %s", originalChoice);
            final Type choiceType = optType.get();

            for (Type caze : runtimeTypes.findCases(referencedType(choiceType))) {
                final Entry<Type,Type> caseIdentifier = new SimpleEntry<>(choiceType, caze);
                final HashSet<Type> caseChildren = new HashSet<>();
                if (caze instanceof GeneratedTypeBuilder) {
                    caze = ((GeneratedTypeBuilder) caze).build();
                }
                collectAllContainerTypes((GeneratedType) caze, caseChildren);
                for (final Type caseChild : caseChildren) {
                    childToCase.put(caseChild, caseIdentifier);
                }
            }
        }
        return ImmutableMap.copyOf(childToCase);
    }

    /**
     * Map enum constants: yang - java.
     *
     * @param enumClass enum generated class
     * @return mapped enum constants from yang with their corresponding values in generated binding classes
     */
    public BiMap<String, String> getEnumMapping(final Class<?> enumClass) {
        final Entry<GeneratedType, WithStatus> typeWithSchema = getTypeWithSchema(enumClass);
        return getEnumMapping(typeWithSchema);
    }

    /**
     * Map enum constants: yang - java.
     *
     * @param enumClassName enum generated class name
     * @return mapped enum constants from yang with their corresponding values in generated binding classes
     */
    public BiMap<String, String> getEnumMapping(final String enumClassName) {
        return getEnumMapping(findTypeWithSchema(enumClassName));
    }

    private Entry<GeneratedType, WithStatus> findTypeWithSchema(final String className) {
        // All we have is a straight FQCN, which we need to split into a hierarchical JavaTypeName. This involves
        // some amount of guesswork -- we do that by peeling components at the dot and trying out, e.g. given
        // "foo.bar.baz.Foo.Bar.Baz" we end up trying:
        // "foo.bar.baz.Foo.Bar" + "Baz"
        // "foo.bar.baz.Foo" + Bar" + "Baz"
        // "foo.bar.baz" + Foo" + Bar" + "Baz"
        //
        // And see which one sticks. We cannot rely on capital letters, as they can be used in package names, too.
        // Nested classes are not common, so we should be arriving at the result pretty quickly.
        final List<String> components = new ArrayList<>();
        String packageName = className;

        for (int lastDot = packageName.lastIndexOf(DOT); lastDot != -1; lastDot = packageName.lastIndexOf(DOT)) {
            components.add(packageName.substring(lastDot + 1));
            packageName = packageName.substring(0, lastDot);

            final Iterator<String> it = components.iterator();
            JavaTypeName name = JavaTypeName.create(packageName, it.next());
            while (it.hasNext()) {
                name = name.createEnclosed(it.next());
            }

            final Type type = new ReferencedTypeImpl(name);
            final java.util.Optional<WithStatus> optSchema = runtimeTypes.findSchema(type);
            if (!optSchema.isPresent()) {
                continue;
            }

            final WithStatus schema = optSchema.get();
            final java.util.Optional<Type> optDefinedType =  runtimeTypes.findType(schema);
            if (!optDefinedType.isPresent()) {
                continue;
            }

            final Type definedType = optDefinedType.get();
            if (definedType instanceof GeneratedTypeBuilder) {
                return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).build(), schema);
            }
            checkArgument(definedType instanceof GeneratedType, "Type %s is not a GeneratedType", className);
            return new SimpleEntry<>((GeneratedType) definedType, schema);
        }

        throw new IllegalArgumentException("Failed to find type for " + className);
    }

    private static BiMap<String, String> getEnumMapping(final Entry<GeneratedType, WithStatus> typeWithSchema) {
        final TypeDefinition<?> typeDef = (TypeDefinition<?>) typeWithSchema.getValue();

        Preconditions.checkArgument(typeDef instanceof EnumTypeDefinition);
        final EnumTypeDefinition enumType = (EnumTypeDefinition) typeDef;

        final HashBiMap<String, String> mappedEnums = HashBiMap.create();

        for (final EnumTypeDefinition.EnumPair enumPair : enumType.getValues()) {
            mappedEnums.put(enumPair.getName(), BindingMapping.getClassName(enumPair.getName()));
        }

        // TODO cache these maps for future use
        return mappedEnums;
    }

    public Set<Class<?>> getCases(final Class<?> choice) {
        final Collection<Type> cazes = runtimeTypes.findCases(referencedType(choice));
        final Set<Class<?>> ret = new HashSet<>(cazes.size());
        for (final Type caze : cazes) {
            try {
                final Class<?> c = strategy.loadClass(caze);
                ret.add(c);
            } catch (final ClassNotFoundException e) {
                LOG.warn("Failed to load class for case {}, ignoring it", caze, e);
            }
        }
        return ret;
    }

    public Class<?> getClassForSchema(final SchemaNode childSchema) {
        final SchemaNode origSchema = getOriginalSchema(childSchema);
        final java.util.Optional<Type> clazzType = runtimeTypes.findType(origSchema);
        checkArgument(clazzType.isPresent(), "Failed to find binding type for %s (original %s)",
            childSchema, origSchema);

        try {
            return strategy.loadClass(clazzType.get());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public ImmutableMap<AugmentationIdentifier, Type> getAvailableAugmentationTypes(final DataNodeContainer container) {
        final Map<AugmentationIdentifier, Type> identifierToType = new HashMap<>();
        if (container instanceof AugmentationTarget) {
            final Set<AugmentationSchemaNode> augments = ((AugmentationTarget) container).getAvailableAugmentations();
            for (final AugmentationSchemaNode augment : augments) {
                // Augmentation must have child nodes if is to be used with Binding classes
                AugmentationSchemaNode augOrig = augment;
                while (augOrig.getOriginalDefinition().isPresent()) {
                    augOrig = augOrig.getOriginalDefinition().get();
                }

                if (!augment.getChildNodes().isEmpty()) {
                    final java.util.Optional<Type> augType = runtimeTypes.findType(augOrig);
                    if (augType.isPresent()) {
                        identifierToType.put(getAugmentationIdentifier(augment), augType.get());
                    }
                }
            }
        }

        return ImmutableMap.copyOf(identifierToType);
    }

    private static AugmentationIdentifier getAugmentationIdentifier(final AugmentationSchemaNode augment) {
        final Set<QName> childNames = new HashSet<>();
        for (final DataSchemaNode child : augment.getChildNodes()) {
            childNames.add(child.getQName());
        }
        return new AugmentationIdentifier(childNames);
    }

    private static Type referencedType(final Type type) {
        if (type instanceof ReferencedTypeImpl) {
            return type;
        }
        return new ReferencedTypeImpl(type.getIdentifier());
    }

    private static Set<Type> collectAllContainerTypes(final GeneratedType type, final Set<Type> collection) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            Type childType = definition.getReturnType();
            if (childType instanceof ParameterizedType) {
                childType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            }
            if (childType instanceof GeneratedType || childType instanceof GeneratedTypeBuilder) {
                collection.add(referencedType(childType));
            }
        }
        for (final Type parent : type.getImplements()) {
            if (parent instanceof GeneratedType) {
                collectAllContainerTypes((GeneratedType) parent, collection);
            }
        }
        return collection;
    }

    private static <T extends SchemaNode> T getOriginalSchema(final T choice) {
        @SuppressWarnings("unchecked")
        final T original = (T) SchemaNodeUtils.getRootOriginalIfPossible(choice);
        if (original != null) {
            return original;
        }
        return choice;
    }

    public Class<?> getIdentityClass(final QName input) {
        return identityClasses.getUnchecked(input);
    }
}
