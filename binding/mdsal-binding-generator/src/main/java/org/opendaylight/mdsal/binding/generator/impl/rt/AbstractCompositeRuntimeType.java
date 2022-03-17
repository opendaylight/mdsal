/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    abstract static class SchemaTreeComparator implements Comparator<RuntimeType> {
        static @NonNull QName extractQName(final RuntimeType type) {
            final var stmt = type.statement();
            verify(type instanceof SchemaTreeEffectiveStatement, "Unexpected statement %s in %s", stmt, type);
            return ((SchemaTreeEffectiveStatement<?>) stmt).argument();
        }
    }

    private static final class SchemaTreeSort extends SchemaTreeComparator {
        static final SchemaTreeSort INSTANCE = new SchemaTreeSort();

        @Override
        public int compare(final RuntimeType o1, final RuntimeType o2) {
            final int cmp = extractQName(o1).compareTo(extractQName(o2));
            verify(cmp != 0, "Type %s conflicts with %s on schema tree", o1, o2);
            return cmp;
        }
    }

    private static final class SchemaTreeLookup extends SchemaTreeComparator {
        private final @NonNull QName qname;

        SchemaTreeLookup(final QName qname) {
            this.qname = requireNonNull(qname);
        }

        @Override
        public int compare(final RuntimeType o1, final RuntimeType o2) {
            // We make assumptions about how Arrays.binarySearch() is implemented: o2 is expected to be the provided
            // key -- which is null. This helps CHA by not introducing a fake RuntimeType class and the correspoding
            // instanceof checks.
            verify(o2 == null, "Unexpected key %s", o2);
            return extractQName(o1).compareTo(qname);
        }
    }

    private static final RuntimeType[] EMPTY = new RuntimeType[0];

    private final ImmutableMap<JavaTypeName, GeneratedRuntimeType> byClass;
    private final RuntimeType[] bySchemaTree;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children) {
        super(bindingType, statement);

        byClass = children.stream()
            .filter(GeneratedRuntimeType.class::isInstance)
            .map(GeneratedRuntimeType.class::cast)
            .collect(ImmutableMap.toImmutableMap(GeneratedRuntimeType::getIdentifier, Functions.identity()));

        final var tmp = children.stream()
            .filter(child -> child.statement() instanceof SchemaTreeEffectiveStatement)
            .toArray(RuntimeType[]::new);
        bySchemaTree = tmp.length == 0 ? EMPTY : tmp;
        Arrays.sort(bySchemaTree, SchemaTreeSort.INSTANCE);
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        final int offset = Arrays.binarySearch(bySchemaTree, null, new SchemaTreeLookup(qname));
        return offset < 0 ? null : bySchemaTree[offset];
    }

    @Override
    public final GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        return byClass.get(requireNonNull(typeName));
    }

    // Makes an assertion of all types being of specified type
    @SuppressWarnings("unchecked")
    final <T extends RuntimeType> @NonNull List<T> schemaTree(final Class<T> expectedType) {
        for (var item : bySchemaTree) {
            verify(expectedType.isInstance(item), "Unexpected schema tree child %s", item);
        }
        return (List<T>) Collections.unmodifiableList(Arrays.asList(bySchemaTree));
    }
}
