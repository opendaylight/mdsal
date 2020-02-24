/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.binding.runtime.api.AbstractBindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Factory Component which implements {@link BindingRuntimeContext}. Instances of this component are created through
 * by {@link YangModuleInfoRegistry} each time it arrives at a new EffectiveModuleContext (and/or set of ClassLoaders).
 */
@Component(factory = BindingRuntimeContextImpl.FACTORY_NAME, service = BindingRuntimeContext.class)
public final class BindingRuntimeContextImpl extends AbstractBindingRuntimeContext {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.runtime.osgi.impl.BindingRuntimeContextFactory";

    // Keys to for activation properties
    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.binding.runtime.osgi.BindingRuntimeContextDelegate";
    @VisibleForTesting
    static final String GENERATION = "org.opendaylight.mdsal.binding.runtime.osgi.Generation";

    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeContextImpl.class);

    private BindingRuntimeContext delegate = null;
    private Long generation;

    @Override
    public ClassLoadingStrategy getStrategy() {
        return delegate.getStrategy();
    }

    @Override
    public BindingRuntimeTypes getTypes() {
        return delegate.getTypes();
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (BindingRuntimeContext) verifyNotNull(properties.get(DELEGATE));
        generation = (Long) verifyNotNull(properties.get(GENERATION));
        LOG.debug("BindingRuntimeContext generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.debug("BindingRuntimeContext generation {} deactivated", generation);
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(final long generation, final BindingRuntimeContext delegate) {
        final Dictionary<String, Object> ret = new Hashtable<>(4);
        ret.put(Constants.SERVICE_RANKING, ranking(generation));
        ret.put(DELEGATE, delegate);
        ret.put(GENERATION, generation);
        return ret;
    }

    private static Integer ranking(final long generation) {
        return generation >= 0 && generation <= Integer.MAX_VALUE ? (int) generation : Integer.MAX_VALUE;
    }
}
