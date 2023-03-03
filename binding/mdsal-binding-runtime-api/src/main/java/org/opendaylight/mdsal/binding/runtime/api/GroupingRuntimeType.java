/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code grouping} statement.
 */
public interface GroupingRuntimeType extends CompositeRuntimeType {
    @Override
    GroupingEffectiveStatement statement();

    /**
     * Return vectors towards concrete instantiations of this type -- i.e. the manifestation in the effective data tree.
     * Each item in this list represents either:
     * <ul>
     *   <li>a concrete instantiation, or<li>
     *   <li>another {@link GroupingRuntimeType}</li>
     * </ul>
     *
     * @return A list of vectors through which this grouping is instantiated.
     */
    @NonNull List<CompositeRuntimeType> instantiationVectors();
}
