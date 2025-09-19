/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.EntryObject;

/**
 * A {@link DescendantQueryBuilder} operating on a single {@link DataObject} as identified
 * by a {@link DataObjectIdentifier}.
 *
 * @param <T> Query result type
 */
public interface InstanceQueryBuilder<T extends DataObject> extends DescendantQueryBuilder<T> {
    /**
     * Return a {@link DescendantQueryBuilder} traversing all {@link EntryObject}s.
     *
     * @param <E> {@link EntryObject} type
     * @param type the type class
     * @return a {@link DescendantQueryBuilder}
     */
    <E extends EntryObject<?, ?>> @NonNull DescendantQueryBuilder<E> queryAllEntries(@NonNull Class<E> type);
}
