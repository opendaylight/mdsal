/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Builder object which {@code build()} method produces a product or throws {@link QueryStructureException}.
 *
 * @param <P> Product of builder
 */
@Beta
public sealed interface StructuralBuilder<P> permits DescendantQueryBuilder, ValueMatch {
    /**
     * Returns instance of the product. Multiple calls to this method are not required to return same instance if
     * the state of the builder has changed.
     *
     * @return A newly-built instance
     * @throws QueryStructureException if the builder's state is not sufficiently initialized
     */
    @NonNull P build() throws QueryStructureException;
}
