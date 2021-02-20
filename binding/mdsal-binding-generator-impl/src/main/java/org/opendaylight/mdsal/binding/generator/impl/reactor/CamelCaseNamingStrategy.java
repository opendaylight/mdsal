/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

@NonNullByDefault
final class CamelCaseNamingStrategy extends NamingStrategy {
    private final StatementNamespace namespace;
    private final AbstractQName localName;

    CamelCaseNamingStrategy(final StatementNamespace namespace, final AbstractQName localName) {
        this.namespace = requireNonNull(namespace);
        this.localName = requireNonNull(localName);
    }

    StatementNamespace namespace() {
        return namespace;
    }

    @Override
    String simpleClassName() {
        return BindingMapping.getClassName(localName.getLocalName());
    }

    @Override
    String packageNameSegment() {
        // Replace dashes with dots, as dashes are not allowed in package names
        return localName.getLocalName().replace('-', '.');
    }

    @Override
    @NonNull NamingStrategy fallback() {
        return new CamelCaseWithNamespaceNamingStrategy(this);
    }
}
