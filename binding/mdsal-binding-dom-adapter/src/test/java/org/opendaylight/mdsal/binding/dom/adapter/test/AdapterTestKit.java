/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.testkit.spi.AbstractTestKit;
import org.opendaylight.mdsal.dom.testkit.DOMTestKit;

final class AdapterTestKit extends AbstractTestKit<BindingDOMDataBrokerAdapter> {
    private final BindingToNormalizedNodeCodec adapterCodec;
    private NotificationPublishService notifPublish;
    private NotificationService notif;

    AdapterTestKit() {
        adapterCodec = new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
            codecRegistry());
        adapterCodec.onGlobalContextUpdated(domTestKit().effectiveModelContext());
    }

    AdapterTestKit(final DOMTestKit domTestKit) {
        super(domTestKit);
        adapterCodec = new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
            codecRegistry());
        adapterCodec.onGlobalContextUpdated(domTestKit().effectiveModelContext());
    }

    @Override
    public BindingNormalizedNodeSerializer nodeSerializer() {
        return adapterCodec;
    }

    @Override
    public void close() {
        super.close();
        adapterCodec.close();
    }

    @Override
    protected @NonNull BindingDOMDataBrokerAdapter createDataBroker() {
        return new BindingDOMDataBrokerAdapter(domTestKit().domDataBroker(), adapterCodec);
    }

    @Override
    protected void closeDataBroker(final BindingDOMDataBrokerAdapter dataBroker) {
        // No-op
    }

    @Override
    public NotificationService notificationService() {
        if (notif == null) {
             notif = new BindingDOMNotificationServiceAdapter(domTestKit().domNotificationService(), codecRegistry());
        }
        return notif;
    }

    @Override
    public NotificationPublishService notificationPublishService() {
        if (notifPublish == null) {
            notifPublish =  new BindingDOMNotificationPublishServiceAdapter(
                domTestKit().domNotificationPublishService(), adapterCodec);

        }
        // TODO Auto-generated method stub
        return notifPublish;
    }
}
