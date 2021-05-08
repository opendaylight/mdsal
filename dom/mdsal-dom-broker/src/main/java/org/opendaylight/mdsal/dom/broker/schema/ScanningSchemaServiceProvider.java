/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.mdsal.dom.spi.AbstractDOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ScanningSchemaServiceProvider extends AbstractDOMSchemaService.WithYangTextSources
        implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ScanningSchemaServiceProvider.class);

    private final YangTextSchemaContextResolver contextResolver = YangTextSchemaContextResolver.create("global-bundle");
    @GuardedBy("lock")
    private final ListenerRegistry<EffectiveModelContextListener> listeners = ListenerRegistry.create();
    private final Object lock = new Object();

    public void tryToUpdateSchemaContext() {
        synchronized (lock) {
            final Optional<? extends EffectiveModelContext> optSchema = contextResolver.getEffectiveModelContext();
            optSchema.ifPresent(schema -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got new SchemaContext: # of modules {}", schema.getModules().size());
                }
                notifyListeners(schema);
            });
        }
    }

    @VisibleForTesting
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void notifyListeners(final EffectiveModelContext schemaContext) {
        synchronized (lock) {
            listeners.streamListeners().forEach(listener -> {
                try {
                    listener.onModelContextUpdated(schemaContext);
                } catch (final Exception e) {
                    LOG.error("Exception occured during invoking listener {}", listener, e);
                }
            });
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

    public boolean hasListeners() {
        synchronized (lock) {
            return !listeners.isEmpty();
        }
    }

    @Override
    public EffectiveModelContext getGlobalContext() {
        return contextResolver.getEffectiveModelContext().orElse(null);
    }

    @Override
    public ListenerRegistration<EffectiveModelContextListener> registerSchemaContextListener(
            final EffectiveModelContextListener listener) {
        synchronized (lock) {
            contextResolver.getEffectiveModelContext().ifPresent(listener::onModelContextUpdated);
            return listeners.register(listener);
        }
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return contextResolver.getSource(sourceIdentifier);
    }

    @Override
    public void close() {
        synchronized (lock) {
            listeners.clear();
        }
    }
}
