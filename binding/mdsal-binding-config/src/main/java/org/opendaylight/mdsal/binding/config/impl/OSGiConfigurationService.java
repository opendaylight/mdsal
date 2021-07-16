/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.impl;

import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.config.api.ConfigurationListener;
import org.opendaylight.mdsal.binding.config.api.ConfigurationProvider;
import org.opendaylight.mdsal.binding.config.api.ConfigurationProviderService;
import org.opendaylight.mdsal.binding.config.api.ConfigurationService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

@NonNullByDefault
public final class OSGiConfigurationService implements ConfigurationService, ConfigurationProviderService {

    @Override
    public <T extends ChildOf<? super DataRoot>> Registration registerProvider(final Class<T> type,
            final ConfigurationProvider<T> provider) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends ChildOf<? super DataRoot>> Registration registerListener(final Class<T> type,
            final ConfigurationListener<T> listener, final Executor executor) {
        // TODO Auto-generated method stub
        return null;
    }
}
