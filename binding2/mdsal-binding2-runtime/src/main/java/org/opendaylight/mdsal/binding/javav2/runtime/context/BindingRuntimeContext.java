/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.context;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.runtime.context.util.BindingSchemaContextUtils;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime Context for Java YANG Binding classes
 *
 * <p>
 * Runtime Context provides additional insight in Java YANG Binding, binding
 * classes and underlying YANG schema, it contains runtime information, which
 * could not be derived from generated classes alone using
 * {@link BindingReflections}.
 * <p>
 * Some of this information are for example list of all available children for
 * cases {@link #getChoiceCaseChildren(DataNodeContainer)}, since choices are
 * augmentable and new choices may be introduced by additional models.
 * <p>
 * Same goes for all possible augmentations.
 *
 */
@Beta
public class BindingRuntimeContext implements Immutable {

    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeContext.class);
    private static final char DOT = '.';
    private final ClassLoadingStrategy strategy;
    private final SchemaContext schemaContext;

    private final Map<Type, AugmentationSchemaNode> augmentationToSchema = new HashMap<>();
    private final BiMap<Type, Object> typeToDefiningSchema = HashBiMap.create();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final Map<QName, Type> identities = new HashMap<>();

    private final LoadingCache<QName, Class<?>> identityClasses = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<QName, Class<?>>() {
            @Override
            public Class<?> load(final QName key) {
                final Type identityType = BindingRuntimeContext.this.identities.get(key);
                Preconditions.checkArgument(identityType != null, "Supplied QName %s is not a valid identity", key);
                try {
                    return BindingRuntimeContext.this.strategy.loadClass(identityType);
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + identityType + "was not found.", e);
                }
            }
        });

    private BindingRuntimeContext(final ClassLoadingStrategy strategy, final SchemaContext schema) {
        this.strategy = strategy;
        this.schemaContext = schema;

        final BindingGeneratorImpl generator = new BindingGeneratorImpl(false);
        final Map<Module, ModuleContext> modules = generator.getModuleContexts(this.schemaContext);

        for (final ModuleContext ctx : modules.values()) {
            this.augmentationToSchema.putAll(ctx.getTypeToAugmentation());
            this.typeToDefiningSchema.putAll(ctx.getTypeToSchema());

            ctx.getTypedefs();
            this.choiceToCases.putAll(ctx.getChoiceToCases());
            this.identities.putAll(ctx.getIdentities());
        }
    }

    /**
     * Creates Binding Runtime Context from supplied class loading strategy and
     * schema context.
     *
     * @param strategy
     *            - class loading strategy to retrieve generated Binding classes
     * @param ctx
     *            - schema context which describes YANG model and to which
     *            Binding classes should be mapped
     * @return Instance of BindingRuntimeContext for supplied schema context.
     */
    public static final BindingRuntimeContext create(final ClassLoadingStrategy strategy, final SchemaContext ctx) {
        return new BindingRuntimeContext(strategy, ctx);
    }

    /**
     * Returns a class loading strategy associated with this binding runtime context
     * which is used to load classes.
     *
     * @return Class loading strategy.
     */
    public ClassLoadingStrategy getStrategy() {
        return this.strategy;
    }

    /**
     * Returns an stable immutable view of schema context associated with this Binding runtime context.
     *
     * @return stable view of schema context
     */
    public SchemaContext getSchemaContext() {
        return this.schemaContext;
    }

    /**
     * Returns schema of augmentation
     * <p>
     * Returned schema is schema definition from which augmentation class was
     * generated. This schema is isolated from other augmentations. This means
     * it contains augmentation definition as was present in original YANG
     * module.
     * <p>
     * Children of returned schema does not contain any additional
     * augmentations, which may be present in runtime for them, thus returned
     * schema is unsuitable for use for validation of data.
     * <p>
     * For retrieving {@link AugmentationSchemaNode}, which will contains full model
     * for child nodes, you should use method
     * {@link #getResolvedAugmentationSchema(DataNodeContainer, Class)} which
     * will return augmentation schema derived from supplied augmentation target
     * schema.
     *
     * @param augClass
     *            - ugmentation class
     * @return Schema of augmentation or null if augmentaiton is not known in
     *         this context
     * @throws IllegalArgumentException
     *             - if supplied class is not an augmentation
     */
    public @Nullable AugmentationSchemaNode getAugmentationDefinition(final Class<?> augClass) {
        Preconditions.checkArgument(Augmentation.class.isAssignableFrom(augClass), "Class %s does not represent augmentation", augClass);
        return this.augmentationToSchema.get(referencedType(augClass));
    }

    /**
     * Returns defining {@link DataSchemaNode} for supplied class.
     *
     * <p>
     * Returned schema is schema definition from which class was generated. This
     * schema may be isolated from augmentations, if supplied class represent
     * node, which was child of grouping or augmentation.
     * <p>
     * For getting augmentation schema from augmentation class use
     * {@link #getAugmentationDefinition(Class)} instead.
     *
     * @param cls
     *            - class which represents list, container, choice or case.
     * @return Schema node, from which class was generated.
     */
    public DataSchemaNode getSchemaDefinition(final Class<?> cls) {
        Preconditions.checkArgument(!Augmentation.class.isAssignableFrom(cls),"Supplied class must not be augmentation (%s is)", cls);
        return (DataSchemaNode) this.typeToDefiningSchema.get(referencedType(cls));
    }

    /**
     * Returns defining {@link AugmentationSchemaNode} of target for supplied class.
     *
     * @param target
     *            - {@link DataNodeContainer}
     * @param aug
     *            - supplied class
     * @return entry of {@link AugmentationSchemaNode} according to its identifier
     *         {@link AugmentationIdentifier}
     */
    public Entry<AugmentationIdentifier, AugmentationSchemaNode> getResolvedAugmentationSchema(
            final DataNodeContainer target, final Class<? extends Augmentation<?>> aug) {
        final AugmentationSchemaNode origSchema = getAugmentationDefinition(aug);
        Preconditions.checkArgument(origSchema != null, "Augmentation %s is not known in current schema context",aug);
        /*
         * FIXME: Validate augmentation schema lookup
         *
         * Currently this algorithm, does not verify if instantiated child nodes
         * are real one derived from augmentation schema. The problem with full
         * validation is, if user used copy builders, he may use augmentation
         * which was generated for different place.
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
                for (final DataSchemaNode dataSchemaNode : target.getChildNodes()) {
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
     * Returns resolved case schema for supplied class
     *
     * @param schema
     *            - resolved parent choice schema
     * @param childClass
     *            - class representing case.
     * @return Optionally a resolved case schema, absent if the choice is not
     *         legal in the given context.
     * @throws IllegalArgumentException
     *             - if supplied class does not represent case
     */
    public Optional<CaseSchemaNode> getCaseSchemaDefinition(final ChoiceSchemaNode schema, final Class<?> childClass) throws IllegalArgumentException {
        final DataSchemaNode origSchema = getSchemaDefinition(childClass);
        Preconditions.checkArgument(origSchema instanceof CaseSchemaNode, "Supplied schema %s is not case.", origSchema);

        /*
         * FIXME: Make sure that if there are multiple augmentations of same
         * named case, with same structure we treat it as equals this is due
         * property of Binding specification and copy builders that user may be
         * unaware that he is using incorrect case which was generated for
         * choice inside grouping.
         */
        final Optional<CaseSchemaNode> found = BindingSchemaContextUtils.findInstantiatedCase(schema,
                (CaseSchemaNode) origSchema);
        return found;
    }

    private static Type referencedType(final Class<?> type) {
        return new ReferencedTypeImpl(type.getPackage().getName(), type.getSimpleName(), true, null);
    }

    static Type referencedType(final String type) {
        final int packageClassSeparator = type.lastIndexOf(DOT);
        return new ReferencedTypeImpl(type.substring(0, packageClassSeparator),
            type.substring(packageClassSeparator + 1), true, null);
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
    public Entry<GeneratedType, Object> getTypeWithSchema(final Class<?> type) {
        return getTypeWithSchema(referencedType(type));
    }

    public Entry<GeneratedType, Object> getTypeWithSchema(final String type) {
        return getTypeWithSchema(referencedType(type));
    }

    private Entry<GeneratedType, Object> getTypeWithSchema(final Type referencedType) {
        final Object schema = this.typeToDefiningSchema.get(referencedType);
        Preconditions.checkNotNull(schema, "Failed to find schema for type %s", referencedType);

        final Type definedType = this.typeToDefiningSchema.inverse().get(schema);
        Preconditions.checkNotNull(definedType, "Failed to find defined type for %s schema %s", referencedType, schema);

        if (definedType instanceof GeneratedTypeBuilder) {
            return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).toInstance(), schema);
        }
        Preconditions.checkArgument(definedType instanceof GeneratedType,"Type {} is not GeneratedType", referencedType);
        return new SimpleEntry<>((GeneratedType) definedType,schema);
    }

    public ImmutableMap<Type, Entry<Type, Type>> getChoiceCaseChildren(final DataNodeContainer schema) {
        final Map<Type,Entry<Type,Type>> childToCase = new HashMap<>();
        for (final ChoiceSchemaNode choice :  FluentIterable.from(schema.getChildNodes()).filter(ChoiceSchemaNode.class)) {
            final ChoiceSchemaNode originalChoice = getOriginalSchema(choice);
            final Type choiceType = referencedType(this.typeToDefiningSchema.inverse().get(originalChoice));
            final Collection<Type> cases = this.choiceToCases.get(choiceType);

            for (Type caze : cases) {
                final Entry<Type,Type> caseIdentifier = new SimpleEntry<>(choiceType,caze);
                final HashSet<Type> caseChildren = new HashSet<>();
                if (caze instanceof GeneratedTypeBuilder) {
                    caze = ((GeneratedTypeBuilder) caze).toInstance();
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
     * Map enum constants: yang - java
     *
     * @param enumClass enum generated class
     * @return mapped enum constants from yang with their corresponding values in generated binding classes
     */
    public BiMap<String, String> getEnumMapping(final Class<?> enumClass) {
        final Entry<GeneratedType, Object> typeWithSchema = getTypeWithSchema(enumClass);
        return getEnumMapping(typeWithSchema);
    }

    /**
     * See {@link #getEnumMapping(Class)}}
     */
    public BiMap<String, String> getEnumMapping(final String enumClass) {
        final Entry<GeneratedType, Object> typeWithSchema = getTypeWithSchema(enumClass);
        return getEnumMapping(typeWithSchema);
    }

    private static BiMap<String, String> getEnumMapping(final Entry<GeneratedType, Object> typeWithSchema) {
        final TypeDefinition<?> typeDef = (TypeDefinition<?>) typeWithSchema.getValue();

        Preconditions.checkArgument(typeDef instanceof EnumTypeDefinition);
        final EnumTypeDefinition enumType = (EnumTypeDefinition) typeDef;

        final HashBiMap<String, String> mappedEnums = HashBiMap.create();

        for (final EnumTypeDefinition.EnumPair enumPair : enumType.getValues()) {
            mappedEnums.put(enumPair.getName(),
                    JavaIdentifierNormalizer.normalizeSpecificIdentifier(enumPair.getName(), JavaIdentifier.CLASS));
        }

        // TODO cache these maps for future use

        return mappedEnums;
    }

    public Set<Class<?>> getCases(final Class<?> choice) {
        final Collection<Type> cazes = this.choiceToCases.get(referencedType(choice));
        final Set<Class<?>> ret = new HashSet<>(cazes.size());
        for(final Type caze : cazes) {
            try {
                final Class<?> c = this.strategy.loadClass(caze);
                ret.add(c);
            } catch (final ClassNotFoundException e) {
                LOG.warn("Failed to load class for case {}, ignoring it", caze, e);
            }
        }
        return ret;
    }

    public Class<?> getClassForSchema(final SchemaNode childSchema) {
        final SchemaNode origSchema = getOriginalSchema(childSchema);
        final Type clazzType = this.typeToDefiningSchema.inverse().get(origSchema);
        try {
            return this.strategy.loadClass(clazzType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public ImmutableMap<AugmentationIdentifier,Type> getAvailableAugmentationTypes(final DataNodeContainer container) {
        final Map<AugmentationIdentifier,Type> identifierToType = new HashMap<>();
        if (container instanceof AugmentationTarget) {
            for (final AugmentationSchemaNode augment : ((AugmentationTarget) container).getAvailableAugmentations()) {
                // Augmentation must have child nodes if is to be used with Binding classes
                AugmentationSchemaNode augOrig = augment;
                while (augOrig.getOriginalDefinition().isPresent()) {
                    augOrig = augOrig.getOriginalDefinition().get();
                }

                if (!augment.getChildNodes().isEmpty()) {
                    final Type augType = this.typeToDefiningSchema.inverse().get(augOrig);
                    if (augType != null) {
                        identifierToType.put(getAugmentationIdentifier(augment),augType);
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
        return new ReferencedTypeImpl(type.getPackageName(), type.getName(), true, null);
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
        return this.identityClasses.getUnchecked(input);
    }

    public Method findOperationMethod(final Class<?> key, final OperationDefinition operationDef)
            throws NoSuchMethodException {
        final String methodName =
                JavaIdentifierNormalizer.normalizeSpecificIdentifier(operationDef.getQName().getLocalName(),
                JavaIdentifier.METHOD);
        if (operationDef.getInput() != null && isExplicitStatement(operationDef.getInput())) {
            final Class<?> inputClz = this.getClassForSchema(operationDef.getInput());
            return key.getMethod(methodName, inputClz);
        }
        return key.getMethod(methodName);
    }

    @SuppressWarnings("rawtypes")
    private static boolean isExplicitStatement(final ContainerSchemaNode node) {
        return node instanceof EffectiveStatement
                && ((EffectiveStatement) node).getDeclared().getStatementSource() == StatementSource.DECLARATION;
    }
}
