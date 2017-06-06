/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Contract for implementations of {@link TreeNodeSerializer}.
 * The contract is kept between implementation of {@link TreeNodeSerializerRegistry},
 * which maintains the lookup context required for recursive serialization.
 */
public interface TreeNodeSerializerImplementation {

    /**
     * Writes stream events for supplied tree node to provided stream.
     *
     * TreeNodeSerializerRegistry may be used to lookup serializers for other generated classes in order to
     * support writing their events.
     */
    void serialize(TreeNodeSerializerRegistry reg, TreeNode obj, BindingStreamEventWriter stream) throws IOException;
}
