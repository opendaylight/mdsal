/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.LeafListRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

@NonNullByDefault
public final class DefaultLeafListRuntimeType extends AbstractRuntimeType<LeafListEffectiveStatement, Type>
        implements LeafListRuntimeType {
    private final LeafListEffectiveStatement statement;

    public DefaultLeafListRuntimeType(final Type bindingType, final LeafListEffectiveStatement statement) {
        super(bindingType);
        this.statement = requireNonNull(statement);
    }

    @Override
    public LeafListEffectiveStatement statement() {
        return statement;
    }
}
