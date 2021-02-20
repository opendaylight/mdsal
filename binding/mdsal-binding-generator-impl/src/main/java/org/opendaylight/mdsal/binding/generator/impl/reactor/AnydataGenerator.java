/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;

/**
 * Generator corresponding to an {@code anydata} statement.
 */
public final class AnydataGenerator extends AbstractOpaqueGenerator<AnydataEffectiveStatement> {
    AnydataGenerator(final AnydataEffectiveStatement statement) {
        super(statement);
    }
}
