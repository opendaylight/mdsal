/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.Dictionary;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A Factory Component which implements {@link EffectiveModelContextListener}. Instances of this component are created
 * through by {@link OSGiDOMSchemaService} each time a listener is registered.
 */
@Component(factory = EffectiveModelContextImpl.FACTORY_NAME, service = EffectiveModelContextListener.class)
public final class EffectiveModelContextImpl implements EffectiveModelContextListener {
    static final String FACTORY_NAME = "org.opendaylight.mdsal.dom.schema.osgi.impl.SchemaSchemaContextListener";

    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.dom.schema.osgi.SchemaSchemaContextListener";

    private EffectiveModelContextListener delegate = null;

    @Override
    public void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        delegate.onModelContextUpdated(newModelContext);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (EffectiveModelContextListener) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    static Dictionary<String, ?> props(final EffectiveModelContextListener delegate) {
        return FrameworkUtil.asDictionary(Map.of(DELEGATE, delegate));
    }
}
