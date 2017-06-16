/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.serializer.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedDOMNotification;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Notification listener adapter.
 */
@Beta
public class BindingDOMNotificationListenerAdapter implements DOMNotificationListener {

    private final BindingNormalizedNodeSerializer codec;
    private final NotificationListener delegate;
    private final Map<SchemaPath, NotificationListenerInvoker> invokers;

    public BindingDOMNotificationListenerAdapter(final BindingNormalizedNodeSerializer codec,
            final NotificationListener delegate) {
        this.codec = codec;
        this.delegate = delegate;
        this.invokers = createInvokerMapFor(delegate.getClass());
    }

    @Override
    public void onNotification(@Nonnull final DOMNotification notification) {
        final Notification<?> baNotification = deserialize(notification);
        final QName notificationQName = notification.getType().getLastComponent();
        getInvoker(notification.getType()).invokeNotification(delegate, notificationQName,
                (Instantiable<?>) baNotification);
    }

    private Notification<?> deserialize(final DOMNotification notification) {
        if (notification instanceof LazySerializedDOMNotification) {
            return ((LazySerializedDOMNotification) notification).getBindingData();
        }
        return codec.fromNormalizedNodeNotification(notification.getType(), notification.getBody());
    }

    private NotificationListenerInvoker getInvoker(final SchemaPath type) {
        return invokers.get(type);
    }

    /**
     * Get supported notifications.
     *
     * @return supported notifications
     */
    public Set<SchemaPath> getSupportedNotifications() {
        return invokers.keySet();
    }

    /**
     * Create map of all notification listeners.
     *
     * @param implClz
     *            - class type of {@link NotificationListener}
     * @return map of notification listeners
     */
    private static Map<SchemaPath, NotificationListenerInvoker>
            createInvokerMapFor(final Class<? extends NotificationListener> implClz) {
        final Map<SchemaPath, NotificationListenerInvoker> builder = new HashMap<>();
        for (final TypeToken<?> ifaceToken : TypeToken.of(implClz).getTypes().interfaces()) {
            final Class<?> iface = ifaceToken.getRawType();
            if (NotificationListener.class.isAssignableFrom(iface) && BindingReflections.isBindingClass(iface)) {
                @SuppressWarnings("unchecked")
                final Class<? extends NotificationListener> listenerType =
                        (Class<? extends NotificationListener>) iface;
                final NotificationListenerInvoker invoker = NotificationListenerInvoker.from(listenerType);
                for (final SchemaPath path : getNotificationTypes(listenerType)) {
                    builder.put(path, invoker);
                }
            }
        }
        return ImmutableMap.copyOf(builder);
    }

    private static Set<SchemaPath> getNotificationTypes(final Class<? extends NotificationListener> type) {
        // TODO: Investigate possibility and performance impact if we cache this or expose
        // it from NotificationListenerInvoker
        final Set<SchemaPath> ret = new HashSet<>();
        for (final Method method : type.getMethods()) {
            if (BindingReflections.isNotificationCallback(method)) {
                final Class<?> notification = method.getParameterTypes()[0];
                final QName name = BindingReflections.findQName(notification);
                ret.add(SchemaPath.create(true, name));
            }
        }
        return ret;
    }
}