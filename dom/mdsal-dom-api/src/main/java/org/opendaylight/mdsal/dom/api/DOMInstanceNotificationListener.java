/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A listener registered to receive instance (YANG 1.1) {@link DOMNotification}s from a
 * {@link DOMInstanceNotificationService}.
 */
@Beta
@FunctionalInterface
@NonNullByDefault
public interface DOMInstanceNotificationListener {
    /**
     * Invoked whenever a {@link DOMNotification} matching the subscription criteria is received.
     *
     * @param path Notification's parent path
     * @param notification Received notification
     */
    void onNotification(DOMDataTreeIdentifier path, DOMNotification notification);
}
