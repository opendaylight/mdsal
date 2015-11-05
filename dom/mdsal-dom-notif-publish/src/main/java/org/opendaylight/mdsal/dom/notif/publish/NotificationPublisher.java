/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.notif.publish;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

public final class NotificationPublisher implements AutoCloseable, SchemaContextListener {
    private final DOMNotificationPublishService notifBroker;
    private final DOMRpcProviderService rpcBroker;
    private DOMRpcImplementationRegistration<PublishNotificationRpc> reg;

    private NotificationPublisher(final DOMNotificationPublishService notifBroker,
            final DOMRpcProviderService rpcBroker) {
        this.notifBroker = Preconditions.checkNotNull(notifBroker);
        this.rpcBroker = Preconditions.checkNotNull(rpcBroker);
    }

    static NotificationPublisher create(final DOMSchemaService schemaService,
            final DOMNotificationPublishService notifBroker, final DOMRpcProviderService rpcBroker) {

        final NotificationPublisher ret = new NotificationPublisher(notifBroker, rpcBroker);

        schemaService.registerSchemaContextListener(ret);

        // FIXME: register for global schema context updates

        return ret;
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        final DOMRpcImplementationRegistration<PublishNotificationRpc> newReg = rpcBroker.registerRpcImplementation(
            new PublishNotificationRpc(notifBroker, context), PublishNotificationRpc.RPC);

        if (reg != null) {
            reg.close();
        }
        reg = newReg;
    }

    @Override
    public void close() {
        if (reg != null) {
            reg.close();
            reg = null;
        }
    }
}
