/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Marker interface for generated classes which expose{@link QName} of the underlying YANG statement which produced
 * them.
 */
@NonNullByDefault
public sealed interface QNameAware permits YangFeature {
    /**
     * Return the {@link QName} associated with this construct.
     *
     * @return A QName
     */
    QName qname();
}
