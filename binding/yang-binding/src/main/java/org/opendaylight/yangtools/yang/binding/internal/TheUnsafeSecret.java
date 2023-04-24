/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.UnsafeSecret;

/**
 * Internal implementation of {@link UnsafeSecret}.
 */
public final class TheUnsafeSecret implements UnsafeSecret {
    public static final @NonNull UnsafeSecret INSTANCE = new TheUnsafeSecret();

    private TheUnsafeSecret() {
        // Hidden on purpose
    }
}
