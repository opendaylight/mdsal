/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Legacy base class for notification brokers.
 *
 * @deprecated Use {@code org.opendaylight.mdsal.binding.testkit.TestKit} instead.
 */
@Deprecated
public class AbstractNotificationBrokerTest extends AbstractSchemaAwareTest {
    private BindingToNormalizedNodeCodec bindingToNormalizedNodeCodec;
    private DOMNotificationRouter domNotificationRouter;
    private NotificationService notificationService;
    private NotificationPublishService notificationPublishService;


    @Override
    protected void setupWithSchema(final SchemaContext context) {
        final DataBrokerTestCustomizer testCustomizer = createDataBrokerTestCustomizer();
        domNotificationRouter = testCustomizer.getDomNotificationRouter();
        notificationService = testCustomizer.createNotificationService();
        notificationPublishService = testCustomizer.createNotificationPublishService();
        bindingToNormalizedNodeCodec = testCustomizer.getBindingToNormalized();
        testCustomizer.updateSchema(context);
    }

    protected DataBrokerTestCustomizer createDataBrokerTestCustomizer() {
        return new DataBrokerTestCustomizer();
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public NotificationPublishService getNotificationPublishService() {
        return notificationPublishService;
    }

    public DOMNotificationRouter getDomNotificationRouter() {
        return domNotificationRouter;
    }

    public BindingToNormalizedNodeCodec getBindingToNormalizedNodeCodec() {
        return bindingToNormalizedNodeCodec;
    }
}
