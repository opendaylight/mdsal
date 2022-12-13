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

/**
 * Naming strategy for {@code ietf-restconf:yang-data} template which has a generic string not matching YANG identifier.
 */
@NonNullByDefault
public class YangDataNamingStrategy extends AbstractNamespacedNamingStrategy {
    private final String templateName;

    public YangDataNamingStrategy(final StatementNamespace namespace, final String templateName) {
        super(namespace);
        this.templateName = requireNonNull(templateName);
    }

    @Override
    AbstractQName nodeIdentifier() {
        // FIXME: refactor so we do not need this method
        throw new UnsupportedOperationException();
    }

    @Override
    String simpleClassName() {
        return BindingMapping.mapYangDataName(templateName);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("originalName", templateName);
    }
}
