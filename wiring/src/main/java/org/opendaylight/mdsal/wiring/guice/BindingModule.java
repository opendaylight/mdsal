/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingWiring;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactoryWiring;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.wiring.schema.SchemaWiring;

/**
 * Guice Module which Guice binds mdsal binding related services, such as the {@link DataBroker} and others.
 *
 * <p>This modules expects another module to bind a {@link DOMDataBroker}.
 *
 * <p>This module implicitly and automatically also installs the {@link DOMModule}.
 *
 * @author Michael Vorburger.ch
 */
public class BindingModule implements Module {

    private final ClassLoadingStrategy classLoadingStrategy =
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();

    private final SchemaWiring schemaWiring;

    public BindingModule(SchemaWiring schemaWiring) {
        this.schemaWiring = schemaWiring;
    }

    @Override
    public void configure(Binder binder) {
        binder.install(new DOMModule(schemaWiring));
    }

    @Provides
    @Singleton ClassLoadingStrategy getClassLoadingStrategy() {
        return classLoadingStrategy;
    }

    @Provides
    @Singleton BindingNormalizedNodeSerializer getBindingNormalizedNodeSerializer(BindingWiring bindingWiring) {
        return bindingWiring.getBindingNormalizedNodeSerializer();
    }

    @Provides
    @Singleton BindingCodecTreeFactory getBindingCodecTreeFactory(BindingWiring bindingWiring) {
        return bindingWiring.getBindingCodecTreeFactory();
    }

    @Provides
    @Singleton DataBroker getDataBroker(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getDataBroker();
    }

//  @Provides
//  @Singleton DataTreeService getDataTreeService(AdapterFactoryWiring adapterFactoryWiring) {
//      return adapterFactoryWiring.getDataTreeService();
//  }

    @Provides
    @Singleton NotificationService getNotificationService(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getNotificationService();
    }

    @Provides
    @Singleton NotificationPublishService getNotificationPublishService(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getNotificationPublishService();
    }

    @Provides
    @Singleton MountPointService getMountPointService(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getMountPointService();
    }

    @Provides
    @Singleton RpcConsumerRegistry getRpcConsumerRegistry(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getRpcConsumerRegistry();
    }

    @Provides
    @Singleton RpcProviderService getRpcProviderService(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getRpcProviderService();
    }

    @Provides
    @Singleton ActionService getActionService(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getActionService();
    }

    @Provides
    @Singleton ActionProviderService getActionProviderService(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getActionProviderService();
    }
}
