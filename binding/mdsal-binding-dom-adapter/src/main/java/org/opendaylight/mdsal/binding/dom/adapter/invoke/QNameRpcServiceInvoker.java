/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import java.lang.reflect.Method;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;

final class QNameRpcServiceInvoker extends AbstractMappedRpcInvoker<QName> {
    QNameRpcServiceInvoker(final Map<QName, Method> qnameToMethod) {
        super(qnameToMethod);
    }

    @Override
    protected QName qnameToKey(final QName qname) {
        return qname;
    }
}
