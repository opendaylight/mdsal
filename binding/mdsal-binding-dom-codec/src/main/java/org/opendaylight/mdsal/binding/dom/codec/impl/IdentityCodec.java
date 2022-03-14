/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.dom.codec.api.BindingIdentityCodec;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractIllegalArgumentCodec;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.common.QName;

final class IdentityCodec extends AbstractIllegalArgumentCodec<QName, BaseIdentity> implements BindingIdentityCodec {
    private final BindingRuntimeContext context;

    IdentityCodec(final BindingRuntimeContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    protected BaseIdentity deserializeImpl(final QName input) {
        return toBinding(input);
    }

    @Override
    protected QName serializeImpl(final BaseIdentity input) {
        return fromBinding(input);
    }

    @Override
    public <T extends BaseIdentity> T toBinding(final QName qname) {
        final Class<?> identity = context.getIdentityClass(requireNonNull(qname));
        checkArgument(BaseIdentity.class.isAssignableFrom(identity), "%s resolves to non-identity %s", qname, identity);
        return (T) BindingReflections.getValue(identity.asSubclass(BaseIdentity.class));
    }

    @Override
    public QName fromBinding(final BaseIdentity bindingValue) {
        return BindingReflections.getQName(bindingValue.implementedInterface());
    }
}
