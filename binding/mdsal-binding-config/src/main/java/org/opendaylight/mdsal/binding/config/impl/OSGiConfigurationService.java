/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.impl;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.config.api.ConfigurationListener;
import org.opendaylight.mdsal.binding.config.api.ConfigurationProvider;
import org.opendaylight.mdsal.binding.config.api.ConfigurationProviderService;
import org.opendaylight.mdsal.binding.config.api.ConfigurationService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@NonNullByDefault
public final class OSGiConfigurationService implements ConfigurationService, ConfigurationProviderService {

    // FIXME: this really needs to be centered around the datastore, so that ...

    @Override
    public <T extends ChildOf<? super DataRoot>> Registration registerProvider(final Class<T> type,
            final ConfigurationProvider<T> provider) {
        // FIXME: here we want to have a cluster singleton for each type which does not exist in the datastore. We then
        //        populate the datastore as needed and shut down the singleton again.
        //        well, actually, we may want to do 'config' -> 'oper' translation, so that we have a single source
        //        of truth.
        return null;
    }

    @Override
    public <T extends ChildOf<? super DataRoot>> Registration registerListener(final Class<T> type,
            final ConfigurationListener<T> listener, final Executor executor) {
        // FIXME: this really is just a listener dispatch towards DTCL:
        final InstanceIdentifier<T> id = InstanceIdentifier.create(type);
        final ConfigurationListener<T> checkedListener = requireNonNull(listener);
        final Executor checkedExecutor = requireNonNull(executor);

        // FIXME: register DTCL and fire checkedListener if the last value exists

        return null;
    }
}
