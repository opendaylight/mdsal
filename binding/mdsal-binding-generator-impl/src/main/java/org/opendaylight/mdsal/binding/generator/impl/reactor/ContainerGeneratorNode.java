/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

/**
 * Generator node corresponding to a {@code container} statement.
 */
public final class ContainerGeneratorNode extends AbstractCompositeGeneratorNode<ContainerEffectiveStatement> {
    public ContainerGeneratorNode(final ContainerEffectiveStatement statement) {
        super(statement);
    }
}
