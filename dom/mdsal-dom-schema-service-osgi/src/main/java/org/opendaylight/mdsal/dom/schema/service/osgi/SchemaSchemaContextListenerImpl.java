/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.service.osgi;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A Factory Component which implements {@link SchemaContextListener}. Instances of this component are created through
 * by {@link OSGiDOMSchemaService} each time a listener is registered.
 */
@Component(factory = SchemaSchemaContextListenerImpl.FACTORY_NAME, service = SchemaContextListener.class)
public final class SchemaSchemaContextListenerImpl implements SchemaContextListener {
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.runtime.osgi.impl.SchemaSchemaContextListener";

    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.binding.runtime.osgi.SchemaSchemaContextListener";

    private SchemaContextListener delegate = null;

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        delegate.onGlobalContextUpdated(context);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        delegate = (SchemaContextListener) verifyNotNull(properties.get(DELEGATE));
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(final SchemaContextListener delegate) {
        final Dictionary<String, Object> ret = new Hashtable<>(2);
        ret.put(DELEGATE, delegate);
        return ret;
    }
}
