/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Base Identity. Interface generated for {@code identity} statements extend this interface.
 */
public non-sealed interface BaseIdentity extends BindingObject, BindingContract<BaseIdentity> {
    /**
     * Return the {@link QName} associated with this {@code identity}.
     *
     * @return A QName
     */
    @NonNull QName qname();
}
