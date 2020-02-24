/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiEffectiveModel;
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
@Component(factory = OSGiEffectiveModelImpl.FACTORY_NAME, service = OSGiEffectiveModel.class)
public final class OSGiEffectiveModelImpl implements OSGiEffectiveModel {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.dom.schema.osgi.impl.BindingRuntimeContextFactory";

    // Keys to for activation properties
    @VisibleForTesting
    static final String MODEL_CONTEXT = "org.opendaylight.mdsal.dom.schema.osgi.impl.EffectiveModelContext";
    @VisibleForTesting
    static final String GENERATION = "org.opendaylight.mdsal.dom.schema.osgi.impl.Generation";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiEffectiveModelImpl.class);

    private EffectiveModelContext modelContext;
    private Long generation;


    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return verifyNotNull(modelContext);
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        modelContext = (EffectiveModelContext) verifyNotNull(properties.get(MODEL_CONTEXT));
        generation = (Long) verifyNotNull(properties.get(GENERATION));
        LOG.debug("ClassLoadingEffectiveModelContext generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        modelContext = null;
        LOG.debug("ClassLoadingEffectiveModelContext generation {} deactivated", generation);
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(final long generation, final EffectiveModelContext modelContext) {
        final Dictionary<String, Object> ret = new Hashtable<>(4);
        ret.put(Constants.SERVICE_RANKING, ranking(generation));
        ret.put(MODEL_CONTEXT, modelContext);
        ret.put(GENERATION, generation);
        return ret;
    }

    private static Integer ranking(final long generation) {
        return generation >= 0 && generation <= Integer.MAX_VALUE ? (int) generation : Integer.MAX_VALUE;
    }
}
