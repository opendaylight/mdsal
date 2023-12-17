/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;

final class GeneratedRuntimeTypeMap extends ArrayMap<JavaTypeName, GeneratedRuntimeType> {
    private static final GeneratedRuntimeType[] EMPTY = new GeneratedRuntimeType[0];

    static final GeneratedRuntimeTypeMap INSTANCE = new GeneratedRuntimeTypeMap();

    private GeneratedRuntimeTypeMap() {
        super(GeneratedRuntimeType.class, GeneratedRuntimeType[].class);
    }

    @Override
    GeneratedRuntimeType[] emptyArray() {
        return EMPTY;
    }

    @Override
    GeneratedRuntimeType[] newArray(final int length) {
        return new GeneratedRuntimeType[length];
    }

    @Override
    int compareValue(final GeneratedRuntimeType obj, final JavaTypeName key) {
        return key.compareTo(obj.getIdentifier());
    }

    @Override
    int compareValues(final GeneratedRuntimeType o1, final GeneratedRuntimeType o2) {
        return compareValue(o1, o2.getIdentifier());
    }
}