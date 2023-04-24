/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.binding.internal.TheUnsafeSecret;

/**
 * Marker interface for unsafe access. An instance of this interface is provided through
 * {@link CodeHelpers#unsafeScalar(Object, Function, BiFunction)}.
 */
public sealed interface UnsafeSecret permits TheUnsafeSecret {

}