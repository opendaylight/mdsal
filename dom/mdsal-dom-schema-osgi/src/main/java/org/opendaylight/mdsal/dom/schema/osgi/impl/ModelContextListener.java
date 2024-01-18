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
import com.google.common.collect.ForwardingObject;
import java.util.Dictionary;
import java.util.Map;
import java.util.function.Consumer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Factory Component for OSGi SR manifestation of a {@code Consumer<EffectiveModelContext>}. Instances of this
 * component are created through by {@link OSGiDOMSchemaService} each time a listener is registered.
 */
@Component(factory = ModelContextListener.FACTORY_NAME, service = ModelContextListener.class)
public final class ModelContextListener extends ForwardingObject {
    static final String FACTORY_NAME = "org.opendaylight.mdsal.dom.schema.osgi.impl.SchemaSchemaContextListener";

    private static final Logger LOG = LoggerFactory.getLogger(ModelContextListener.class);

    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.dom.schema.osgi.SchemaSchemaContextListener";

    private Consumer<EffectiveModelContext> delegate = null;

    @Activate
    public ModelContextListener(final Map<String, ?> properties) {
        delegate = (Consumer<EffectiveModelContext>) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @Override
    protected Object delegate() {
        return delegate;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    void onModelContextUpdated(final EffectiveModelContext modelContext) {
        LOG.trace("Notifying {} of {}", delegate, modelContext);
        try {
            delegate.accept(modelContext);
        } catch (RuntimeException e) {
            LOG.warn("Failed to notify listener {}", delegate, e);
        }
    }

    static Dictionary<String, ?> props(final Consumer<EffectiveModelContext> delegate) {
        return FrameworkUtil.asDictionary(Map.of(DELEGATE, delegate));
    }
}
