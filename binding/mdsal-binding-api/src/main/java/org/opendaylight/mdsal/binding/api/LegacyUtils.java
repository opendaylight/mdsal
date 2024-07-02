/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class LegacyUtils {
    private LegacyUtils() {
        // Hidden on purpose
    }

    static <T extends DataObject> @NonNull DataObjectIdentifier<T> legacyToIdentifier(
            final InstanceIdentifier<T> legacy) {
        try {
            return legacy.toIdentifier();
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
