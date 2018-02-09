/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecPrototype;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context for prototype of choice node codec.
 *
 * @param <D>
 *            - type of tree node
 */
@Beta
public class ChoiceNodeCodecContext<D extends TreeNode> extends DataContainerCodecContext<D, ChoiceSchemaNode> {

    private static final Logger LOG = LoggerFactory.getLogger(ChoiceNodeCodecContext.class);

    private final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChild;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClass;

    /**
     * Prepare context for choice node from prototype and all case children of choice class.
     *
     * @param prototype
     *            - codec prototype of choice node
     */
    public ChoiceNodeCodecContext(final DataContainerCodecPrototype<ChoiceSchemaNode> prototype) {
        super(prototype);
        final Map<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChildBuilder =
                new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byClassBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClassBuilder = new HashMap<>();
        final Set<Class<?>> potentialSubstitutions = new HashSet<>();

        //TODO: Collect all choice/cases' descendant data children including augmented data nodes.
        // Walks all cases for supplied choice in current runtime context
        for (final Class<?> caze : factory().getRuntimeContext().getCases(getBindingClass())) {
            // We try to load case using exact match thus name
            // and original schema must equals
            final DataContainerCodecPrototype<CaseSchemaNode> cazeDef = loadCase(caze);
            // If we have case definition, this case is instantiated
            // at current location and thus is valid in context of parent choice
            if (cazeDef != null) {
                byClassBuilder.put(cazeDef.getBindingClass(), cazeDef);
                // Updates collection of case children
                @SuppressWarnings("unchecked")
                final Class<? extends Instantiable<?>> cazeCls = (Class<? extends Instantiable<?>>) caze;
                for (final Class<? extends TreeNode> cazeChild : BindingReflections.getChildrenClasses(cazeCls)) {
                    byCaseChildClassBuilder.put(cazeChild, cazeDef);
                }
                // Updates collection of YANG instance identifier to case
                for (final DataSchemaNode cazeChild : cazeDef.getSchema().getChildNodes()) {
                    if (cazeChild.isAugmenting()) {
                        final AugmentationSchemaNode augment =
                                SchemaUtils.findCorrespondingAugment(cazeDef.getSchema(), cazeChild);
                        if (augment != null) {
                            byYangCaseChildBuilder.put(SchemaUtils.getNodeIdentifierForAugmentation(augment), cazeDef);
                            continue;
                        }
                    }
                    byYangCaseChildBuilder.put(NodeIdentifier.create(cazeChild.getQName()), cazeDef);
                }
            } else {
                /*
                 * If case definition is not available, we store it for later check if it could be used as
                 * substitution of existing one.
                 */
                potentialSubstitutions.add(caze);
            }
        }

        final Map<Class<?>, DataContainerCodecPrototype<?>> bySubstitutionBuilder = new HashMap<>();
        /*
         * Walks all cases which are not directly instantiated and tries to match them to instantiated cases -
         * represent same data as instantiated case, only case name or schema path is different. This is
         * required due property of binding specification, that if choice is in grouping schema path location
         * is lost, and users may use incorrect case class using copy builders.
         */
        for (final Class<?> substitution : potentialSubstitutions) {
            for (final Entry<Class<?>, DataContainerCodecPrototype<?>> real : byClassBuilder.entrySet()) {
                if (BindingReflections.isSubstitutionFor(substitution, real.getKey())) {
                    bySubstitutionBuilder.put(substitution, real.getValue());
                    break;
                }
            }
        }
        byClassBuilder.putAll(bySubstitutionBuilder);
        byYangCaseChild = ImmutableMap.copyOf(byYangCaseChildBuilder);
        byClass = ImmutableMap.copyOf(byClassBuilder);
        byCaseChildClass = ImmutableMap.copyOf(byCaseChildClassBuilder);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <C extends TreeNode> DataContainerCodecContext<C, ?> streamChild(@Nonnull final Class<C> childClass) {
        final DataContainerCodecPrototype<?> child = byClass.get(childClass);
        return (DataContainerCodecContext<C,
                ?>) childNonNull(child, childClass, "Supplied class %s is not valid case", childClass).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends TreeNode> Optional<DataContainerCodecContext<C, ?>>
            possibleStreamChild(@Nonnull final Class<C> childClass) {
        final DataContainerCodecPrototype<?> child = byClass.get(childClass);
        if (child != null) {
            return Optional.of((DataContainerCodecContext<C, ?>) child.get());
        }
        return Optional.absent();
    }


    /**
     * Gets the map of case class and prototype for {@link
     * org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext}
     * to catch choice/cases' data child by class.
     *
     * @return the map of case class and prototype
     */
    public Map<Class<?>, DataContainerCodecPrototype<?>> getClassCaseChildren() {
        return byCaseChildClass;
    }


    /**
     * Gets the map of case path argument and prototype for {@link
     * org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext}
     * to catch choice/cases' data child by class.
     *
     * @return the the map of case path and prototype
     */
    public Map<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> getYangCaseChildren() {
        return byYangCaseChild;
    }

    public DataContainerCodecContext<?, ?> getCaseByChildClass(final @Nonnull Class<? extends TreeNode> type) {
        final DataContainerCodecPrototype<?> protoCtx =
            childNonNull(byCaseChildClass.get(type), type, "Class %s is not child of any cases for %s", type,
                bindingArg());
        return protoCtx.get();
    }

    private DataContainerCodecPrototype<CaseSchemaNode> loadCase(final Class<?> childClass) {
        final Optional<CaseSchemaNode> childSchema =
                factory().getRuntimeContext().getCaseSchemaDefinition(getSchema(), childClass);
        if (childSchema.isPresent()) {
            return DataContainerCodecPrototype.from(childClass, childSchema.get(), factory());
        }

        LOG.debug("Supplied class %s is not valid case in schema %s", childClass, getSchema());
        return null;
    }

    @Nonnull
    @Override
    public NodeCodecContext<?> yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg) {
        final DataContainerCodecPrototype<?> cazeProto;
        if (arg instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates) {
            cazeProto = byYangCaseChild.get(new NodeIdentifier(arg.getNodeType()));
        } else {
            cazeProto = byYangCaseChild.get(arg);
        }

        return childNonNull(cazeProto, arg, "Argument %s is not valid child of %s", arg, getSchema()).get()
                .yangPathArgumentChild(arg);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> data) {
        Preconditions.checkArgument(data instanceof ChoiceNode);
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> casted =
                (NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>>) data;
        final NormalizedNode<?, ?> first = Iterables.getFirst(casted.getValue(), null);

        if (first == null) {
            return null;
        }
        final DataContainerCodecPrototype<?> caze = byYangCaseChild.get(first.getIdentifier());
        return (D) caze.get().deserialize(data);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Nonnull
    @Override
    public TreeArgument<?> deserializePathArgument(@Nonnull final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(getDomPathArgument().equals(arg));
        return null;
    }

    @Nonnull
    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(@Nonnull final TreeArgument<?> arg) {
        return getDomPathArgument();
    }
}
