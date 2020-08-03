/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.util.Optional;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public final class SimpleDOMMountPoint implements DOMMountPoint {

    private final YangInstanceIdentifier identifier;
    private final ClassToInstanceMap<DOMService> services;

    private SimpleDOMMountPoint(final YangInstanceIdentifier identifier,
            final ClassToInstanceMap<DOMService> services) {
        this.identifier =  requireNonNull(identifier);
        this.services = ImmutableClassToInstanceMap.copyOf(services);
    }

    public static SimpleDOMMountPoint create(final YangInstanceIdentifier identifier,
            final ClassToInstanceMap<DOMService> services) {
        return new SimpleDOMMountPoint(identifier, services);
    }

    @Override
    public YangInstanceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public <T extends DOMService> Optional<T> getService(final Class<T> cls) {
        return Optional.ofNullable(services.getInstance(cls));
    }
}
