/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Provides single method invocation of notification callbacks on supplied instance.
 *
 * <p>
 * Notification Listener invoker provides common invocation interface for any subtype of
 * {@link NotificationListener}. via {@link #invokeNotification(NotificationListener, QName, Instantiable)}
 * method.
 */
@Beta
public final class NotificationListenerInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    private static final LoadingCache<Class<? extends NotificationListener>, NotificationListenerInvoker> INVOKERS =
            CacheBuilder.newBuilder().weakKeys()
                    .build(new CacheLoader<Class<? extends NotificationListener>, NotificationListenerInvoker>() {

                        private NotificationListenerInvoker
                                createInvoker(final Class<? extends NotificationListener> key) {
                            return new NotificationListenerInvoker(createInvokerMap(key));
                        }

                        private Map<QName, MethodHandle>
                                createInvokerMap(final Class<? extends NotificationListener> key) {
                            final Builder<QName, MethodHandle> ret = ImmutableMap.builder();
                            for (final Method method : key.getMethods()) {
                                if (BindingReflections.isNotificationCallback(method)) {

                                    final Class<?> notification = method.getParameterTypes()[0];
                                    final QName name = BindingReflections.findQName(notification);
                                    MethodHandle handle;
                                    try {
                                        handle = LOOKUP.unreflect(method).asType(MethodType.methodType(void.class,
                                                NotificationListener.class, Instantiable.class));
                                        ret.put(name, handle);
                                    } catch (final IllegalAccessException e) {
                                        throw new IllegalStateException("Can not access public method.", e);
                                    }
                                }

                            }
                            return ret.build();
                        }

                        @Override
                        public NotificationListenerInvoker load(final Class<? extends NotificationListener> key)
                                throws Exception {
                            return createInvoker(key);
                        }

                    });

    private final Map<QName, MethodHandle> methodInvokers;

    public NotificationListenerInvoker(final Map<QName, MethodHandle> map) {
        this.methodInvokers = map;
    }

    /**
     * Creates Notification service invoker for specified type.
     *
     * @param type
     *            - NotificationListener interface, which was generated from model.
     * @return Cached instance of {@link NotificationListenerInvoker} for supplied notification type.
     *
     */
    public static NotificationListenerInvoker from(final Class<? extends NotificationListener> type) {
        Preconditions.checkArgument(type.isInterface());
        Preconditions.checkArgument(BindingReflections.isBindingClass(type));
        return INVOKERS.getUnchecked(type);
    }

    /**
     * Invokes supplied Notification on provided implementation of NotificationListener.
     *
     * @param impl
     *            - implementation on which notification callback should be invoked.
     * @param notificationName
     *            - name of notification to be invoked.
     * @param input
     *            - input data for notification.
     *
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void invokeNotification(@Nonnull final NotificationListener impl, @Nonnull final QName notificationName,
            @Nullable final Instantiable<?> input) {
        requireNonNull(impl, "implementation must be supplied");
        final MethodHandle invoker = methodInvokers.get(notificationName);
        Preconditions.checkArgument(invoker != null, "Supplied notification is not valid for implementation %s", impl);
        try {
            invoker.invokeExact(impl, input);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}

