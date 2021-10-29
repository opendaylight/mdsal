/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;

/**
 * A {@link RuntimeType} which is also a {@link RuntimeTypeContainer}.
 */
@Beta
public interface CompositeRuntimeType extends GeneratedRuntimeType, RuntimeTypeContainer {

    @NonNull ImmutableMap<AugmentationIdentifier, AugmentRuntimeType> augments();

    @NonNull Entry<AugmentationIdentifier, AugmentRuntimeType> resolveAugmentation(AugmentRuntimeType type);
}
