/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.CheckedFuture;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanningSchemaServiceProvider
        implements DOMSchemaService, SchemaContextProvider, DOMYangTextSourceProvider, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ScanningSchemaServiceProvider.class);

    @GuardedBy("lock")
    private final ListenerRegistry<SchemaContextListener> listeners = new ListenerRegistry<>();
    private final Object lock = new Object();
    private final YangTextSchemaContextResolver contextResolver = YangTextSchemaContextResolver.create("global-bundle");

    public void tryToUpdateSchemaContext() {
        synchronized (lock) {
            final Optional<SchemaContext> schema = contextResolver.getSchemaContext();
            if (schema.isPresent()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got new SchemaContext: # of modules {}", schema.get().getAllModuleIdentifiers().size());
                }
                notifyListeners(schema.get());
            }
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public void notifyListeners(final SchemaContext schemaContext) {
        for (final ListenerRegistration<SchemaContextListener> registration : listeners) {
            try {
                registration.getInstance().onGlobalContextUpdated(schemaContext);
            } catch (final Exception e) {
                LOG.error("Exception occured during invoking listener", e);
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

    public boolean isEmpty() {
        boolean isEmpty;
        synchronized (lock) {
            if (Iterables.size(listeners.getListeners()) > 0) {
                isEmpty = false;
            } else {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    @Override
    public SchemaContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaContext getGlobalContext() {
        return contextResolver.getSchemaContext().orNull();
    }

    @Override
    public ListenerRegistration<SchemaContextListener>
            registerSchemaContextListener(final SchemaContextListener listener) {
        synchronized (lock) {
            final Optional<SchemaContext> potentialCtx = contextResolver.getSchemaContext();
            if (potentialCtx.isPresent()) {
                listener.onGlobalContextUpdated(potentialCtx.get());
            }
            return listeners.register(listener);
        }
    }

    @Override
    public SchemaContext getSchemaContext() {
        return getGlobalContext();
    }

    @Override
    public CheckedFuture<? extends YangTextSchemaSource, SchemaSourceException>
            getSource(final SourceIdentifier sourceIdentifier) {
        return contextResolver.getSource(sourceIdentifier);
    }

    @Override
    public Map<Class<? extends DOMSchemaServiceExtension>, DOMSchemaServiceExtension> getSupportedExtensions() {
        return ImmutableMap.of(DOMYangTextSourceProvider.class, this);
    }

    @Override
    public void close() {
        synchronized (lock) {
            for (final ListenerRegistration<SchemaContextListener> registration : listeners) {
                registration.close();
            }
        }
    }
}
