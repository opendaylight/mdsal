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
    private final ImmutableMap<JavaTypeName, RuntimeType> byClass;
    private final ImmutableMap<QName, RuntimeType> bySchemaTree;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S schema,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children) {
        super(bindingType, schema);
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
        // FIXME: implement this
        return ImmutableMap.of();
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        return bySchemaTree.get(requireNonNull(qname));
    }

    @Override
    public final RuntimeType bindingChild(final JavaTypeName typeName) {
        return byClass.get(requireNonNull(typeName));

// FIXME: this part should live in CompositeGenerator
//        @Override
//        public final DataSchemaNode findChildSchemaDefinition(final DataNodeContainer parentSchema,
//                final QNameModule parentNamespace, final Class<?> childClass) {
//            final DataSchemaNode origDef = getSchemaDefinition(childClass);
//            if (origDef == null) {
//                // Weird, the child does not have an associated definition
//                return null;
//            }
//
//            // Direct instantiation or use in same module in which grouping was defined.
//            final QName origName = origDef.getQName();
//            final DataSchemaNode sameName = parentSchema.dataChildByName(origName);
//            if (sameName != null) {
//                // Check if it is:
//                // - exactly same schema node, or
//                // - instantiated node was added via uses statement and is instantiation of same grouping
//                if (origDef.equals(sameName) || origDef.equals(getRootOriginalIfPossible(sameName))) {
//                    return sameName;
//                }
//
//                // Node has same name, but clearly is different
//                return null;
//            }
//
//            // We are looking for instantiation via uses in other module
//            final DataSchemaNode potential = parentSchema.dataChildByName(origName.bindTo(parentNamespace));
//            // We check if it is really instantiated from same definition as class was derived
//            if (potential != null && origDef.equals(getRootOriginalIfPossible(potential))) {
//                return potential;
//            }
//            return null;
//        }
    }

//    private static @Nullable SchemaNode getRootOriginalIfPossible(final SchemaNode data) {
//        SchemaNode previous = null;
//        SchemaNode next = originalNodeOf(data);
//        while (next != null) {
//            previous = next;
//            next = originalNodeOf(next);
//        }
//        return previous;
//    }
}
