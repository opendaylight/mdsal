/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testkit.spi;

import com.google.common.annotations.Beta;
import java.util.function.Predicate;
import org.opendaylight.mdsal.binding.api.DataBroker;

@Beta
public interface CapturingDataBroker extends DataBroker {
    @FunctionalInterface
    interface EventPredicate extends Predicate<Object> {

    }

    void fireCapturedEvents(EventPredicate predicate);
}
