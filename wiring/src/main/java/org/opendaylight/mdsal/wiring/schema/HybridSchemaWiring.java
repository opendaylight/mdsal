/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import static com.google.common.util.concurrent.Futures.immediateFuture;

import com.google.common.util.concurrent.Futures;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.mdsal.dom.spi.schema.AbstractDOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link SchemaWiring} which combines the
 * {@link PurelyClassLoadingSchemaWiring} and the
 * {@link PurelyDynamicSchemaWiring}.
 *
 * @author Michael Vorburger.ch
 */
public class HybridSchemaWiring implements SchemaWiring {

    private class AnotherHybridDOMSchemaService extends AbstractDOMSchemaService {

        AnotherHybridDOMSchemaService() {
            super(getSchemaContextProvider(), getSchemaSourceProvider());
        }

        @Override
        public ListenerRegistration<SchemaContextListener> registerSchemaContextListener(
                SchemaContextListener listener) {
            return hybridDOMSchemaService.registerSchemaContextListener(listener);
        }
    }

    private final HybridDOMSchemaService hybridDOMSchemaService;
    private final SchemaWiring dynamicSchemaWiring;
    private final SchemaWiring fixedSchemaWiring;
    private final AnotherHybridDOMSchemaService anotherHybridDOMSchemaService;

    public HybridSchemaWiring() {
        this.hybridDOMSchemaService = new HybridDOMSchemaService(getSchemaContextProvider());
        this.dynamicSchemaWiring = new DynamicSchemaWiring(hybridDOMSchemaService);
        this.fixedSchemaWiring = new PurelyClassLoadingSchemaWiring();
        this.anotherHybridDOMSchemaService = new AnotherHybridDOMSchemaService();
    }

    @Override
    public SchemaContextProvider getSchemaContextProvider() {
        return () -> {
            SchemaContext dynContext = dynamicSchemaWiring.getSchemaContextProvider().getSchemaContext();
            SchemaContext fixedContext = fixedSchemaWiring.getSchemaContextProvider().getSchemaContext();
            if (dynContext != null) {
                return new HybridSchemaContext(dynContext, fixedContext);
            } else {
                return fixedContext;
            }
        };
    }

    @Override
    public SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider() {
        return sourceIdentifier -> {
            try {
                return immediateFuture(dynamicSchemaWiring.getSchemaSourceProvider().getSource(sourceIdentifier).get());
            } catch (ExecutionException e1) {
                if (e1.getCause() instanceof MissingSchemaSourceException) {
                    try {
                        return immediateFuture(
                                fixedSchemaWiring.getSchemaSourceProvider().getSource(sourceIdentifier).get());
                    } catch (InterruptedException | ExecutionException e2) {
                        return Futures.immediateFailedFuture(e2);
                    }
                } else {
                    return Futures.immediateFailedFuture(e1);
                }
            } catch (InterruptedException e1) {
                return Futures.immediateFailedFuture(e1);
            }
        };
    }

    @Override
    public DOMSchemaService getDOMSchemaService() {
        return anotherHybridDOMSchemaService;
    }

    @Override
    public DOMYangTextSourceProvider getDOMYangTextSourceProvider() {
        return anotherHybridDOMSchemaService;
    }

    @Override
    public Optional<YangRegisterer> getYangRegisterer() {
        return dynamicSchemaWiring.getYangRegisterer();
    }
}
