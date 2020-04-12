/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;

abstract class AbstractAdapterFactory implements AdapterFactory {
    @Override
    public final DataBroker createDataBroker(final DOMDataBroker domService) {
        return new BindingDOMDataBrokerAdapter(domService, codec());
    }

    @Override
    public final DataTreeService createDataTreeService(final DOMDataTreeService domService) {
        return BindingDOMDataTreeServiceAdapter.create(domService, codec());
    }

    @Override
    public final MountPointService createMountPointService(final DOMMountPointService domService) {
        return new BindingDOMMountPointServiceAdapter(domService, codec());
    }

    @Override
    public final NotificationService createNotificationService(final DOMNotificationService domService) {
        return new BindingDOMNotificationServiceAdapter(domService, codec());
    }

    @Override
    public final NotificationPublishService createNotificationPublishService(
            final DOMNotificationPublishService domService) {
        return new BindingDOMNotificationPublishServiceAdapter(domService, codec());
    }

    @Override
    public final RpcConsumerRegistry createRpcConsumerRegistry(final DOMRpcService domService) {
        return new BindingDOMRpcServiceAdapter(domService, codec());
    }

    @Override
    public final RpcProviderService createRpcProviderService(final DOMRpcProviderService domService) {
        return new BindingDOMRpcProviderServiceAdapter(domService, codec());
    }

    @Override
    public final ActionService createActionService(final DOMActionService domService) {
        return new ActionServiceAdapter(codec(), domService);
    }

    @Override
    public final ActionProviderService createActionProviderService(final DOMActionProviderService domService) {
        return new ActionProviderServiceAdapter(codec(), domService);
    }

    abstract @NonNull AdapterContext codec();
}
