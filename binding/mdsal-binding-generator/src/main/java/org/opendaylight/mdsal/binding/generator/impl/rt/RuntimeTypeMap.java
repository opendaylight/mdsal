/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;

final class RuntimeTypeMap extends ArrayMap<JavaTypeName, RuntimeType> {
    private static final RuntimeType[] EMPTY = new RuntimeType[0];

    static final RuntimeTypeMap INSTANCE = new RuntimeTypeMap();

    private RuntimeTypeMap() {
        super(RuntimeType.class, RuntimeType[].class);
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
    int compareValue(final RuntimeType obj, final JavaTypeName key) {
        return key.compareTo(obj.javaType().getIdentifier());
    }

    @Override
    int compareValues(final RuntimeType o1, final RuntimeType o2) {
        return compareValue(o1, o2.javaType().getIdentifier());
    }
}