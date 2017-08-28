/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.yangtools.concepts.Codec;
import org.w3c.dom.Document;

/**
 * Codec for serialize/deserialize anyxml.
 */
@Beta
public final class AnyxmlCodec implements Codec<DOMSource, Document> {

    private final BindingRuntimeContext context;

    /**
     * Prepared binding runtime context for anyxml codec.
     *
     * @param context
     *            - binding runtime context
     */
    public AnyxmlCodec(final BindingRuntimeContext context) {
        this.context = Preconditions.checkNotNull(context);
    }

    @Override
    public Document deserialize(final DOMSource input) {
        Preconditions.checkArgument(input != null, "Input must not be null.");
        return (Document) input.getNode();
    }

    @Override
    public DOMSource serialize(final Document input) {
        Preconditions.checkArgument(input != null, "Input must not be null.");
        return new DOMSource(input);
    }
}
