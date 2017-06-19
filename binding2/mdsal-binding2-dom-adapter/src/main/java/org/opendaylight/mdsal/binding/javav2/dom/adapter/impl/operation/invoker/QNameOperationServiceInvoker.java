/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import com.google.common.annotations.Beta;
import java.lang.reflect.Method;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
final class QNameOperationServiceInvoker extends AbstractMappedOperationInvoker<QName> {

    private QNameOperationServiceInvoker(final Map<QName, Method> qnameToMethod) {
        super(qnameToMethod);
    }

    static OperationServiceInvoker instanceFor(final Map<QName, Method> qnameToMethod) {
        return new QNameOperationServiceInvoker(qnameToMethod);
    }

    @Override
    protected QName qnameToKey(final QName qname) {
        return qname;
    }
}