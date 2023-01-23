/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.IdentityHashMap;
import java.util.Map;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiModuleInfoSnapshot;
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

@Beta
@Component(immediate = true)
public final class OSGiBindingRuntime {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingRuntime.class);

    @GuardedBy("this")
    private final Map<OSGiModuleInfoSnapshot, ComponentInstance<OSGiBindingRuntimeContextImpl>> instances =
        new IdentityHashMap<>();
    private final ComponentFactory<OSGiBindingRuntimeContextImpl> contextFactory;
    private final BindingRuntimeGenerator generator;

    @Activate
    public OSGiBindingRuntime(@Reference final BindingRuntimeGenerator generator,
            @Reference(target = "(component.factory=" + OSGiBindingRuntimeContextImpl.FACTORY_NAME + ")")
            final ComponentFactory<OSGiBindingRuntimeContextImpl> contextFactory) {
        this.generator = requireNonNull(generator);
        this.contextFactory = requireNonNull(contextFactory);
        LOG.info("Binding Runtime activated");
    }

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY)
    synchronized void addModuleInfoSnapshot(final OSGiModuleInfoSnapshot snapshot) {
        final var service = snapshot.getService();
        final var types = generator.generateTypeMapping(service.getEffectiveModelContext());

        instances.put(snapshot, contextFactory.newInstance(OSGiBindingRuntimeContextImpl.props(
            snapshot.getGeneration(), snapshot.getServiceRanking(),
            new DefaultBindingRuntimeContext(types, service))));
        LOG.info("Binding Runtime updated");
    }

    synchronized void removeModuleInfoSnapshot(final OSGiModuleInfoSnapshot snapshot) {
        final var instance = instances.remove(snapshot);
        if (instance != null) {
            instance.dispose();
        } else {
            LOG.warn("Instance for generation {} not found", snapshot.getGeneration());
        }
    }

    @Deactivate
    synchronized void deactivate() {
        LOG.info("Binding Runtime deactivating");
        instances.values().forEach(ComponentInstance::dispose);
        LOG.info("Binding Runtime deactivated");
    }
}
