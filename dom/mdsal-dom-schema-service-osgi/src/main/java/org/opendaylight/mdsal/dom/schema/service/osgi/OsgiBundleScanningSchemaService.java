/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.service.osgi;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.CheckedFuture;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
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
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiBundleScanningSchemaService implements SchemaContextProvider, DOMSchemaService,
        ServiceTrackerCustomizer<SchemaContextListener, SchemaContextListener>, DOMYangTextSourceProvider,
        AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiBundleScanningSchemaService.class);

    private static final AtomicReference<OsgiBundleScanningSchemaService> GLOBAL_INSTANCE = new AtomicReference<>();

    private static final long FRAMEWORK_BUNDLE_ID = 0;

    @GuardedBy("lock")
    private final ListenerRegistry<SchemaContextListener> listeners = new ListenerRegistry<>();
    private final YangTextSchemaContextResolver contextResolver = YangTextSchemaContextResolver.create("global-bundle");
    private final BundleScanner scanner = new BundleScanner();
    private final Object lock = new Object();
    private final BundleContext context;

    private ServiceTracker<SchemaContextListener, SchemaContextListener> listenerTracker;
    private BundleTracker<Iterable<Registration>> bundleTracker;
    private boolean starting = true;

    private volatile boolean stopping;

    private OsgiBundleScanningSchemaService(final BundleContext context) {
        this.context = Preconditions.checkNotNull(context);
    }

    public static @Nonnull OsgiBundleScanningSchemaService createInstance(final BundleContext ctx) {
        OsgiBundleScanningSchemaService instance = new OsgiBundleScanningSchemaService(ctx);
        Preconditions.checkState(GLOBAL_INSTANCE.compareAndSet(null, instance));
        instance.start();
        return instance;
    }

    public static OsgiBundleScanningSchemaService getInstance() {
        OsgiBundleScanningSchemaService instance = GLOBAL_INSTANCE.get();
        Preconditions.checkState(instance != null, "Global Instance was not instantiated");
        return instance;
    }

    @VisibleForTesting
    public static void destroyInstance() {
        OsgiBundleScanningSchemaService instance = GLOBAL_INSTANCE.getAndSet(null);
        if (instance != null) {
            instance.close();
        }
    }

    public BundleContext getContext() {
        return context;
    }

    private void start() {
        checkState(context != null);
        LOG.debug("start() starting");

        listenerTracker = new ServiceTracker<>(context, SchemaContextListener.class, this);
        bundleTracker = new BundleTracker<>(context, Bundle.RESOLVED | Bundle.STARTING
                | Bundle.STOPPING | Bundle.ACTIVE, scanner);

        synchronized (lock) {
            bundleTracker.open();

            LOG.debug("BundleTracker.open() complete");

            if (Iterables.size(listeners.getListeners()) > 0) {
                tryToUpdateSchemaContext();
            }
        }

        listenerTracker.open();
        starting = false;

        LOG.debug("start() complete");
    }

    @Override
    public SchemaContext getSchemaContext() {
        return getGlobalContext();
    }

    @Override
    public SchemaContext getGlobalContext() {
        return contextResolver.getSchemaContext().orNull();
    }

    @Override
    public SchemaContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenerRegistration<SchemaContextListener> registerSchemaContextListener(
            final SchemaContextListener listener) {
        synchronized (lock) {
            final Optional<SchemaContext> potentialCtx = contextResolver.getSchemaContext();
            if (potentialCtx.isPresent()) {
                listener.onGlobalContextUpdated(potentialCtx.get());
            }
            return listeners.register(listener);
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            stopping = true;
            if (bundleTracker != null) {
                bundleTracker.close();
                bundleTracker = null;
            }
            if (listenerTracker != null) {
                listenerTracker.close();
                listenerTracker = null;
            }

            for (final ListenerRegistration<SchemaContextListener> l : listeners.getListeners()) {
                l.close();
            }
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @VisibleForTesting
    @GuardedBy("lock")
    void notifyListeners(final SchemaContext snapshot) {
        final Object[] services = listenerTracker.getServices();
        for (final ListenerRegistration<SchemaContextListener> listener : listeners) {
            try {
                listener.getInstance().onGlobalContextUpdated(snapshot);
            } catch (final Exception e) {
                LOG.error("Exception occured during invoking listener", e);
            }
        }
        if (services != null) {
            for (final Object rawListener : services) {
                final SchemaContextListener listener = (SchemaContextListener) rawListener;
                try {
                    listener.onGlobalContextUpdated(snapshot);
                } catch (final Exception e) {
                    LOG.error("Exception occured during invoking listener {}", listener, e);
                }
            }
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private class BundleScanner implements BundleTrackerCustomizer<Iterable<Registration>> {
        @Override
        public Iterable<Registration> addingBundle(final Bundle bundle, final BundleEvent event) {

            if (bundle.getBundleId() == FRAMEWORK_BUNDLE_ID) {
                return Collections.emptyList();
            }

            final Enumeration<URL> enumeration = bundle.findEntries("META-INF/yang", "*.yang", false);
            if (enumeration == null) {
                return Collections.emptyList();
            }

            final List<Registration> urls = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                final URL u = enumeration.nextElement();
                try {
                    urls.add(contextResolver.registerSource(u));
                    LOG.debug("Registered {}", u);
                } catch (final Exception e) {
                    LOG.warn("Failed to register {}, ignoring it", e);
                }
            }

            if (!urls.isEmpty()) {
                LOG.debug("Loaded {} new URLs from bundle {}, attempting to rebuild schema context",
                        urls.size(), bundle.getSymbolicName());
                tryToUpdateSchemaContext();
            }

            return ImmutableList.copyOf(urls);
        }

        @Override
        public void modifiedBundle(final Bundle bundle, final BundleEvent event, final Iterable<Registration> object) {
            if (bundle.getBundleId() == FRAMEWORK_BUNDLE_ID) {
                LOG.debug("Framework bundle {} got event {}", bundle, event.getType());
                if ((event.getType() & BundleEvent.STOPPING) != 0) {
                    LOG.info("OSGi framework is being stopped, halting bundle scanning");
                    stopping = true;
                }
            }
        }

        /**
         * If removing YANG files makes yang store inconsistent, method
         * {@link #getYangStoreSnapshot()} will throw exception. There is no
         * rollback.
         */
        @SuppressWarnings("checkstyle:IllegalCatch")
        @Override
        public void removedBundle(final Bundle bundle, final BundleEvent event, final Iterable<Registration> urls) {
            for (final Registration url : urls) {
                try {
                    url.close();
                } catch (final Exception e) {
                    LOG.warn("Failed do unregister URL {}, proceeding", url, e);
                }
            }

            final int numUrls = Iterables.size(urls);
            if (numUrls > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removedBundle: {}, state: {}, # urls: {}", bundle.getSymbolicName(),
                            bundle.getState(), numUrls);
                }

                tryToUpdateSchemaContext();
            }
        }
    }

    @Override
    public SchemaContextListener addingService(final ServiceReference<SchemaContextListener> reference) {

        final SchemaContextListener listener = context.getService(reference);
        final SchemaContext ctxContext = getGlobalContext();
        if (getContext() != null && ctxContext != null) {
            listener.onGlobalContextUpdated(ctxContext);
        }
        return listener;
    }

    public void tryToUpdateSchemaContext() {
        if (starting || stopping) {
            return;
        }

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

    @Override
    public void modifiedService(final ServiceReference<SchemaContextListener> reference,
            final SchemaContextListener service) {
        // NOOP
    }

    @Override
    public void removedService(final ServiceReference<SchemaContextListener> reference,
            final SchemaContextListener service) {
        context.ungetService(reference);
    }

    @Override
    public Map<Class<? extends DOMSchemaServiceExtension>, DOMSchemaServiceExtension> getSupportedExtensions() {
        return ImmutableMap.of(DOMYangTextSourceProvider.class, this);
    }

    @Override
    public CheckedFuture<? extends YangTextSchemaSource, SchemaSourceException> getSource(
            final SourceIdentifier sourceIdentifier) {
        return contextResolver.getSource(sourceIdentifier);
    }
}
