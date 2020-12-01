/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true, service = BootstrapSupport.class)
public class OSGiBootstrapSupportProvider implements BootstrapSupport {

    private AbstractBootstrapSupport delegate;

    @Activate
    void activate(Map<String, Object> properties) {
        delegate = AbstractBootstrapSupport.create();
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @Override
    public @NonNull Bootstrap newBootstrap() {
        return delegate.newBootstrap();
    }

    @Override
    public @NonNull ServerBootstrap newServerBootstrap() {
        return delegate.newServerBootstrap();
    }
}
