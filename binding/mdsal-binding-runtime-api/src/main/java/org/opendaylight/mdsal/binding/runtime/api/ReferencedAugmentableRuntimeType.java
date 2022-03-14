/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Global view of an {@link AugmentableRuntimeType}, with all concrete specializations available through
 *  {@link #referencingAugments()}.
 */
@Beta
public interface ReferencedAugmentableRuntimeType extends AugmentableRuntimeType {
    /**
     * Return the {@link AugmentRuntimeType}s extending any instantiation of this type.
     *
     * @return {@link AugmentRuntimeType}s extending any instantiation of this type.
     */
    @NonNull List<AugmentRuntimeType> referencingAugments();
}
