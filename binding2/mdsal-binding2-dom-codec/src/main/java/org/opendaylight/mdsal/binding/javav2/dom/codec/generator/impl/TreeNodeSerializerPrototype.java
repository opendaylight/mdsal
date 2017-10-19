/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerRegistry;

/**
 * Prototype of a TreeNodeSerializerImplementation. This is a template class, which the stream writer
 * generator uses to instantiate {@link TreeNodeSerializerImplementation} on a per-type basis. During that
 * time, the {@link #serialize(TreeNodeSerializerRegistry, TreeNode, BindingStreamEventWriter)} method will be
 * replaced by the real implementation.
 */
@Beta
public final class TreeNodeSerializerPrototype implements TreeNodeSerializerImplementation {

    private static final TreeNodeSerializerPrototype INSTANCE = new TreeNodeSerializerPrototype();

    private TreeNodeSerializerPrototype() {
        // Intentionally hidden, subclasses can replace it
    }

    /**
     * Return the shared serializer instance.
     *
     * @return Global singleton instance.
     */
    public static TreeNodeSerializerPrototype getInstance() {
        return INSTANCE;
    }

    @Override
    public void serialize(final TreeNodeSerializerRegistry reg, final TreeNode obj,
            final BindingStreamEventWriter stream) throws IOException {
        throw new UnsupportedOperationException("Prototype body, this code should never be invoked.");
    }

}
