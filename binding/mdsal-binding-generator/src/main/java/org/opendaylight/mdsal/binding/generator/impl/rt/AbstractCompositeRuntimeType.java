/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class AbstractCompositeRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractRuntimeType<S, GeneratedType> implements CompositeRuntimeType {
    private final @NonNull Object byClass;
    private final @NonNull Object bySchemaTree;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children) {
        super(bindingType, statement);
        byClass = GeneratedRuntimeTypeMap.INSTANCE.indexFiltered(children.stream());
        bySchemaTree = SchemaTreeRuntimeTypeMap.INSTANCE.index(children.stream());
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        return SchemaTreeRuntimeTypeMap.INSTANCE.lookup(bySchemaTree, qname);
    }

    @Override
    public final GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        return GeneratedRuntimeTypeMap.INSTANCE.lookup(byClass, typeName);
    }

    final <T extends RuntimeType> @NonNull List<T> schemaTree(final Class<T> expectedType) {
        return SchemaTreeRuntimeTypeMap.INSTANCE.toList(expectedType, bySchemaTree);
    }
}