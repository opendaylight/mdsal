/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;

/**
 * A {@link RuntimeType} associated with an {@code anyxml} statement.
 */
@Beta
public interface AnyxmlRuntimeType extends OpaqueRuntimeType {
    @Override
    AnyxmlEffectiveStatement statement();
}
