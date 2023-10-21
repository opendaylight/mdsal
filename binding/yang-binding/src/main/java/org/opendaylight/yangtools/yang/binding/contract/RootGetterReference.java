/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 *
 */
public sealed interface RootGetterReference<P extends DataRoot, C> extends GetterReference<P, C> {

    @FunctionalInterface
    non-sealed interface BooleanReference<P extends DataRoot> extends RootGetterReference<P, Boolean> {

    }

    @FunctionalInterface
    non-sealed interface Decimal64Reference<P extends DataRoot> extends RootGetterReference<P, Decimal64> {

    }

    @FunctionalInterface
    non-sealed interface EmptyReference<P extends DataRoot> extends RootGetterReference<P, Empty> {

    }

    @FunctionalInterface
    non-sealed interface StringReference<P extends DataRoot> extends RootGetterReference<P, String> {

    }

    @FunctionalInterface
    non-sealed interface Int8Reference<P extends DataRoot> extends RootGetterReference<P, Byte> {

    }

    @FunctionalInterface
    non-sealed interface Int16Reference<P extends DataRoot> extends RootGetterReference<P, Short> {

    }

    @FunctionalInterface
    non-sealed interface Int32Reference<P extends DataRoot> extends RootGetterReference<P, Integer> {

    }

    @FunctionalInterface
    non-sealed interface Int64Reference<P extends DataRoot> extends RootGetterReference<P, Long> {

    }

    @FunctionalInterface
    non-sealed interface Uint8Reference<P extends DataRoot> extends RootGetterReference<P, Uint8> {

    }

    @FunctionalInterface
    non-sealed interface Uint16Reference<P extends DataRoot> extends RootGetterReference<P, Uint16> {

    }

    @FunctionalInterface
    non-sealed interface Uint32Reference<P extends DataRoot> extends RootGetterReference<P, Uint32> {

    }

    @FunctionalInterface
    non-sealed interface Uint64Reference<P extends DataRoot> extends RootGetterReference<P, Uint64> {

    }

    @FunctionalInterface
    non-sealed interface IdentityReference<P extends DataRoot, T extends BaseIdentity>
        extends RootGetterReference<P, T> {

    }

    @FunctionalInterface
    non-sealed interface TypeObjectReference<P extends DataRoot, T extends TypeObject>
        extends RootGetterReference<P, T> {

    }
}
