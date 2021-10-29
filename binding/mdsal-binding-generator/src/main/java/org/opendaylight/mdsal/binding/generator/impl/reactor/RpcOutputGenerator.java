/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * Specialization for legacy RPC services.
 */
final class RpcOutputGenerator extends OutputGenerator {
    RpcOutputGenerator(final OutputEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    CollisionDomain parentDomain() {
        return getParent().parentDomain();
    }

    @Override
    AbstractCompositeGenerator<?, ?> getPackageParent() {
        return getParent().getParent();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, getParent().ensureMember());
    }
}
