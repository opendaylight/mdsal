/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;

@NonNullByDefault
public class YangDataNamingStrategy extends CamelCaseNamingStrategy {
    private final String originalName;

    public YangDataNamingStrategy(final StatementNamespace namespace, final String originalName, final String altName) {
        super(namespace, buildNodeIdentifier(requireNonNull(originalName), requireNonNull(altName)));
        this.originalName = originalName;
    }

    private static AbstractQName buildNodeIdentifier(final String originalName, final String altName) {
        final AbstractQName originalNodeIdentier = UnresolvedQName.tryLocalName(originalName);
        return originalNodeIdentier == null ? UnresolvedQName.tryLocalName(altName) : originalNodeIdentier;
    }

    @Override
    String simpleClassName() {
        // if name is yang identifier compliant then use camel case transformation,
        // encode non-compliant characters otherwise
        return originalName.equals(nodeIdentifier().getLocalName())
                ? BindingMapping.getClassName(originalName) : BindingMapping.mapYangDataName(originalName);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("originalName", originalName);
    }
}
