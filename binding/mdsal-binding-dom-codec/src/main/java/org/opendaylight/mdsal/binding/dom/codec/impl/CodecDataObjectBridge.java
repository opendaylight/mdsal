/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.impl.CodecDataObjectGenerator.Fixed;
import org.opendaylight.mdsal.binding.dom.codec.impl.CodecDataObjectGenerator.Reusable;

/**
 * Bridge for initializing {@link CodecDataObject} instance constants during class loading time. This class is public
 * only due to implementation restrictions and can change at any time.
 */
@Beta
public final class CodecDataObjectBridge {
    private static final ThreadLocal<CodecDataObjectGenerator<?>> CURRENT_CUSTOMIZER = new ThreadLocal<>();

    private CodecDataObjectBridge() {

    }

    public static @NonNull NodeContextSupplier resolve(final @NonNull String methodName) {
        return current(Fixed.class).resolve(methodName);
    }

    public static @NonNull String resolveLocalName(final @NonNull String methodName) {
        return current(Reusable.class).resolveLocalName(methodName);
    }

    static @Nullable CodecDataObjectGenerator<?> setup(final @NonNull CodecDataObjectGenerator<?> next) {
        final CodecDataObjectGenerator<?> prev = CURRENT_CUSTOMIZER.get();
        CURRENT_CUSTOMIZER.set(verifyNotNull(next));
        return prev;
    }

    static void tearDown(final @Nullable CodecDataObjectGenerator<?> prev) {
        if (prev == null) {
            CURRENT_CUSTOMIZER.remove();
        } else {
            CURRENT_CUSTOMIZER.set(prev);
        }
    }

    private static <T extends CodecDataObjectGenerator<?>> @NonNull T current(final Class<T> requested) {
        final CodecDataObjectGenerator<?> ret = verifyNotNull(CURRENT_CUSTOMIZER.get(), "No customizer attached");
        return requested.cast(ret);
    }
}
