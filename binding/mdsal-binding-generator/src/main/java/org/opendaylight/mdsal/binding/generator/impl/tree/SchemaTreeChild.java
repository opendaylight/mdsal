/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import java.util.Optional;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * An object reflecting a YANG {@code schema node}.
 *
 * @param <S> Concrete {@link SchemaTreeEffectiveStatement} type
 * @param <R> Concrete {@link RuntimeType} type
 */
public interface SchemaTreeChild<S extends SchemaTreeEffectiveStatement<?>, R extends RuntimeType>
        extends Identifiable<QName>, StatementRepresentation<S> {
    @Override
    default QName getIdentifier() {
        return statement().argument();
    }

    Optional<R> recursiveRuntimeType();
}
