/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

abstract class AbstractCompositeRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractRuntimeType<S, GeneratedType> implements CompositeRuntimeType {
    private final ImmutableMap<AugmentationIdentifier, AugmentRuntimeType> augments;
    private final ImmutableMap<JavaTypeName, RuntimeType> byClass;
    private final ImmutableMap<QName, RuntimeType> bySchemaTree;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S statement,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children,
            final Map<AugmentationIdentifier, AugmentRuntimeType> augments) {
        super(bindingType, statement);
        this.augments = ImmutableMap.copyOf(augments);

        byClass = Maps.uniqueIndex(children.keySet(), RuntimeType::getIdentifier);

        // Note: this may be over-sized, but we typically deal with schema tree statements, hence it is kind of accurate
        final var builder = ImmutableMap.<QName, RuntimeType>builderWithExpectedSize(children.size());
        for (var entry : children.entrySet()) {
            final var stmt = entry.getValue();
            if (stmt instanceof SchemaTreeEffectiveStatement) {
                builder.put(((SchemaTreeEffectiveStatement<?>)stmt).argument(), entry.getKey());
            }
        }
        bySchemaTree = builder.build();
    }

    @Override
    public final ImmutableMap<AugmentationIdentifier, AugmentRuntimeType> augments() {
        return augments;
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        return bySchemaTree.get(requireNonNull(qname));
    }

    @Override
    public final RuntimeType bindingChild(final JavaTypeName typeName) {
        return byClass.get(requireNonNull(typeName));
    }

    @Override
    public final Entry<AugmentationIdentifier, AugmentRuntimeType> resolveAugmentation(final AugmentRuntimeType type) {

//        @Override
//        public final Entry<AugmentationIdentifier, AugmentRuntimeType> getResolvedAugmentationSchema(
//                final DataNodeContainer target, final Class<? extends Augmentation<?>> aug) {
//            final AugmentRuntimeType origSchema = getAugmentationDefinition(aug);
//            checkArgument(origSchema != null, "Augmentation %s is not known in current schema context", aug);
//            /*
//             * FIXME: Validate augmentation schema lookup
//             *
//             * Currently this algorithm, does not verify if instantiated child nodes
//             * are real one derived from augmentation schema. The problem with
//             * full validation is, if user used copy builders, he may use
//             * augmentation which was generated for different place.
//             *
//             * If this augmentations have same definition, we emit same identifier
//             * with data and it is up to underlying user to validate data.
//             *
//             */
//            final Set<QName> childNames = new HashSet<>();
//            final Set<DataSchemaNode> realChilds = new HashSet<>();
//            for (final DataSchemaNode child : origSchema.getChildNodes()) {
//                final DataSchemaNode dataChildQNname = target.dataChildByName(child.getQName());
//                final String childLocalName = child.getQName().getLocalName();
//                if (dataChildQNname == null) {
//                    for (DataSchemaNode dataSchemaNode : target.getChildNodes()) {
//                        if (childLocalName.equals(dataSchemaNode.getQName().getLocalName())) {
//                            realChilds.add(dataSchemaNode);
//                            childNames.add(dataSchemaNode.getQName());
//                        }
//                    }
//                } else {
//                    realChilds.add(dataChildQNname);
//                    childNames.add(child.getQName());
//                }
//            }
//
//            final AugmentationIdentifier identifier = AugmentationIdentifier.create(childNames);
//            final AugmentationSchemaNode proxy = new EffectiveAugmentationSchema(origSchema, realChilds);
//            return new SimpleEntry<>(identifier, proxy);
//        }
        return null;
    }
}
