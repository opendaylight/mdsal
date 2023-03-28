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
 * Interface implemented by classes interested in receiving data changes. This interface provides a much simplified API
 * surface, only reporting when a particular subtree changes.
 */
@Beta
@FunctionalInterface
public interface DataChangeListener<T extends DataObject> {
    /**
     * Invoked when there was a change to registered data path. This method is triggered on initial synchronization even
     * if the data path does not exist, in which case both previous and current values will be reported as {@code null}.
     * Also note that the two values may actually compare as equal -- users interested in suppressing such changes
     * are advised to perform the comparison themselves.
     *
     * @param previousValue Previous data value, {@code null} if non-existent
     * @param currentValue Current data value, {@code null} if non-existent.
     */
    void dataChanged(@Nullable T previousValue, @Nullable T currentValue);
}
