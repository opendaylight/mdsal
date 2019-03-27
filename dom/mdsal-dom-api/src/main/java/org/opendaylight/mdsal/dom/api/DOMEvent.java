/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.time.Instant;

/**
 * Generic event interface. This interface is mixed in into implementations of other DOM-level constructs, such as
 * {@link DOMNotification} to add the time when the event occurred when appropriate.
 */
public interface DOMEvent {
    /**
     * Get the time of the event occurrence.
     *
     * @return the event time
     */
    Instant getEventInstant();
}
