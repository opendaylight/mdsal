/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testkit;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
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

/**
 * Base class for tests running with models packaged as Binding artifacts on their classpath and need to interact with
 * {@link BindingService}s. Services are lazily instantiated on-demand.
 *
 * @author Robert Varga
 */
@Beta
public class TestKit extends AbstractTestKit<BindingDOMDataBrokerAdapter> {
    private volatile BindingToNormalizedNodeCodec adapterCodec;
    private volatile NotificationPublishService notifPublish;
    private volatile NotificationService notif;

    public TestKit() {
        this(new DOMTestKit());
    }

    public TestKit(final DOMTestKit domTestKit) {
        super(domTestKit);
    }

    public TestKit(final DOMTestKit domTestKit, final ListenerClassifier classifier) {
        super(domTestKit, classifier);
    }

    @Override
    public final BindingNormalizedNodeSerializer nodeSerializer() {
        return adapterCodec();
    }

    @Override
    public NotificationService getNotificationService() {
        NotificationService local = notif;
        if (local == null) {
            synchronized (this) {
                local = notif;
                if (local == null) {
                    notif = local = new BindingDOMNotificationServiceAdapter(domTestKit().domNotificationService(),
                        codecRegistry());
                }
            }
        }
        return local;
    }

    @Override
    public NotificationPublishService getNotificationPublishService() {
        NotificationPublishService local = notifPublish;
        if (local == null) {
            synchronized (this) {
                local = notifPublish;
                if (local == null) {
                    notifPublish = local = new BindingDOMNotificationPublishServiceAdapter(
                        domTestKit().domNotificationPublishService(), adapterCodec());
                }
            }
        }
        return local;
    }

    @Override
    public void close() {
        super.close();
        if (adapterCodec != null) {
            adapterCodec.close();
            adapterCodec  = null;
        }
    }

    @Override
    protected BindingDOMDataBrokerAdapter createDataBroker() {
        return new BindingDOMDataBrokerAdapter(domTestKit().domDataBroker(), adapterCodec());
    }

    @Override
    protected void closeDataBroker(final BindingDOMDataBrokerAdapter dataBroker) {
        // No-op
    }

    private @NonNull BindingToNormalizedNodeCodec adapterCodec() {
        BindingToNormalizedNodeCodec local = adapterCodec;
        if (local == null) {
            synchronized (this) {
                local = adapterCodec;
                if (local == null) {
                    local = new BindingToNormalizedNodeCodec(
                        GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), codecRegistry());
                    local.onGlobalContextUpdated(domTestKit().effectiveModelContext());
                    adapterCodec = local;
                }
            }
        }
        return local;
    }

}
