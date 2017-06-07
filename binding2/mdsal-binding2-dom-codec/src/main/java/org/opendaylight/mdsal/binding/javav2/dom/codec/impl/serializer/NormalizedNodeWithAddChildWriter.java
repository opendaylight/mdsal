/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

/**
 * Event Stream Writer based on Normalized Node tree representation with
 * allowing to add children.
 *
 */
@Beta
final class NormalizedNodeWithAddChildWriter extends ImmutableNormalizedNodeStreamWriter {

    /**
     * Initialization writer based on {@link NormalizedNodeResult}.
     *
     * @param result
     *            - result holder for writer
     */
    NormalizedNodeWithAddChildWriter(final NormalizedNodeResult result) {
        super(result);
    }

    void addChild(final NormalizedNode<?, ?> child) {
        this.writeChild(child);
    }
}