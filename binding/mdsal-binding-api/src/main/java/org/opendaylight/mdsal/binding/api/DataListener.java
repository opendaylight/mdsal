/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;

@Beta
@FunctionalInterface
public interface DataListener<T extends DataObject> {
    void dataChangedTo(@Nullable T data);
}
