/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension.DemandListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@VisibleForTesting
public final class BindingDOMNotificationPublishServiceAdapter
        extends AbstractBindingAdapter<DOMNotificationPublishService> implements NotificationPublishService {
    static final Factory<NotificationPublishService> BUILDER_FACTORY = Builder::new;

    private final OnDemandSupport onDemandSupport;

    public BindingDOMNotificationPublishServiceAdapter(final AdapterContext adapterContext,
            final DOMNotificationPublishService domPublishService) {
        super(adapterContext, domPublishService);

        final var demandExt = domPublishService.extension(DOMNotificationPublishDemandExtension.class);
        onDemandSupport = demandExt == null ? null : new OnDemandSupport(demandExt);

    }

    @Override
    public void putNotification(final Notification<?> notification) throws InterruptedException {
        getDelegate().putNotification(toDomNotification(notification));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification) {
        return toBindingResult(getDelegate().offerNotification(toDomNotification(notification)));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification, final int timeout,
            final TimeUnit unit) throws InterruptedException {
        return toBindingResult(getDelegate().offerNotification(toDomNotification(notification), timeout, unit));
    }

    @Override
    @NonNullByDefault
    public <N extends Notification<N> & DataObject> Registration registerDemandMonitor(final Class<N> type,
            final DemandMonitor monitor) {
        return onDemandSupport == null ? NotificationPublishService.super.registerDemandMonitor(type, monitor)
            : onDemandSupport.registerDemandMonitor(requireNonNull(type), requireNonNull(monitor));
    }


    private @NonNull DOMNotification toDomNotification(final Notification<?> notification) {
        return new LazySerializedNotification(currentSerializer(), notification);
    }

    private static @NonNull ListenableFuture<? extends Object> toBindingResult(
            final @NonNull ListenableFuture<? extends Object> domResult) {
        return DOMNotificationPublishService.REJECTED.equals(domResult) ? NotificationPublishService.REJECTED
            : domResult;
    }

    private static final class Builder extends BindingDOMAdapterBuilder<NotificationPublishService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<Class<? extends DOMService<?, ?>>> getRequiredDelegates() {
            return ImmutableSet.of(DOMNotificationPublishService.class);
        }

        @Override
        protected NotificationPublishService createInstance(final ClassToInstanceMap<DOMService<?, ?>> delegates) {
            return new BindingDOMNotificationPublishServiceAdapter(adapterContext(),
                delegates.getInstance(DOMNotificationPublishService.class));
        }
    }

    @NonNullByDefault
    private static final class OnDemandSupport {
        private final DOMNotificationPublishDemandExtension demandExt;

        OnDemandSupport(final DOMNotificationPublishDemandExtension demandExt) {
            this.demandExt = requireNonNull(demandExt);
        }

        <N extends Notification<N> & DataObject> Registration registerDemandMonitor(final Class<N> type,
                final DemandMonitor monitor) {
            // FIXME: do not use BindingReflections here
            final var qname = BindingReflections.findQName(type);
            if (qname == null) {
                throw new IllegalArgumentException("Cannot find QName of " + type);
            }
            return new MonitorDemandListener(qname, monitor, demandExt);
        }
    }

    private static final class MonitorDemandListener extends AbstractRegistration implements DemandListener {
        private final Absolute type;
        private final DemandMonitor monitor;
        private final Registration domReg;

        private Registration monitorReg;

        @NonNullByDefault
        MonitorDemandListener(final QName type, final DemandMonitor monitor,
                final DOMNotificationPublishDemandExtension demandExt) {
            this.type = Absolute.of(type);
            this.monitor = requireNonNull(monitor);
            domReg = demandExt.registerDemandListener(this);
        }

        @Override
        public synchronized void onDemandUpdated(final ImmutableSet<Absolute> neededTypes) {
            if (neededTypes.contains(type)) {
                becomeActive();
            } else {
                becomeInactive();
            }
        }

        @Override
        protected synchronized void removeRegistration() {
            try {
                becomeInactive();
            } finally {
                domReg.close();
            }
        }

        private void becomeActive() {
            if (monitorReg == null) {
                monitorReg = monitor.demandEncountered();
            }
        }

        private void becomeInactive() {
            final var local = monitorReg;
            if (local != null) {
                monitorReg = null;
                local.close();
            }
        }
    }
}
