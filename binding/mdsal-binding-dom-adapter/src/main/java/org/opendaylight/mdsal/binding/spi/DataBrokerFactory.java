/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;

/**
 * Factory for creating DataBroker instances.
 *
 * @author Thomas Pantelis
 */
@NonNullByDefault
public interface DataBrokerFactory {
    /**
     * Return a {@link DataBroker} implementation backed by the specified {@link DOMDataBroker}.
     *
     * @param domBroker Backing DOMDataBroker
     * @return A DataBroker instance.
     * @throws NullPointerException if {@code domBroker} is null.
     */
    DataBroker newInstance(DOMDataBroker domBroker);
}
