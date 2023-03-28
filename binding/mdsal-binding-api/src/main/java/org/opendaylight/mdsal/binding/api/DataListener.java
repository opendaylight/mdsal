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

/**
 * Interface implemented by classes interested in receiving the last data change.
 */
@Beta
@FunctionalInterface
public interface DataListener<T extends DataObject> {

    /**
     * Invoked when there was data change for the supplied path, which was used to register listener.
     * @param data last state.
     */
    void dataChangedTo(@Nullable T data);
}
