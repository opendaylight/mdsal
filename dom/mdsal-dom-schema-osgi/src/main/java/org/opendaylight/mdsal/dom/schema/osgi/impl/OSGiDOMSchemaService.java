/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiModuleInfoSnapshot;
import org.opendaylight.mdsal.dom.spi.AbstractDOMSchemaService;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
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
@Component(service = DOMSchemaService.class, immediate = true)
public final class OSGiDOMSchemaService extends AbstractDOMSchemaService.WithYangTextSources {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiDOMSchemaService.class);


    private final List<EffectiveModelContextListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<ModuleInfoSnapshot> currentSnapshot = new AtomicReference<>();
    private final ComponentFactory<EffectiveModelContextImpl> listenerFactory;

    private boolean deactivated;

    @Activate
    public OSGiDOMSchemaService(
            @Reference(target = "(component.factory=" + EffectiveModelContextImpl.FACTORY_NAME + ")")
            final ComponentFactory<EffectiveModelContextImpl> listenerFactory) {
        this.listenerFactory = requireNonNull(listenerFactory);
        LOG.info("DOM Schema services activated");
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
        final var modelContext = snapshot.getEffectiveModelContext();
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
    void addListener(final EffectiveModelContextListener listener) {
        LOG.trace("Adding listener {}", listener);
        listeners.add(listener);
        listener.onModelContextUpdated(getGlobalContext());
    }

    void removeListener(final EffectiveModelContextListener listener) {
        LOG.trace("Removing listener {}", listener);
        listeners.remove(listener);
    }

    @Override
    public @NonNull EffectiveModelContext getGlobalContext() {
        return currentSnapshot.get().getEffectiveModelContext();
    }

    @Override
    public Registration registerSchemaContextListener(final EffectiveModelContextListener listener) {
        final var reg = listenerFactory.newInstance(EffectiveModelContextImpl.props(listener));
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                reg.dispose();
            }
        };
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return currentSnapshot.get().getSource(sourceIdentifier);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static void notifyListener(final @NonNull EffectiveModelContext modelContext,
            final EffectiveModelContextListener listener) {
        try {
            listener.onModelContextUpdated(modelContext);
        } catch (RuntimeException e) {
            LOG.warn("Failed to notify listener {}", listener, e);
        }
    }
}
