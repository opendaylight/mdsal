/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
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
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public final class AdapterFactory {
    private final BindingToNormalizedNodeCodec codec;

    public AdapterFactory(final BindingToNormalizedNodeCodec codec) {
        this.codec = requireNonNull(codec);
    }

    /**
     * Create a {@link DataBroker} backed by a {@link DOMDataBroker}.
     *
     * @param domService Backing DOMDataBroker
     * @return A DataBroker
     * @throws NullPointerException if {@code domService} is null
     */
    public DataBroker createDataBroker(final DOMDataBroker domService) {
        return new BindingDOMDataBrokerAdapter(domService, codec);
    }

    /**
     * Create a {@link DataTreeService} backed by a {@link DOMDataTreeService}.
     *
     * @param domService Backing DOMDataTreeService
     * @return A DataTreeService
     * @throws NullPointerException if {@code domService} is null
     */
    public DataTreeService createDataTreeService(final DOMDataTreeService domService) {
        return BindingDOMDataTreeServiceAdapter.create(domService, codec);
    }

    /**
     * Create a {@link MountPointService} backed by a {@link DOMMountPointService}.
     *
     * @param domService Backing DOMMountPointService
     * @return A MountPointService
     * @throws NullPointerException if {@code domService} is null
     */
    public MountPointService createMountPointService(final DOMMountPointService domService) {
        return new BindingDOMMountPointServiceAdapter(domService, codec);
    }

    /**
     * Create a {@link DataBroker} backed by a {@link DOMDataBroker}.
     *
     * @param domService Backing DOMDataBroker
     * @return A DataBroker
     * @throws NullPointerException if {@code domService} is null
     */
    public NotificationService createNotificationService(final DOMNotificationService domService) {
        return new BindingDOMNotificationServiceAdapter(domService, codec);
    }

    /**
     * Create a {@link NotificationPublishService} backed by a {@link DOMNotificationPublishService}.
     *
     * @param domService Backing DOMNotificationPublishService
     * @return A NotificationPublishService
     * @throws NullPointerException if {@code domService} is null
     */
    public NotificationPublishService createNotificationPublishService(final DOMNotificationPublishService domService) {
        return new BindingDOMNotificationPublishServiceAdapter(domService, codec);
    }

    /**
     * Create a {@link RpcConsumerRegistry} backed by a {@link DOMRpcService}.
     *
     * @param domService Backing DOMRpcService
     * @return A RpcConsumerRegistry
     * @throws NullPointerException if {@code domService} is null
     */
    public RpcConsumerRegistry createRpcConsumerRegistry(final DOMRpcService domService) {
        return new BindingDOMRpcServiceAdapter(domService, codec);
    }

    /**
     * Create a {@link RpcProviderService} backed by a {@link DOMRpcProviderService}.
     *
     * @param domService Backing DOMRpcProviderService
     * @return A RpcProviderService
     * @throws NullPointerException if {@code domService} is null
     */
    public RpcProviderService createRpcProviderService(final DOMRpcProviderService domService) {
        return new BindingDOMRpcProviderServiceAdapter(domService, codec);
    }

    /**
     * Create a {@link ActionService} backed by a {@link DOMOperationService}.
     *
     * @param domService Backing DOMOperationService
     * @return A ActionService
     * @throws NullPointerException if {@code domService} is null
     */
    public ActionService createActionService(final DOMOperationService domService) {
        return new ActionServiceAdapter(codec, domService);
    }
}
