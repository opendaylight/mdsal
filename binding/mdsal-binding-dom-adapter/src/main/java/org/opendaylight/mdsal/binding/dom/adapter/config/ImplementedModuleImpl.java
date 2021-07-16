/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.config;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.api.config.ImplementedModule;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.yang.binding.DataRoot;

final class ImplementedModuleImpl<M extends DataRoot> extends AbstractRegistration implements ImplementedModule<M> {
    private final List<? extends ConfigurationListenerBridge<?>> bridges;

    ImplementedModuleImpl(final List<? extends ConfigurationListenerBridge<?>> bridges) {
        this.bridges = requireNonNull(bridges);
    }

    @Override
    protected void removeRegistration() {
        bridges.forEach(ConfigurationListenerBridge::close);
    }
}
