/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * Generator node corresponding to a {@code augment} statement.
 */
public final class AugmentGeneratorNode extends AbstractCompositeGeneratorNode<AugmentEffectiveStatement> {
    AugmentGeneratorNode(final AugmentEffectiveStatement statement) {
        super(statement);
    }
}
