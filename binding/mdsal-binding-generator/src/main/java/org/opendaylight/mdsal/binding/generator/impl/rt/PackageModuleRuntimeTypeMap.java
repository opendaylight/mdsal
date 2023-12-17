/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;

final class PackageModuleRuntimeTypeMap extends ArrayMap<String, ModuleRuntimeType> {
    private static final ModuleRuntimeType[] EMPTY = new ModuleRuntimeType[0];

    static final PackageModuleRuntimeTypeMap INSTANCE = new PackageModuleRuntimeTypeMap();

    private PackageModuleRuntimeTypeMap() {
        super(ModuleRuntimeType.class, ModuleRuntimeType[].class);
    }

    @Override
    ModuleRuntimeType[] emptyArray() {
        return EMPTY;
    }

    @Override
    ModuleRuntimeType[] newArray(final int length) {
        return new ModuleRuntimeType[length];
    }

    @Override
    int compareValue(final ModuleRuntimeType obj, final String key) {
        return key.compareTo(obj.getIdentifier().packageName());
    }

    @Override
    int compareValues(final ModuleRuntimeType o1, final ModuleRuntimeType o2) {
        return compareValue(o1, o2.getIdentifier().packageName());
    }
}