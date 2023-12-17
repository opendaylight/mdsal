/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Functions;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

abstract class AbstractCompositeRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractRuntimeType<S, GeneratedType> implements CompositeRuntimeType {
    private static final ArrayIndex<QName, RuntimeType> SCHEMA_TREE_INDEX =
        new ArrayIndex<>(RuntimeType.class, RuntimeType[].class) {
            private static final RuntimeType[] EMPTY = new RuntimeType[0];

            @Override
            RuntimeType[] emptyArray() {
                return EMPTY;
            }

            @Override
            RuntimeType[] newArray(final int length) {
                return new RuntimeType[length];
            }

            int compareValue(final RuntimeType obj, final QName key) {
                return key.compareTo(extractQName(obj));
            }

            @Override
            int compareValues(final RuntimeType o1, final RuntimeType o2) {
                return compareValue(o1, extractQName(o2));
            }

            private static @NonNull QName extractQName(final RuntimeType type) {
                final var stmt = type.statement();
                if (stmt instanceof SchemaTreeEffectiveStatement<?> schemaTreeStmt) {
                    return schemaTreeStmt.argument();
                }
                throw new VerifyException("Unexpected statement " + stmt + " in " + type);
            }
        };

    private final ImmutableMap<JavaTypeName, GeneratedRuntimeType> byClass;
    private final @NonNull Object bySchemaTree;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children) {
        super(bindingType, statement);

        byClass = children.stream()
            .filter(GeneratedRuntimeType.class::isInstance)
            .map(GeneratedRuntimeType.class::cast)
            .collect(ImmutableMap.toImmutableMap(GeneratedRuntimeType::getIdentifier, Functions.identity()));

        bySchemaTree = SCHEMA_TREE_INDEX.index(children.stream()
            .filter(child -> child.statement() instanceof SchemaTreeEffectiveStatement));
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        return SCHEMA_TREE_INDEX.lookup(bySchemaTree, qname);
    }

    @Override
    public final GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        return byClass.get(requireNonNull(typeName));
    }

    final <T extends RuntimeType> @NonNull List<T> schemaTree(final Class<T> expectedType) {
        return SCHEMA_TREE_INDEX.toList(expectedType, bySchemaTree);
    }
}