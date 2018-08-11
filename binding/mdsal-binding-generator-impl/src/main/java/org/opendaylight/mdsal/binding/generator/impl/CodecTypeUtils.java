/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;

public final class CodecTypeUtils {
    private CodecTypeUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static IdentifiableItem<?, ?> newIdentifiableItem(final Class<?> type, final Object key) {
        return IdentifiableItem.of((Class)type, (Identifier)key);
    }
}
