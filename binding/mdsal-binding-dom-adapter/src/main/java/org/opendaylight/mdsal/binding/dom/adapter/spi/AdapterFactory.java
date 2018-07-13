/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.spi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Factory to turn various {@link DOMService}s into {@link BindingService}s.
 *
 * @author Thomas Pantelis
 */
@Beta
@NonNullByDefault
public interface AdapterFactory {
    /**
     * Create a {@link DataBroker} backed by a {@link DOMDataBroker}.
     *
     * @param domService Backing DOMDataBroker
     * @return A DataBroker
     * @throws NullPointerException if {@code domService} is null
     */
    DataBroker createDataBroker(DOMDataBroker domService);

    /**
     * Create a {@link DataTreeService} backed by a {@link DOMDataTreeService}.
     *
     * @param domService Backing DOMDataTreeService
     * @return A DataTreeService
     * @throws NullPointerException if {@code domService} is null
     */
    DataTreeService createDataTreeService(DOMDataTreeService domService);

    /**
     * Create a {@link MountPointService} backed by a {@link DOMMountPointService}.
     *
     * @param domService Backing DOMMountPointService
     * @return A MountPointService
     * @throws NullPointerException if {@code domService} is null
     */
    MountPointService createMountPointService(DOMMountPointService domService);

    /**
     * Create a {@link DataBroker} backed by a {@link DOMDataBroker}.
     *
     * @param domService Backing DOMDataBroker
     * @return A DataBroker
     * @throws NullPointerException if {@code domService} is null
     */
    NotificationService createNotificationService(DOMNotificationService domService);

    /**
     * Create a {@link NotificationPublishService} backed by a {@link DOMNotificationPublishService}.
     *
     * @param domService Backing DOMNotificationPublishService
     * @return A NotificationPublishService
     * @throws NullPointerException if {@code domService} is null
     */
    NotificationPublishService createNotificationPublishService(DOMNotificationPublishService domService);

    /**
     * Create a {@link RpcConsumerRegistry} backed by a {@link DOMRpcService}.
     *
     * @param domService Backing DOMRpcService
     * @return A RpcConsumerRegistry
     * @throws NullPointerException if {@code domService} is null
     */
    RpcConsumerRegistry createRpcConsumerRegistry(DOMRpcService domService);

    /**
     * Create a {@link RpcProviderService} backed by a {@link DOMRpcProviderService}.
     *
     * @param domService Backing DOMRpcProviderService
     * @return A RpcProviderService
     * @throws NullPointerException if {@code domService} is null
     */
    RpcProviderService createRpcProviderService(DOMRpcProviderService domService);

    /**
     * Create a {@link ActionService} backed by a {@link DOMOperationService}.
     *
     * @param domService Backing DOMOperationService
     * @return A ActionService
     * @throws NullPointerException if {@code domService} is null
     */
    ActionService createActionService(DOMOperationService domService);
}
