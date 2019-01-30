/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for {@link DOMNotificationRouter}.
 *
 * @author Michael Vorburger.ch
 */
public interface DOMNotificationRouterConfig {

    int queueDepth();

    long spinTime();

    long parkTime();

    TimeUnit timeUnit();


    class Immutable implements DOMNotificationRouterConfig {

        private final int queueDepth;
        private final long spinTime;
        private final long parkTime;
        private final TimeUnit timeUnit;

        public Immutable(int queueDepth, long spinTime, long parkTime, TimeUnit timeUnit) {
            this.queueDepth = queueDepth;
            this.spinTime = spinTime;
            this.parkTime = parkTime;
            this.timeUnit = timeUnit;
        }

        @Override
        public int queueDepth() {
            return queueDepth;
        }

        @Override
        public long spinTime() {
            return spinTime;
        }

        @Override
        public long parkTime() {
            return parkTime;
        }

        @Override
        public TimeUnit timeUnit() {
            return timeUnit;
        }
    }
}
