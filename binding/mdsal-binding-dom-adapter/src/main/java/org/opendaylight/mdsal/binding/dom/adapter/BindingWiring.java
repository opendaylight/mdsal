/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;

/**
 * Wiring for dependency injection (DI).
 *
 * <p>This class does not depend on any particular DI framework.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class BindingWiring {

    private final BindingToNormalizedNodeCodec codec;
    private final AdapterFactory adapterFactory;

    @Inject
    public BindingWiring(ClassLoadingStrategy classLoadingStrategy, DOMSchemaService schemaService) {
        codec = BindingToNormalizedNodeCodec.newInstance(classLoadingStrategy, schemaService);
        schemaService.registerSchemaContextListener(codec);
        adapterFactory = new BindingAdapterFactory(codec);
    }

    @PreDestroy
    public void close() {
        codec.close();
    }

    public BindingNormalizedNodeSerializer getBindingNormalizedNodeSerializer() {
        return codec;
    }

    public BindingCodecTreeFactory getBindingCodecTreeFactory() {
        return codec;
    }

    public AdapterFactory getAdapterFactory() {
        return adapterFactory;
    }
}
