/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;

/**
 * Wiring for DI of mdsal's base services in a standalone environment.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class MdsalWiring {

    private final ClassLoadingStrategy classLoadingStrategy =
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();

    private final ScanningSchemaServiceProvider schemaService = new ScanningSchemaServiceProvider();

    @PreDestroy
    public void close() {
        schemaService.close();
    }

    public ClassLoadingStrategy getClassLoadingStrategy() {
        return classLoadingStrategy;
    }

    public DOMSchemaService getDOMSchemaService() {
        return schemaService;
    }
}
