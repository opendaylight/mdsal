/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * A {@link RuntimeType} associated with an {@code action} statement.
 */
@Beta
public interface ActionRuntimeType extends CompositeRuntimeType {
    @Override
    ActionEffectiveStatement statement();

    /**
     * Return the run-time type for this action's input.
     *
     * @return Input run-time type
     */
    @NonNull InputRuntimeType input();

    /**
     * Return the run-time type for this action's output.
     *
     * @return Output run-time type
     */
    @NonNull OutputRuntimeType output();
}
