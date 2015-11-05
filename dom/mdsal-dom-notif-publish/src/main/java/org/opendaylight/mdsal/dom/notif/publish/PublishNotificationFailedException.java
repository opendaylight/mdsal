/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.notif.publish;

import com.google.common.base.Function;
import org.opendaylight.mdsal.dom.api.DOMRpcException;

final class PublishNotificationFailedException extends DOMRpcException {
    private static final long serialVersionUID = 1L;

    static final Function<Exception, DOMRpcException> MAPPER = new Function<Exception, DOMRpcException>() {
        @Override
        public DOMRpcException apply(final Exception input) {
            return new PublishNotificationFailedException(input);
        }
    };

    PublishNotificationFailedException(final Exception cause) {
        super("Failed to publish notification", cause);
    }
}
