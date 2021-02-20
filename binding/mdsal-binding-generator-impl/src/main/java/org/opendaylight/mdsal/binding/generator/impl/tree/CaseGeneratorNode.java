/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;

/**
 * Generator node corresponding to a {@code case} statement.
 */
public final class CaseGeneratorNode extends AbstractCompositeGeneratorNode<CaseEffectiveStatement> {
    CaseGeneratorNode(final CaseEffectiveStatement statement) {
        super(statement);
    }
}
