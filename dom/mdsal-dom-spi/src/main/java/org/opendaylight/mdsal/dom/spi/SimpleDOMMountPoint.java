/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.util.Optional;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public final class SimpleDOMMountPoint implements DOMMountPoint {

    private final YangInstanceIdentifier identifier;
    private final ClassToInstanceMap<DOMService> services;
    private final EffectiveModelContext schemaContext;

    private SimpleDOMMountPoint(final YangInstanceIdentifier identifier,
            final ClassToInstanceMap<DOMService> services, final EffectiveModelContext ctx) {
        this.identifier =  requireNonNull(identifier);
        this.services = ImmutableClassToInstanceMap.copyOf(services);
        this.schemaContext = ctx;
    }

    public static SimpleDOMMountPoint create(final YangInstanceIdentifier identifier,
            final ClassToInstanceMap<DOMService> services, final EffectiveModelContext ctx) {
        return new SimpleDOMMountPoint(identifier, services, ctx);
    }

    @Override
    public YangInstanceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        checkState(schemaContext != null, "Mount point %s does not have a model context", identifier);
        return schemaContext;
    }

    @Override
    public <T extends DOMService> Optional<T> getService(final Class<T> cls) {
        return Optional.ofNullable(services.getInstance(cls));
    }
}
