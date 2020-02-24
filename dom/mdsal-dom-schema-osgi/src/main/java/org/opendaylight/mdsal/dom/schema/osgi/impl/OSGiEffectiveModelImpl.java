/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(factory = OSGiEffectiveModelImpl.FACTORY_NAME,
           service = { OSGiModuleInfoSnapshot.class, ModuleInfoSnapshot.class })
public final class OSGiEffectiveModelImpl implements OSGiModuleInfoSnapshot {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.dom.schema.osgi.impl.OSGiEffectiveModelImpl";

    // Keys to for activation properties
    @VisibleForTesting
    static final String GENERATION = "org.opendaylight.mdsal.dom.schema.osgi.impl.Generation";
    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.dom.schema.osgi.impl.ModuleInfoSnapshot";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiEffectiveModelImpl.class);

    private ModuleInfoSnapshot delegate;
    private long generation;

    @Override
    public long getGeneration() {
        return generation;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return delegate.getEffectiveModelContext();
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return delegate.getSource(sourceIdentifier);
    }

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        return delegate.loadClass(fullyQualifiedName);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        generation = (Long) verifyNotNull(properties.get(GENERATION));
        delegate = (ModuleInfoSnapshot) verifyNotNull(properties.get(DELEGATE));
        LOG.debug("ClassLoadingEffectiveModelContext generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.debug("ClassLoadingEffectiveModelContext generation {} deactivated", generation);
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(final long generation, final ModuleInfoSnapshot delegate) {
        final Dictionary<String, Object> ret = new Hashtable<>(4);
        ret.put(Constants.SERVICE_RANKING, ranking(generation));
        ret.put(GENERATION, generation);
        ret.put(DELEGATE, requireNonNull(delegate));
        return ret;
    }

    private static Integer ranking(final long generation) {
        return generation >= 0 && generation <= Integer.MAX_VALUE ? (int) generation : Integer.MAX_VALUE;
    }
}
