/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

@Beta
public interface ChoiceRuntimeType extends CompositeRuntimeType<ChoiceEffectiveStatement> {
    /**
     * Returns resolved {@link CaseRuntimeType} for specified binding class name.
     *
     * @param typeName Binding class name
     * @return {@link CaseRuntimeType}, or null if absent
     */
    @Nullable CaseRuntimeType bindingCaseChild(JavaTypeName typeName);
}
