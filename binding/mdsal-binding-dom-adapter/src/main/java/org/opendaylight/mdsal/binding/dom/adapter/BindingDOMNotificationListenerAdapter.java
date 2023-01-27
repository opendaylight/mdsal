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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.NotificationListenerInvoker;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class BindingDOMNotificationListenerAdapter extends AbstractDOMNotificationListenerAdapter {
    private final ImmutableMap<Absolute, NotificationListenerInvoker> invokers;
    private final @NonNull NotificationListener delegate;

    BindingDOMNotificationListenerAdapter(final AdapterContext adapterContext, final NotificationListener delegate) {
        super(adapterContext);
        this.delegate = requireNonNull(delegate);
        invokers = createInvokerMapFor(delegate.getClass());
    }

    @Override
    void onNotification(final Absolute domType, final Notification<?> notification) {
        invokers.get(domType).invokeNotification(delegate, domType.lastNodeIdentifier(), notification);
    }

    @Override
    Set<Absolute> getSupportedNotifications() {
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
        final BindingRuntimeContext runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        for (final Method method : type.getMethods()) {
            if (BindingReflections.isNotificationCallback(method)) {
                final Class<?> notification = method.getParameterTypes()[0];
                QName qname = BindingRuntimeHelpers.getQName(runtimeContext, notification);
                ret.add(Absolute.of(qname));
            }
        }
        return ret;
    }
}
