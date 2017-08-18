/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.CheckedFuture;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
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

public class JarScanningSchemaService
        implements DOMSchemaService, SchemaContextProvider, DOMYangTextSourceProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(JarScanningSchemaService.class);

    @GuardedBy("lock")
    private final ListenerRegistry<SchemaContextListener> listeners = new ListenerRegistry<>();
    private final Object lock = new Object();

    private final YangTextSchemaContextResolver contextResolver = YangTextSchemaContextResolver.create("global-bundle");
    private final List<URL> yangs = new ArrayList<>();

    private boolean starting = true;
    private volatile boolean stopping;

    public void start(final List<URL> yangs) {
        this.yangs.addAll(yangs);
        LOG.info("JAR scanning schema service starting");
        registerAvalaibleYangs();

        if (Iterables.size(listeners.getListeners()) > 0) {
            tryToUpdateSchemaContext();
        }

        starting = false;
    }

    private void tryToUpdateSchemaContext() {
        if (starting || stopping) {
            return;
        }

        synchronized (lock) {
            final Optional<SchemaContext> schema = contextResolver.getSchemaContext();
            if (schema.isPresent()) {
                notifyListeners(schema.get());
            }
        }
    }

    private void notifyListeners(final SchemaContext schemaContext) {
        for (final ListenerRegistration<SchemaContextListener> registration : listeners) {
            registration.getInstance().onGlobalContextUpdated(schemaContext);
        }
    }

    private void registerAvalaibleYangs() {
        for (final URL url : yangs) {
            try {
                contextResolver.registerSource(url);
            } catch (SchemaSourceException | IOException | YangSyntaxErrorException e) {
                LOG.warn("Failed to register {}, ignoring it", url, e);
            }
        }
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
    public void close() throws Exception {
        synchronized (lock) {
            for (final ListenerRegistration<SchemaContextListener> registration : listeners) {
                registration.close();
            }
        }
    }
}
