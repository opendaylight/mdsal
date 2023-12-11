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
import java.util.function.Consumer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A Factory Component for OSGi SR manifestation of a {@code Consumer<EffectiveModelContext>}. Instances of this
 * component are created through by {@link OSGiDOMSchemaService} each time a listener is registered.
 */
@Component(factory = ModelContextListener.FACTORY_NAME, service = ModelContextListener.class)
public final class ModelContextListener {
    static final String FACTORY_NAME = "org.opendaylight.mdsal.dom.schema.osgi.impl.SchemaSchemaContextListener";

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

    void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        delegate.accept(newModelContext);
    }

    static Dictionary<String, ?> props(final Consumer<EffectiveModelContext> delegate) {
        return FrameworkUtil.asDictionary(Map.of(DELEGATE, delegate));
    }
}
