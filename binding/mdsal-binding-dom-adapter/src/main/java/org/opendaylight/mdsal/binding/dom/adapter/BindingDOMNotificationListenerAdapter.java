/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.NotificationListenerInvoker;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

class BindingDOMNotificationListenerAdapter implements DOMNotificationListener {

    private final AdapterContext adapterContext;
    private final NotificationListener delegate;
    private final ImmutableMap<Absolute, NotificationListenerInvoker> invokers;

    BindingDOMNotificationListenerAdapter(final AdapterContext adapterContext, final NotificationListener delegate) {
        this.adapterContext = requireNonNull(adapterContext);
        this.delegate = requireNonNull(delegate);
        this.invokers = createInvokerMapFor(delegate.getClass());
    }

    @Override
    public void onNotification(final DOMNotification notification) {
        final Notification baNotification = deserialize(notification);
        final QName notificationQName = notification.getType().lastNodeIdentifier();
        getInvoker(notification.getType()).invokeNotification(delegate, notificationQName, baNotification);
    }

    private Notification deserialize(final DOMNotification notification) {
        if (notification instanceof LazySerializedDOMNotification) {
            // TODO: This is a routed-back notification, for which we may end up losing event time here, but that is
            //       okay, for now at least.
            return ((LazySerializedDOMNotification) notification).getBindingData();
        }

        final CurrentAdapterSerializer serializer = adapterContext.currentSerializer();
        // FIXME: do not instantiana a SchemaPath
        final SchemaPath notifPath = notification.getType().asSchemaPath();
        return notification instanceof DOMEvent ? serializer.fromNormalizedNodeNotification(notifPath,
            notification.getBody(), ((DOMEvent) notification).getEventInstant())
                : serializer.fromNormalizedNodeNotification(notifPath, notification.getBody());
    }

    private NotificationListenerInvoker getInvoker(final Absolute type) {
        return invokers.get(type);
    }

    protected Set<Absolute> getSupportedNotifications() {
        return invokers.keySet();
    }

    private static ImmutableMap<Absolute, NotificationListenerInvoker> createInvokerMapFor(
            final Class<? extends NotificationListener> implClz) {
        final Map<Absolute, NotificationListenerInvoker> builder = new HashMap<>();
        for (final TypeToken<?> ifaceToken : TypeToken.of(implClz).getTypes().interfaces()) {
            Class<?> iface = ifaceToken.getRawType();
            if (NotificationListener.class.isAssignableFrom(iface) && BindingReflections.isBindingClass(iface)) {
                @SuppressWarnings("unchecked")
                final Class<? extends NotificationListener> listenerType
                        = (Class<? extends NotificationListener>) iface;
                final NotificationListenerInvoker invoker = NotificationListenerInvoker.from(listenerType);
                for (final Absolute path : getNotificationTypes(listenerType)) {
                    builder.put(path, invoker);
                }
            }
        }
        return ImmutableMap.copyOf(builder);
    }

    private static Set<Absolute> getNotificationTypes(final Class<? extends NotificationListener> type) {
        // TODO: Investigate possibility and performance impact if we cache this or expose
        // it from NotificationListenerInvoker
        final Set<Absolute> ret = new HashSet<>();
        for (final Method method : type.getMethods()) {
            if (BindingReflections.isNotificationCallback(method)) {
                final Class<?> notification = method.getParameterTypes()[0];
                ret.add(Absolute.of(BindingReflections.findQName(notification)));
            }
        }
        return ret;
    }
}
