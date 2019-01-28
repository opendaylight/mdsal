/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.spi.schema.AbstractDOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanningSchemaServiceProvider extends AbstractDOMSchemaService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ScanningSchemaServiceProvider.class);

    private final YangTextSchemaContextResolver contextResolver;

    @GuardedBy("lock")
    private final ListenerRegistry<SchemaContextListener> listeners = ListenerRegistry.create();
    private final Object lock = new Object();

    public ScanningSchemaServiceProvider() {
        this(YangTextSchemaContextResolver.create("global-bundle"));
    }

    private ScanningSchemaServiceProvider(YangTextSchemaContextResolver contextResolver) {
        super(() -> contextResolver.getSchemaContext().orElse(null),
            sourceIdentifier -> contextResolver.getSource(sourceIdentifier));
        this.contextResolver = contextResolver;
    }

    public void tryToUpdateSchemaContext() {
        synchronized (lock) {
            final Optional<SchemaContext> schema = contextResolver.getSchemaContext();
            if (schema.isPresent()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got new SchemaContext: # of modules {}", schema.get().getModules().size());
                }
                notifyListeners(schema.get());
            }
        }
    }

    @VisibleForTesting
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void notifyListeners(final SchemaContext schemaContext) {
        synchronized (lock) {
            for (final ListenerRegistration<SchemaContextListener> registration : listeners) {
                try {
                    registration.getInstance().onGlobalContextUpdated(schemaContext);
                } catch (final Exception e) {
                    LOG.error("Exception occured during invoking listener", e);
                }
            }
        }
    }

    public List<Registration> registerAvailableYangs(final List<URL> yangs) {
        final List<Registration> sourceRegistrator = new ArrayList<>();
        for (final URL url : yangs) {
            try {
                sourceRegistrator.add(contextResolver.registerSource(url));
            } catch (SchemaSourceException | IOException | YangSyntaxErrorException e) {
                LOG.warn("Failed to register {}, ignoring it", url, e);
            }
        }
        return sourceRegistrator;
    }

    public void removeListener(final SchemaContextListener schemaContextListener) {
        synchronized (lock) {
            for (final ListenerRegistration<SchemaContextListener> listenerRegistration : listeners.getListeners()) {
                if (listenerRegistration.getInstance().equals(schemaContextListener)) {
                    listenerRegistration.close();
                    break;
                }
            }
        }
    }

    public boolean hasListeners() {
        synchronized (lock) {
            return !Iterables.isEmpty(listeners.getListeners());
        }
    }

    @Override
    public ListenerRegistration<SchemaContextListener>
            registerSchemaContextListener(final SchemaContextListener listener) {
        synchronized (lock) {
            contextResolver.getSchemaContext().ifPresent(listener::onGlobalContextUpdated);
            return listeners.register(listener);
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            listeners.forEach(ListenerRegistration::close);
        }
    }
}
