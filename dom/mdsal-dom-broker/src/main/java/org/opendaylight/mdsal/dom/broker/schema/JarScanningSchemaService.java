/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarScanningSchemaService implements ScanningSchemaService {

    private static final Logger LOG = LoggerFactory.getLogger(JarScanningSchemaService.class);

    @GuardedBy("lock")
    private final Object lock = new Object();

    public void tryToUpdateSchemaContext() {
        synchronized (lock) {
            final Optional<SchemaContext> schema = CONTEXT_RESOLVER.getSchemaContext();
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
        for (final ListenerRegistration<SchemaContextListener> registration : LISTENERS) {
            try {
                registration.getInstance().onGlobalContextUpdated(schemaContext);
            } catch (final Exception e) {
                LOG.error("Exception occured during invoking listener", e);
            }
        }
    }

    public List<Registration> registerAvalaibleYangs(final List<URL> yangs) {
        final List<Registration> sourceRegistrator = new ArrayList<>();
        for (final URL url : yangs) {
            try {
                sourceRegistrator.add(CONTEXT_RESOLVER.registerSource(url));
            } catch (SchemaSourceException | IOException | YangSyntaxErrorException e) {
                LOG.warn("Failed to register {}, ignoring it", url, e);
            }
        }
        return sourceRegistrator;
    }

    @Override
    public SchemaContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaContext getGlobalContext() {
        return CONTEXT_RESOLVER.getSchemaContext().orNull();
    }

    @Override
    public ListenerRegistration<SchemaContextListener>
            registerSchemaContextListener(final SchemaContextListener listener) {
        synchronized (lock) {
            final Optional<SchemaContext> potentialCtx = CONTEXT_RESOLVER.getSchemaContext();
            if (potentialCtx.isPresent()) {
                listener.onGlobalContextUpdated(potentialCtx.get());
            }
            return LISTENERS.register(listener);
        }
    }

    @Override
    public SchemaContext getSchemaContext() {
        return getGlobalContext();
    }

    @Override
    public CheckedFuture<? extends YangTextSchemaSource, SchemaSourceException>
            getSource(final SourceIdentifier sourceIdentifier) {
        return CONTEXT_RESOLVER.getSource(sourceIdentifier);
    }

    @Override
    public void close() {
        synchronized (lock) {
            for (final ListenerRegistration<SchemaContextListener> registration : LISTENERS) {
                registration.close();
            }
        }
    }
}
