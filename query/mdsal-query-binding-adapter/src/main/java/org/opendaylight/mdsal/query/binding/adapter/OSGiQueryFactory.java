/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.api.query.DescendantQueryBuilder;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
public final class OSGiQueryFactory implements QueryFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiQueryFactory.class);

    @Reference
    BindingCodecTree codec;

    private DefaultQueryFactory delegate;

    @Override
    public <T extends DataObject> DescendantQueryBuilder<T> querySubtree(final InstanceIdentifier<T> rootPath) {
        return delegate.querySubtree(rootPath);
    }

    @Activate
    void activate() {
        delegate = new DefaultQueryFactory(codec);
        LOG.info("Binding Query activated");
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Binding Query deactivated");
    }
}
