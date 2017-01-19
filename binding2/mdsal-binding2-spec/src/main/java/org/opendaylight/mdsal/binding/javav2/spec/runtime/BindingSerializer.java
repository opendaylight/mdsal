/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import com.google.common.annotations.Beta;

/**
 * The concept of a serializer in Binding part, which produces an object from some input.
 *
 * @param <P> Product type
 * @param <I> Input type
 */
@Beta
public interface BindingSerializer<P, I> {

    /**
     * Produces an object based on input.
     *
     * @param input Input object
     * @return Product derived from input
     */
    P serialize(I input);
}