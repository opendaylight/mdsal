/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.spi.DataBrokerFactory;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;

/**
 * Implementation of DataBrokerFactory.
 *
 * @author Thomas Pantelis
 */
public class DataBrokerFactoryImpl implements DataBrokerFactory {
    private final BindingToNormalizedNodeCodec codec;

    public DataBrokerFactoryImpl(final BindingToNormalizedNodeCodec codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    public DataBroker newInstance(final DOMDataBroker domBroker) {
        return new BindingDOMDataBrokerAdapter(requireNonNull(domBroker), codec);
    }
}
