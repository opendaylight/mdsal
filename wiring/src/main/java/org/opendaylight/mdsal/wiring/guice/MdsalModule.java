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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.adapter.BindingWiring;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactoryWiring;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.wiring.MdsalWiring;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Guice Module which binds mdsal services such as the {@link DataBroker} and others.
 *
 * <p>This modules expects another module to bind a {@link DOMDataBroker}.
 *
 * @author Michael Vorburger.ch
 */
public class MdsalModule implements Module {

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton SchemaContextProvider getSchemaContextProvider(MdsalWiring mdsalWiring) {
        return mdsalWiring.getSchemaContextProvider();
    }

    @Provides
    @Singleton SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider(MdsalWiring mdsalWiring) {
        return mdsalWiring.getSchemaSourceProvider();
    }

    @Provides
    @Singleton DOMSchemaService getSchemaService(MdsalWiring mdsalWiring) {
        return mdsalWiring.getDOMSchemaService();
    }

    @Provides
    @Singleton DOMYangTextSourceProvider getDOMYangTextSourceProvider(MdsalWiring mdsalWiring) {
        return mdsalWiring.getDOMYangTextSourceProvider();
    }

    @Provides
    @Singleton ClassLoadingStrategy getClassLoadingStrategy(MdsalWiring mdsalWiring) {
        return mdsalWiring.getClassLoadingStrategy();
    }

    @Provides
    @Singleton DataBroker getDataBroker(AdapterFactoryWiring adapterFactoryWiring) {
        return adapterFactoryWiring.getDataBroker();
    }

    @Provides
    @Singleton BindingNormalizedNodeSerializer getBindingNormalizedNodeSerializer(BindingWiring bindingWiring) {
        return bindingWiring.getBindingNormalizedNodeSerializer();
    }

    @Provides
    @Singleton BindingCodecTreeFactory getBindingCodecTreeFactory(BindingWiring bindingWiring) {
        return bindingWiring.getBindingCodecTreeFactory();
    }
}
