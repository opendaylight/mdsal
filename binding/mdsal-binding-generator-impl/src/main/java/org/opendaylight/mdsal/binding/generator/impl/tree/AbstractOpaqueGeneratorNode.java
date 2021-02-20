/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * Common base class for generator nodes corresponding to an {@code anyxml} or on {@code anydata} statement.
 */
abstract class AbstractOpaqueGeneratorNode<T extends DataTreeEffectiveStatement<?>> extends AbstractGeneratorNode<T> {
    AbstractOpaqueGeneratorNode(final T statement) {
        super(statement);
    }
}
