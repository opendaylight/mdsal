/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * A {@link ClassNamingStrategy} which is based on a {@link #nodeIdentifier()}'s {@link AbstractQName#getLocalName()}.
 */
abstract class YangIdentifierClassNamingStrategy extends ClassNamingStrategy {
    /**
     * Return the YANG node identifier backing this naming strategy. Only the {@link AbstractQName#getLocalName()} part
     * of the identifier is significant.
     *
     * @return YANG node identifier.
     */
    abstract @NonNull AbstractQName nodeIdentifier();

    @Override
    final String simpleClassName() {
        return BindingMapping.getClassName(rootName());
    }

    @Override
    final String rootName() {
        return nodeIdentifier().getLocalName();
    }

    @Override
    final String childPackage() {
        return CollisionDomain.packageString(nodeIdentifier());
    }
}
