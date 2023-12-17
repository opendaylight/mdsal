/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

final class SchemaTreeRuntimeTypeMap extends ArrayMap<QName, RuntimeType> {
    private static final RuntimeType[] EMPTY = new RuntimeType[0];
    static final SchemaTreeRuntimeTypeMap INSTANCE = new SchemaTreeRuntimeTypeMap();

    private SchemaTreeRuntimeTypeMap() {
        super(RuntimeType.class, RuntimeType[].class);
    }

    @Override
    Object index(final Stream<? extends RuntimeType> values) {
        return super.index(values.filter(value -> value.statement() instanceof SchemaTreeEffectiveStatement));
    }

    @Override
    RuntimeType[] emptyArray() {
        return EMPTY;
    }

    @Override
    RuntimeType[] newArray(final int length) {
        return new RuntimeType[length];
    }

    @Override
    int compareValue(final RuntimeType obj, final QName key) {
        return key.compareTo(qnameStatementArgument(obj));
    }

    @Override
    int compareValues(final RuntimeType o1, final RuntimeType o2) {
        return compareValue(o1, qnameStatementArgument(o2));
    }

    private static @NonNull QName qnameStatementArgument(final @NonNull RuntimeType value) {
        return ((SchemaTreeEffectiveStatement<?>) value.statement()).argument();
    }
}