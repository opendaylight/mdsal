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
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
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

    @Reference(target = "(component.factory=" + EffectiveModelContextImpl.FACTORY_NAME + ")")
    ComponentFactory<EffectiveModelContextImpl> listenerFactory = null;

    private final List<EffectiveModelContextListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<ModuleInfoSnapshot> currentSnapshot = new AtomicReference<>();

    private boolean deactivated;

    @Override
    public EffectiveModelContext getGlobalContext() {
        return currentSnapshot.get().getEffectiveModelContext();
    }

    @Override
    public ListenerRegistration<EffectiveModelContextListener> registerSchemaContextListener(
            final EffectiveModelContextListener listener) {
        return registerListener(requireNonNull(listener));
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return currentSnapshot.get().getSource(sourceIdentifier);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    void bindSnapshot(final OSGiModuleInfoSnapshot newContext) {
        LOG.info("Updating context to generation {}", newContext.getGeneration());
        final ModuleInfoSnapshot snapshot = newContext.getService();
        final EffectiveModelContext ctx = snapshot.getEffectiveModelContext();
        final ModuleInfoSnapshot previous = currentSnapshot.getAndSet(snapshot);
        LOG.debug("Snapshot updated from {} to {}", previous, snapshot);

        listeners.forEach(listener -> notifyListener(ctx, listener));
    }

    void unbindSnapshot(final OSGiModuleInfoSnapshot oldContext) {
        final ModuleInfoSnapshot snapshot = oldContext.getService();
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

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("DOM Schema services activated");
    }

    @Deactivate
    void deactivate() {
        LOG.info("DOM Schema services deactivated");
        deactivated = true;
    }

    private @NonNull ListenerRegistration<EffectiveModelContextListener> registerListener(
            final @NonNull EffectiveModelContextListener listener) {
        final ComponentInstance<EffectiveModelContextImpl> reg =
            listenerFactory.newInstance(EffectiveModelContextImpl.props(listener));
        return new ListenerRegistration<>() {
            @Override
            public EffectiveModelContextListener getInstance() {
                return listener;
            }

            @Override
            public void close() {
                reg.dispose();
            }
        };
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static void notifyListener(final EffectiveModelContext context,
            final EffectiveModelContextListener listener) {
        try {
            listener.onModelContextUpdated(context);
        } catch (RuntimeException e) {
            LOG.warn("Failed to notify listener {}", listener, e);
        }
    }
}
