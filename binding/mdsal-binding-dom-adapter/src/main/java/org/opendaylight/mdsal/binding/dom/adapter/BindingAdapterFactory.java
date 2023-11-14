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
import java.util.ServiceLoader;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.concepts.Immutable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of AdapterFactory.
 *
 * @author Robert Varga
 */
@Beta
@Component(immediate = true, service = AdapterFactory.class)
@MetaInfServices(value = AdapterFactory.class)
@NonNullByDefault
@Singleton
public final class BindingAdapterFactory implements AdapterFactory, Immutable {
    private final AdapterContext codec;

    public BindingAdapterFactory() {
        this(ServiceLoader.load(AdapterContext.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to load BlockingBindingNormalizer")));
    }

    @Inject
    @Activate
    public BindingAdapterFactory(@Reference final AdapterContext codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    public DataBroker createDataBroker(final DOMDataBroker domService) {
        return new BindingDOMDataBrokerAdapter(codec, domService);
    }

    @Override
    public MountPointService createMountPointService(final DOMMountPointService domService) {
        return new BindingDOMMountPointServiceAdapter(codec, domService);
    }

    @Override
    public NotificationService createNotificationService(final DOMNotificationService domService) {
        return new BindingDOMNotificationServiceAdapter(codec, domService);
    }

    @Override
    public NotificationPublishService createNotificationPublishService(
            final DOMNotificationPublishService domService) {
        return new BindingDOMNotificationPublishServiceAdapter(codec, domService);
    }

    @Override
    public RpcService createRpcConsumerRegistry(final DOMRpcService domService) {
        return new BindingDOMRpcServiceAdapter(codec, domService);
    }

    @Override
    public RpcProviderService createRpcProviderService(final DOMRpcProviderService domService) {
        return new BindingDOMRpcProviderServiceAdapter(codec, domService);
    }

    @Override
    public ActionService createActionService(final DOMActionService domService) {
        return new ActionServiceAdapter(codec, domService);
    }

    @Override
    public ActionProviderService createActionProviderService(final DOMActionProviderService domService) {
        return new ActionProviderServiceAdapter(codec, domService);
    }
}
