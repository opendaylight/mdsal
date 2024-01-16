/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiModuleInfoSnapshot;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Service Registry-backed implementation of {@link DOMSchemaService}.
 */
@Component(immediate = true)
public final class OSGiDOMSchemaService implements DOMSchemaService, DOMSchemaService.YangTextSourceExtension {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiDOMSchemaService.class);

    private final List<ModelContextListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<ModuleInfoSnapshot> currentSnapshot = new AtomicReference<>();
    private final ComponentFactory<ModelContextListener> listenerFactory;

    private boolean deactivated;

    @Activate
    public OSGiDOMSchemaService(
            @Reference(target = "(component.factory=" + ModelContextListener.FACTORY_NAME + ")")
            final ComponentFactory<ModelContextListener> listenerFactory) {
        this.listenerFactory = requireNonNull(listenerFactory);
        LOG.info("DOM Schema services activated");
    }

    @Override
    public List<Extension> supportedExtensions() {
        return List.of(this);
    }

    @Deactivate
    void deactivate() {
        LOG.info("DOM Schema services deactivated");
        deactivated = true;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    void bindSnapshot(final OSGiModuleInfoSnapshot newContext) {
        LOG.info("Updating context to generation {}", newContext.getGeneration());
        final var snapshot = newContext.getService();
        final var modelContext = snapshot.modelContext();
        final var previous = currentSnapshot.getAndSet(snapshot);
        LOG.debug("Snapshot updated from {} to {}", previous, snapshot);

        listeners.forEach(listener -> notifyListener(modelContext, listener));
    }

    void unbindSnapshot(final OSGiModuleInfoSnapshot oldContext) {
        final var snapshot = oldContext.getService();
        if (currentSnapshot.compareAndSet(snapshot, null) && !deactivated) {
            LOG.info("Lost final generation {}", oldContext.getGeneration());
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    void addListener(final ModelContextListener listener) {
        LOG.trace("Adding listener {}", listener);
        listeners.add(listener);
        listener.onModelContextUpdated(getGlobalContext());
    }

    void removeListener(final ModelContextListener listener) {
        LOG.trace("Removing listener {}", listener);
        listeners.remove(listener);
    }

    @Override
    public @NonNull EffectiveModelContext getGlobalContext() {
        return currentSnapshot.get().modelContext();
    }

    @Override
    public Registration registerSchemaContextListener(final Consumer<EffectiveModelContext> listener) {
        final var reg = listenerFactory.newInstance(ModelContextListener.props(listener));
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                reg.dispose();
            }
        };
    }

    @Override
    public ListenableFuture<YangTextSource> getYangTexttSource(final SourceIdentifier sourceId) {
        try {
            return Futures.immediateFuture(currentSnapshot.get().getYangTextSource(sourceId));
        } catch (MissingSchemaSourceException e) {
            return Futures.immediateFailedFuture(e);
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static void notifyListener(final @NonNull EffectiveModelContext modelContext,
            final ModelContextListener listener) {
        try {
            listener.onModelContextUpdated(modelContext);
        } catch (RuntimeException e) {
            LOG.warn("Failed to notify listener {}", listener, e);
        }
    }
}
