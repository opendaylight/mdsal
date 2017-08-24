/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.service.osgi;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
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

public class OsgiBundleScanningSchemaService extends ScanningSchemaServiceProvider
        implements ServiceTrackerCustomizer<SchemaContextListener, SchemaContextListener> {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiBundleScanningSchemaService.class);
    private static final AtomicReference<OsgiBundleScanningSchemaService> GLOBAL_INSTANCE = new AtomicReference<>();
    private static final long FRAMEWORK_BUNDLE_ID = 0;

    private final BundleScanner scanner = new BundleScanner();
    private final BundleContext context;

    private ServiceTracker<SchemaContextListener, SchemaContextListener> listenerTracker;
    private BundleTracker<Iterable<Registration>> bundleTracker;
    private boolean starting = true;

    private volatile boolean stopping;

    private OsgiBundleScanningSchemaService(final BundleContext context) {
        this.context = Preconditions.checkNotNull(context);
    }

    public static @Nonnull OsgiBundleScanningSchemaService createInstance(final BundleContext ctx) {
        final OsgiBundleScanningSchemaService instance = new OsgiBundleScanningSchemaService(ctx);
        Preconditions.checkState(GLOBAL_INSTANCE.compareAndSet(null, instance));
        instance.start();
        return instance;
    }

    private void start() {
        checkState(context != null);
        LOG.debug("start() starting");

        listenerTracker = new ServiceTracker<>(context, SchemaContextListener.class, this);
        bundleTracker = new BundleTracker<>(context,
                Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING | Bundle.ACTIVE, scanner);

        bundleTracker.open();

        LOG.debug("BundleTracker.open() complete");

        if (!isEmpty()) {
            tryToUpdateSchemaContext();
        }

        listenerTracker.open();
        starting = false;

        LOG.debug("start() complete");
    }

    public static OsgiBundleScanningSchemaService getInstance() {
        final OsgiBundleScanningSchemaService instance = GLOBAL_INSTANCE.get();
        Preconditions.checkState(instance != null, "Global Instance was not instantiated");
        return instance;
    }

    @VisibleForTesting
    public static void destroyInstance() throws Exception {
        final OsgiBundleScanningSchemaService instance = GLOBAL_INSTANCE.getAndSet(null);
        if (instance != null) {

            instance.closeInstance();
        }
    }

    private void closeInstance() {
        stopping = true;
        if (bundleTracker != null) {
            bundleTracker.close();
            bundleTracker = null;
        }
        if (listenerTracker != null) {
            listenerTracker.close();
            listenerTracker = null;
        }
        close();
    }

    public BundleContext getContext() {
        return context;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @VisibleForTesting
    void notify(final SchemaContext snapshot) {
        notifyListeners(snapshot);

        final Object[] services = listenerTracker.getServices();
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

            final List<URL> urls = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                final URL u = enumeration.nextElement();
                try {
                    urls.add(u);
                    LOG.debug("Registered {}", u);
                } catch (final Exception e) {
                    LOG.warn("Failed to register {}, ignoring it", e);
                }
            }

            final List<Registration> registrations = registerAvalaibleYangs(urls);
            if (!registrations.isEmpty()) {
                LOG.debug("Loaded {} new URLs from bundle {}, attempting to rebuild schema context",
                        registrations.size(), bundle.getSymbolicName());
                if (!starting && !stopping) {
                    tryToUpdateSchemaContext();
                }
            }
            return ImmutableList.copyOf(registrations);
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
         * If removing YANG files makes yang store inconsistent, method {@link #getYangStoreSnapshot()} will
         * throw exception. There is no rollback.
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
                    LOG.debug("removedBundle: {}, state: {}, # urls: {}", bundle.getSymbolicName(), bundle.getState(),
                            numUrls);
                }
                if (!starting && !stopping) {
                    tryToUpdateSchemaContext();
                }
            }
        }
    }

    @Override
    public SchemaContextListener addingService(final ServiceReference<SchemaContextListener> reference) {
        final SchemaContextListener listener = context.getService(reference);
        registerSchemaContextListener(listener);
        return listener;
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
        removeListener(service);
    }
}
