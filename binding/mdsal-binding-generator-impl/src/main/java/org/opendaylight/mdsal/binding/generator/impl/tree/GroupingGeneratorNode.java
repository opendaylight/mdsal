/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

/**
 * Generator node corresponding to a {@code grouping} statement.
 */
public final class GroupingGeneratorNode extends AbstractCompositeGeneratorNode<GroupingEffectiveStatement> {
    GroupingGeneratorNode(final GroupingEffectiveStatement statement) {
        super(statement);
    }

    @Override
    YangStatementNamespace namespace() {
        return YangStatementNamespace.GROUPING;
    }
}
