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
import java.util.ServiceLoader;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.api.query.DescendantQueryBuilder;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Beta
@MetaInfServices
@Singleton
public final class DefaultQueryFactory implements QueryFactory {
    private final BindingCodecTree codec;

    public DefaultQueryFactory() {
        this(ServiceLoader.load(BindingCodecTree.class).findFirst().orElseThrow());
    }

    @Inject
    public DefaultQueryFactory(final BindingCodecTree codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    public <T extends DataObject> DescendantQueryBuilder<T> querySubtree(final InstanceIdentifier<T> rootPath) {
        return new DefaultDescendantQueryBuilder<>(codec, rootPath);
    }
}
