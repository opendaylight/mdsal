/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Trait for objects which can be formulated in terms of a {@link DOMQuery}.
 */
@Beta
@NonNullByDefault
public interface DOMQueryLike extends Immutable {
    /**
     * Return a {@link DOMQuery} view of this object.
     *
     * @return A DOMQuery
     */
    DOMQuery asDOMQuery();
}
