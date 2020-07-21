/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * A single YANG notification. This interface captures the data portion of a notification. Implementations may choose
 * to additionally implement {@link DOMEvent}, in which case {@link DOMEvent#getEventInstant()} returns the time when
 * this notification was generated -- and corresponds to <a href="https://tools.ietf.org/html/rfc5277#section-2.2.1">
 * RFC5277</a> NETCONF notification's {@code eventTime} parameter.
 */
public interface DOMNotification {
    /**
     * Return the type of this notification.
     *
     * @return Notification type.
     */
    @NonNull Absolute getType();

    /**
     * Return the body of this notification.
     *
     * @return Notification body.
     */
    @NonNull ContainerNode getBody();
}
