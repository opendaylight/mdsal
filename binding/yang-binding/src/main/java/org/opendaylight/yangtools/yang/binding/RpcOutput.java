/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;

/**
 * Marker interface for all interfaces generated for {@code output} statement within an {@code action} or an {@code rpc}
 * statement.
 *
 * @author Robert Varga
 */
@Beta
public interface RpcOutput extends DataObject {
    // FIXME: 4.0.0: MDSAL-395: make this default non-default
    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    default Class<? extends RpcOutput> implementedInterface() {
        return (Class<? extends RpcOutput>) verifyNotNull(getImplementedInterface());
    }
}
