/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD
import java.util.Optional;
=======
import java.util.Map.Entry;
import java.util.Set;
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentation;
<<<<<<< HEAD
=======
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataContainer;
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
<<<<<<< HEAD
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
=======
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation detail. It is public only due to technical reasons and may change at any time.
 */
@Beta
public abstract class DataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends AbstractDataObjectCodecContext<D, T> implements BindingDataObjectCodecTreeNode<D> {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectCodecContext.class);

    private static final VarHandle MISMATCHED_AUGMENTED;

    static {
        try {
            MISMATCHED_AUGMENTED = MethodHandles.lookup().findVarHandle(DataObjectCodecContext.class,
                "mismatchedAugmented", ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final ImmutableMap<String, ValueNodeCodecContext> leafChild;
    private final ImmutableMap<PathArgument, NodeContextSupplier> byYang;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<PathArgument, Class<?>> augmentationPathToBindingClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> augmentationBindingClassToProto;
    private final @NonNull Class<? extends CodecDataObject<?>> generatedClass;
    private final MethodHandle proxyConstructor;
    private final ImmutableSet<PathArgument> childPathArgs;
    private final ImmutableSet<Class<?>> childBindingClasses;

    // Note this the content of this field depends only of invariants expressed as this class's fields or
    // BindingRuntimeContext. It is only accessed via MISMATCHED_AUGMENTED above.
    @SuppressWarnings("unused")
    private volatile ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> mismatchedAugmented = ImmutableMap.of();

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype) {
        this(prototype, CodecItemFactory.of());
    }

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype, final CodecItemFactory itemFactory) {
        this(prototype, new CodecDataObjectAnalysis<>(prototype, itemFactory, null), null);
    }

    DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype, final Method keyMethod) {
        this(prototype, new CodecDataObjectAnalysis<>(prototype, CodecItemFactory.of(), keyMethod), keyMethod);
    }

    private DataObjectCodecContext(final DataContainerCodecPrototype<T> prototype,
            final CodecDataObjectAnalysis<T> analysis, final Method keyMethod) {
        super(prototype);

        // Inherit analysis stuff
        leafChild = analysis.leafNodes;
        proxyConstructor = analysis.proxyConstructor;
        generatedClass = analysis.generatedClass;
        byBindingArgClass = analysis.byBindingArgClass;
        byStreamClass = analysis.byStreamClass;
        byYang = analysis.byYang;

<<<<<<< HEAD
        // Deal with augmentations, which are not something we analysis provides
        final var augByYang = new HashMap<PathArgument, DataContainerCodecPrototype<?>>();
        final var augByStream = new HashMap<Class<?>, DataContainerCodecPrototype<?>>();
        for (var augment : analysis.possibleAugmentations) {
            final var augProto = loadAugmentPrototype(augment);
            if (augProto != null) {
                final var augYangArg = augProto.getYangArg();
                if (augByYang.putIfAbsent(augYangArg, augProto) == null) {
                    LOG.trace("Discovered new YANG mapping {} -> {} in {}", augYangArg, augProto, this);
                }
                final var augBindingClass = augProto.getBindingClass();
                if (augByStream.putIfAbsent(augBindingClass, augProto) == null) {
                    LOG.trace("Discovered new class mapping {} -> {} in {}", augBindingClass, augProto, this);
=======
        final ImmutableMap<Method, ValueNodeCodecContext> tmpLeaves = factory().getLeafNodes(bindingClass,
            getType().statement());
        final Map<Class<? extends DataContainer>, Method> clsToMethod = getChildrenClassToMethod(bindingClass);

        final Map<PathArgument, NodeContextSupplier> byYangBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byStreamClassBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClassBuilder = new HashMap<>();

        // Adds leaves to mapping
        final Builder<String, ValueNodeCodecContext> leafChildBuilder =
                ImmutableMap.builderWithExpectedSize(tmpLeaves.size());
        for (final ValueNodeCodecContext leaf : tmpLeaves.values()) {
            leafChildBuilder.put(leaf.getSchema().getQName().getLocalName(), leaf);
            byYangBuilder.put(leaf.getDomPathArgument(), leaf);
        }
        leafChild = leafChildBuilder.build();

        final Map<Class<?>, PropertyInfo> daoProperties = new HashMap<>();
        for (final Entry<Class<? extends DataContainer>, Method> childDataObj : clsToMethod.entrySet()) {
            final Method method = childDataObj.getValue();
            verify(!method.isDefault(), "Unexpected default method %s in %s", method, bindingClass);

            final Class<? extends DataContainer> retClass = childDataObj.getKey();
            if (OpaqueObject.class.isAssignableFrom(retClass)) {
                // Filter OpaqueObjects, they are not containers
                continue;
            }

            // Record getter method
            daoProperties.put(retClass, new PropertyInfo.Getter(method));

            final DataContainerCodecPrototype<?> childProto = loadChildPrototype(retClass);
            byStreamClassBuilder.put(childProto.getBindingClass(), childProto);
            byYangBuilder.put(childProto.getYangArg(), childProto);

            // FIXME: It really feels like we should be specializing DataContainerCodecPrototype so as to ditch
            //        createInstance() and then we could do an instanceof check instead.
            if (childProto.getType() instanceof ChoiceRuntimeType) {
                final ChoiceNodeCodecContext<?> choice = (ChoiceNodeCodecContext<?>) childProto.get();
                for (final Class<?> cazeChild : choice.getCaseChildrenClasses()) {
                    byBindingArgClassBuilder.put(cazeChild, childProto);
                }
            }
        }

        // Find all non-default nonnullFoo() methods and update the corresponding property info
        for (var entry : getChildrenClassToNonnullMethod(bindingClass).entrySet()) {
            final var method = entry.getValue();
            if (!method.isDefault()) {
                daoProperties.compute(entry.getKey(), (key, value) -> new PropertyInfo.GetterAndNonnull(
                    verifyNotNull(value, "No getter for %s", key).getterMethod(), method));
            }
        }

        byYang = ImmutableMap.copyOf(byYangBuilder);
        byStreamClass = ImmutableMap.copyOf(byStreamClassBuilder);

        // Slight footprint optimization: we do not want to copy byStreamClass, as that would force its entrySet view
        // to be instantiated. Furthermore the two maps can easily end up being equal -- hence we can reuse
        // byStreamClass for the purposes of both.
        byBindingArgClassBuilder.putAll(byStreamClassBuilder);
        byBindingArgClass = byStreamClassBuilder.equals(byBindingArgClassBuilder) ? byStreamClass
                : ImmutableMap.copyOf(byBindingArgClassBuilder);

        final List<AugmentRuntimeType> possibleAugmentations;
        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            // Verify we have the appropriate backing runtimeType
            final var type = getType();
            verify(type instanceof AugmentableRuntimeType, "Unexpected type %s backing augmenable %s", type,
                bindingClass);
            possibleAugmentations = ((AugmentableRuntimeType) type).augments();
            generatedClass = CodecDataObjectGenerator.generateAugmentable(prototype.getFactory().getLoader(),
                bindingClass, tmpLeaves, daoProperties, keyMethod);
        } else {
            possibleAugmentations = List.of();
            generatedClass = CodecDataObjectGenerator.generate(prototype.getFactory().getLoader(), bindingClass,
                tmpLeaves, daoProperties, keyMethod);
        }

        // Iterate over all possible augmentations, indexing them as needed
        final Map<PathArgument, Class<?>> augPathToBinding = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> augClassToProto = new HashMap<>();
        for (final AugmentRuntimeType augment : possibleAugmentations) {
            final DataContainerCodecPrototype<?> augProto = loadAugmentPrototype(augment);
            if (augProto != null) {
                final Class<?> augBindingClass = augProto.getBindingClass();
                for (var childPath : augProto.getChildArgs()) {
                    augPathToBinding.putIfAbsent(childPath, augBindingClass);
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
                }
                augClassToProto.putIfAbsent(augBindingClass, augProto);
            }
        }
<<<<<<< HEAD
        augmentationByYang = ImmutableMap.copyOf(augByYang);
        augmentationByStream = ImmutableMap.copyOf(augByStream);
=======
        augmentationPathToBindingClass = ImmutableMap.copyOf(augPathToBinding);
        augmentationBindingClassToProto = ImmutableMap.copyOf(augClassToProto);

        // complete set of child path argument
        childPathArgs = ImmutableSet.<PathArgument>builder()
            .addAll(byYang.keySet())
            .addAll(augmentationPathToBindingClass.keySet()).build();

        // complete set of child binding classes
        final Set<Class<?>> childClasses = new HashSet(byBindingArgClass.keySet());
        childClasses.addAll(augmentationPathToBindingClass.values());
        childBindingClasses = ImmutableSet.copyOf(childClasses);

        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(generatedClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + generatedClass, e);
        }

        proxyConstructor = ctor.asType(DATAOBJECT_TYPE);
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
    }

    @Override
    final DataContainerCodecPrototype<?> pathChildPrototype(final Class<? extends DataObject> argType) {
        // FIXME: super.pathChildPrototype(argType)
        final var child = byBindingArgClass.get(argType);
        return child != null ? child : augmentationBindingClassToProto.get(argType);
    }

    @Override
    final DataContainerCodecPrototype<?> streamChildPrototype(final Class<?> childClass) {
        // FIXME: super.childByStreamClass(childClass)
        final var child = byStreamClass.get(childClass);
        if (child == null && Augmentation.class.isAssignableFrom(childClass)) {
            return getAugmentationProtoByClass(childClass);
        }
        return child;
    }

    @Override
    NodeContextSupplier yangChildSupplier(final PathArgument arg) {
        // FIXME: super.yangChildPrototype(arg);
        final var child = byYang.get(arg);
        if (child == null) {
            final var augClass = augmentationPathToBindingClass.get(arg);
            if (augClass != null) {
                return augmentationBindingClassToProto.get(augClass);
            }
        }
        return child;
    }

    protected final ValueNodeCodecContext getLeafChild(final String name) {
        final ValueNodeCodecContext value = leafChild.get(name);
        if (value == null) {
            throw new IncorrectNestingException("Leaf %s is not valid for %s", name, getBindingClass());
        }
        return value;
    }

<<<<<<< HEAD
    private @Nullable DataContainerCodecPrototype<?> augmentationByClass(final @NonNull Class<?> childClass) {
        final DataContainerCodecPrototype<?> childProto = augmentationByStream.get(childClass);
        return childProto != null ? childProto : mismatchedAugmentationByClass(childClass);
=======
    @Override
    public Set<PathArgument> getChildPathArguments() {
        return childPathArgs;
    }

    @Override
    public Set<Class<?>> getChildBindingClasses() {
        return childBindingClasses;
    }

    private DataContainerCodecPrototype<?> loadChildPrototype(final Class<? extends DataContainer> childClass) {
        final var type = getType();
        final var child = childNonNull(type.bindingChild(JavaTypeName.create(childClass)), childClass,
            "Node %s does not have child named %s", type, childClass);

        return DataContainerCodecPrototype.from(createBindingArg(childClass, child.statement()),
            (CompositeRuntimeType) child, factory());
    }

    // FIXME: MDSAL-697: move this method into BindingRuntimeContext
    //                   This method is only called from loadChildPrototype() and exists only to be overridden by
    //                   CaseNodeCodecContext. Since we are providing childClass and our schema to BindingRuntimeContext
    //                   and receiving childSchema from it via findChildSchemaDefinition, we should be able to receive
    //                   the equivalent of Map.Entry<Item, DataSchemaNode>, along with the override we create here. One
    //                   more input we may need to provide is our bindingClass().
    @SuppressWarnings("unchecked")
    Item<?> createBindingArg(final Class<?> childClass, final EffectiveStatement<?, ?> childSchema) {
        return Item.of((Class<? extends DataObject>) childClass);
    }

    private @Nullable DataContainerCodecPrototype<?> getAugmentationProtoByClass(final @NonNull Class<?> augmClass) {
        final var childProto = augmentationBindingClassToProto.get(augmClass);
        return childProto != null ? childProto : mismatchedAugmentationByClass(augmClass);
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
    }

    private @Nullable DataContainerCodecPrototype<?> mismatchedAugmentationByClass(final @NonNull Class<?> childClass) {
        /*
         * It is potentially mismatched valid augmentation - we look up equivalent augmentation using reflection
         * and walk all stream child and compare augmentations classes if they are equivalent. When we find a match
         * we'll cache it so we do not need to perform reflection operations again.
         */
        final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> local =
                (ImmutableMap<Class<?>, DataContainerCodecPrototype<?>>) MISMATCHED_AUGMENTED.getAcquire(this);
        final DataContainerCodecPrototype<?> mismatched = local.get(childClass);
        return mismatched != null ? mismatched : loadMismatchedAugmentation(local, childClass);
    }

    private @Nullable DataContainerCodecPrototype<?> loadMismatchedAugmentation(
            final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> oldMismatched,
            final @NonNull Class<?> childClass) {
        @SuppressWarnings("rawtypes")
        final Class<?> augTarget = BindingReflections.findAugmentationTarget((Class) childClass);
        // Do not bother with proposals which are not augmentations of our class, or do not match what the runtime
        // context would load.
        if (getBindingClass().equals(augTarget) && belongsToRuntimeContext(childClass)) {
            for (final DataContainerCodecPrototype<?> realChild : augmentationBindingClassToProto.values()) {
                if (Augmentation.class.isAssignableFrom(realChild.getBindingClass())
                        && isSubstitutionFor(childClass, realChild.getBindingClass())) {
                    return cacheMismatched(oldMismatched, childClass, realChild);
                }
            }
        }
        LOG.trace("Failed to resolve {} as a valid augmentation in {}", childClass, this);
        return null;
    }

    private @NonNull DataContainerCodecPrototype<?> cacheMismatched(
            final @NonNull ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> oldMismatched,
            final @NonNull Class<?> childClass, final @NonNull DataContainerCodecPrototype<?> prototype) {

        ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> expected = oldMismatched;
        while (true) {
            final Map<Class<?>, DataContainerCodecPrototype<?>> newMismatched =
                    ImmutableMap.<Class<?>, DataContainerCodecPrototype<?>>builderWithExpectedSize(expected.size() + 1)
                        .putAll(expected)
                        .put(childClass, prototype)
                        .build();

            final var witness = (ImmutableMap<Class<?>, DataContainerCodecPrototype<?>>)
                MISMATCHED_AUGMENTED.compareAndExchangeRelease(this, expected, newMismatched);
            if (witness == expected) {
                LOG.trace("Cached mismatched augmentation {} -> {} in {}", childClass, prototype, this);
                return prototype;
            }

            expected = witness;
            final DataContainerCodecPrototype<?> existing = expected.get(childClass);
            if (existing != null) {
                LOG.trace("Using raced mismatched augmentation {} -> {} in {}", childClass, existing, this);
                return existing;
            }
        }
    }

    private boolean belongsToRuntimeContext(final Class<?> cls) {
        final BindingRuntimeContext ctx = factory().getRuntimeContext();
        final Class<?> loaded;
        try {
            loaded = ctx.loadClass(Type.of(cls));
        } catch (ClassNotFoundException e) {
            LOG.debug("Proposed {} cannot be loaded in {}", cls, ctx, e);
            return false;
        }
        return cls.equals(loaded);
    }

    private @Nullable DataContainerCodecPrototype<?> loadAugmentPrototype(final AugmentRuntimeType augment) {
        // FIXME: in face of deviations this code should be looking at declared view, i.e. all possibilities at augment
        //        declaration site
        final var childPaths = augment.statement()
            .streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class)
            .map(stmt -> new NodeIdentifier((QName) stmt.argument()))
            .collect(ImmutableSet.toImmutableSet());

        final var it = childPaths.iterator();
        if (!it.hasNext()) {
            return null;
        }
        final var namespace = it.next().getNodeType().getModule();

        final var factory = factory();
        final GeneratedType javaType = augment.javaType();
        final Class<? extends Augmentation<?>> augClass;
        try {
            augClass = factory.getRuntimeContext().loadClass(javaType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(
                "RuntimeContext references type " + javaType + " but failed to load its class", e);
        }
        return DataContainerCodecPrototype.from(augClass, namespace, childPaths, augment, factory);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    protected final @NonNull D createBindingProxy(final DistinctNodeContainer<?, ?> node) {
        try {
            return (D) proxyConstructor.invokeExact(this, node);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
            final DistinctNodeContainer<PathArgument, NormalizedNode> data) {

        /**
         * Due to augmentation fields are at same level as direct children
         * the data of each augmentation needs to be aggregated into own container node,
         * then only deserialized using associated prototype.
         */

        final Map<Class<?>, DataContainerNodeBuilder> builders = new HashMap<>();
        for (final NormalizedNode childValue : data.body()) {
            final Class<?> bindingClass = augmentationPathToBindingClass.get(childValue.getIdentifier());
            if (bindingClass != null) {
                builders.computeIfAbsent(bindingClass,
                                key -> Builders.containerBuilder()
                                        .withNodeIdentifier(new NodeIdentifier(data.getIdentifier().getNodeType())))
                        .addChild(childValue);
            }
        }
        @SuppressWarnings("rawtypes") final Map map = new HashMap<>();
        for (final var entry : builders.entrySet()) {
            final Class<?> bindingClass = entry.getKey();
            final DataContainerCodecPrototype<?> codecProto = augmentationBindingClassToProto.get(bindingClass);
            if (codecProto != null) {
                map.put(bindingClass, codecProto.get().deserializeObject(entry.getValue().build()));
            }
        }
        return map;
    }

    final @NonNull Class<? extends CodecDataObject<?>> generatedClass() {
        return generatedClass;
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final PathArgument arg) {
        checkArgument(getDomPathArgument().equals(arg));
        return bindingArg();
    }

    @Override
    public PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        checkArgument(bindingArg().equals(arg));
        return getDomPathArgument();
    }

<<<<<<< HEAD
=======
    @Override
    public NormalizedNode serialize(final D data) {
        return serializeImpl(data);
    }

    @Override
    public final BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier) {
        return createCachingCodec(this, cacheSpecifier);
    }

    /**
     * Scans supplied class and returns an iterable of all data children classes.
     *
     * @param type YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    // FIXME: MDSAL-780: replace use of this method
    private static Map<Class<? extends DataContainer>, Method> getChildrenClassToMethod(final Class<?> type) {
        return getChildClassToMethod(type, Naming.GETTER_PREFIX);
    }

    // FIXME: MDSAL-780: replace use of this method
    private static Map<Class<? extends DataContainer>, Method> getChildrenClassToNonnullMethod(final Class<?> type) {
        return getChildClassToMethod(type, Naming.NONNULL_PREFIX);
    }

    // FIXME: MDSAL-780: replace use of this method
    private static Map<Class<? extends DataContainer>, Method> getChildClassToMethod(final Class<?> type,
            final String prefix) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(DataContainer.class.isAssignableFrom(type), "Supplied type %s must be derived from DataContainer",
            type);
        final var ret = new HashMap<Class<? extends DataContainer>, Method>();
        for (Method method : type.getMethods()) {
            getYangModeledReturnType(method, prefix).ifPresent(entity -> ret.put(entity, method));
        }
        return ret;
    }
>>>>>>> f406e364a3 (Adopt augmentation node and identifier removal at YANGTOOLS)
}
