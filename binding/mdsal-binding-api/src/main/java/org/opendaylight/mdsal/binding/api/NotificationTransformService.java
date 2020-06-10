/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * A plug-in service which allows on-demand transformation of notifications. This service acts as a conceptual bridge
 * between {@link NotificationService} (as the source of input notifications) and {@link NotificationPublishService}
 * (as the sing of output notifications).
 *
 * <p>
 * Bridging occurs through one of transform functions, such as {@link OneToOneTransformer} or
 * {@link OneToOptionalTransformer}.
 */
@Beta
public interface NotificationTransformService extends BindingService {
    /**
     * Register a one-to-one notification transformer.
     *
     * @param <I> Input notification type
     * @param <O> Output notification type
     * @param input Input notification class
     * @param output Output notification class
     * @param transformer Transformer function
     * @return Registration object
     * @throws NullPointerException if any argument is null
     */
    <I extends Notification, O extends Notification> Registration registerNotificationTransformer(Class<I> input,
            Class<O> output, OneToOneTransformer<I, O> transformer);

    /**
     * Register a one-to-optional notification transformer.
     *
     * @param <I> Input notification type
     * @param <O> Output notification type
     * @param input Input notification class
     * @param output Output notification class
     * @param transformer Transformer function
     * @return Registration object
     * @throws NullPointerException if any argument is null
     */
    <I extends Notification, O extends Notification> Registration registerNotificationTransformer(Class<I> input,
            Class<O> output, OneToOptionalTransformer<I, O> transformer);

    /**
     * A one-to-one transformer. For each input notification it produces exactly one output notification.
     *
     * @param <I> Input notification type
     * @param <O> Output notification type
     */
    @FunctionalInterface
    interface OneToOneTransformer<I extends Notification, O extends Notification> {
        /**
         * Transform an input notification to an output notification.
         *
         * @param input Input notification, guaranteed to be non-null
         * @return Output notification, must not be null
         */
        @NonNull O transform(@NonNull I input);
    }

    /**
     * A one-to-optional transformer. For each input notification it produces at most one output notification.
     *
     * @param <I> Input notification type
     * @param <O> Output notification type
     */
    @FunctionalInterface
    interface OneToOptionalTransformer<I extends Notification, O extends Notification> {
        /**
         * Transform an input notification to an output notification.
         *
         * @param input Input notification, guaranteed to be non-null
         * @return Output notification, potentially empty
         */
        Optional<O> transform(@NonNull I input);
    }
}
