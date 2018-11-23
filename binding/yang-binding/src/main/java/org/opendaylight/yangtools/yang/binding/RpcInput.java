/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Marker interface for all interfaces generated for {@code input} statement within an {@code action} or an {@code rpc}
 * statement.
 */
public interface RpcInput extends DataObject {
    // FIXME: 4.0.0: MDSAL-395: make this default non-default
    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    default Class<? extends RpcInput> implementedInterface() {
        return (Class<? extends RpcInput>) verifyNotNull(getImplementedInterface());
    }
}
