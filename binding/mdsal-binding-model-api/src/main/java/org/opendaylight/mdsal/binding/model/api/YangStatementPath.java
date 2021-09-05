/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * A path to (and including) an {@link EffectiveStatement} from which a particular {@link GeneratedType} has been
 * generated.
 */
@Beta
public interface YangStatementPath extends EffectiveStatementInference {
    /**
     * {@inheritDoc}
     *
     * <p>
     * The statement path always starts with a {@link ModuleEffectiveStatement} and may be followed by other statements,
     * ordered from ancestors to (potentially indirect) descendants.
     *
     * @apiNote
     *     This method may be arbitrarily expensive, as can be the act of iterating through the returned list. Users
     *     should try to use {@link #lastStatement()} or {@link #reverseStatementPathIterator()} whenever possible.
     */
    @Override
    List<? extends EffectiveStatement<?, ?>> statementPath();

    @NonNull Iterator<? extends EffectiveStatement<?, ?>> reverseStatementPathIterator();

    default @NonNull EffectiveStatement<?, ?> lastStatement() {
        return reverseStatementPathIterator().next();
    }
}
