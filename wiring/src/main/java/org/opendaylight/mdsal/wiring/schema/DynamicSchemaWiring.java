/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SchemaWiring} base class for both {@link PurelyDynamicSchemaWiring} and {@link HybridSchemaWiring}.
 *
 * @author Michael Vorburger.ch
 */
class DynamicSchemaWiring implements SchemaWiring {
    // intentionally package local, for now

    private final ScanningSchemaServiceProvider scanningSchemaServiceProvider;
    private final Optional<YangRegisterer> yangRegisterer = Optional.of(new YangRegistererImpl());

    DynamicSchemaWiring(ScanningSchemaServiceProvider scanningSchemaServiceProvider) {
        this.scanningSchemaServiceProvider = scanningSchemaServiceProvider;
    }

    @Override
    public SchemaContextProvider getSchemaContextProvider() {
        return scanningSchemaServiceProvider;
    }

    @Override
    public SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider() {
        return scanningSchemaServiceProvider;
    }

    @Override
    public DOMSchemaService getDOMSchemaService() {
        return scanningSchemaServiceProvider;
    }

    @Override
    public DOMYangTextSourceProvider getDOMYangTextSourceProvider() {
        return scanningSchemaServiceProvider;
    }

    @Override
    public Optional<YangRegisterer> getYangRegisterer() {
        return yangRegisterer;
    }

    private class YangRegistererImpl implements YangRegisterer {
        private final Logger log = LoggerFactory.getLogger(DynamicSchemaWiring.YangRegistererImpl.class);

        @Override
        public List<Registration> registerYANGs(List<URI> yangs) {
            List<URL> yangURLs = new ArrayList<>(yangs.size());
            for (URI yangURI : yangs) {
                try {
                    yangURLs.add(yangURI.toURL());
                } catch (MalformedURLException e) {
                    log.error("URI.toURL failed: {}", yangURI, e);
                }
            }
            List<Registration> registration = scanningSchemaServiceProvider.registerAvailableYangs(yangURLs);
            scanningSchemaServiceProvider.tryToUpdateSchemaContext();
            return registration;
        }
    }
}
