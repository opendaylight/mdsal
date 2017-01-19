/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Codec;

/**
 * Base interface for Binding2 encoding/decoding mechanism implementation
 */
@Beta
public interface BindingCodec<P, I> extends BindingSerializer<P, I>, BindingDeserializer<I, P>, Codec<P, I> {

    /**
     * Produces an object based on input.
     *
     * @param input Input object
     * @return Product derived from input
     */
    @Override
    P serialize(I input);

    /**
     * Produces an object based on input.
     *
     * @param input Input object
     * @return Product derived from input
     */
    @Override
    I deserialize(P input);

}