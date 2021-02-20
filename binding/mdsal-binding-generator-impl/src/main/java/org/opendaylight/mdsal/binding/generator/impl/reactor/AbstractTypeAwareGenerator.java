/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * Common base class for {@link LeafGenerator} and {@link LeafListGenerator}.
 */
abstract class AbstractTypeAwareGenerator<T extends DataTreeEffectiveStatement<?>>
        extends AbstractLeafrefAwareGenerator<T> {
    AbstractTypeAwareGenerator(final T statement) {
        super(statement);
    }

    final void linkType(final Iterable<? extends Iterable<? extends Generator<?>>> generatorsTowardsRoot) {
        // FIXME: implement this
    }
}
