/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Internal interface exposing the {@link YangInstanceIdentifier} backing a {@link BindingInstanceIdentifier}
 * specialization.
 */
@NonNullByDefault
sealed interface CodecInstanceIdentifier permits CodecDataObjectIdentifier, CodecKeyAwareIdentifier,
        CodecPropertyValueIdentifier, CodecRootValueIdentifier {
    /**
     * Return the backing {@link YangInstanceIdentifier}.
     *
     * @return A {@link YangInstanceIdentifier}
     */
    YangInstanceIdentifier dom();
}
