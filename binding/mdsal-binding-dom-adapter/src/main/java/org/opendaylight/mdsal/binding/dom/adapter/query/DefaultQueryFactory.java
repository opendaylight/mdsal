/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.query.DescendantQueryBuilder;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Beta
@Singleton
public final class DefaultQueryFactory implements QueryFactory {
    private final BindingNormalizedNodeCodecRegistry registry;

    @Inject
    public DefaultQueryFactory(final BindingNormalizedNodeCodecRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public <T extends DataObject> DescendantQueryBuilder<T> querySubtree(final InstanceIdentifier<T> rootPath) {
        return new DefaultDescendantQueryBuilder<>(registry.getCodecContext(), rootPath);
    }
}
