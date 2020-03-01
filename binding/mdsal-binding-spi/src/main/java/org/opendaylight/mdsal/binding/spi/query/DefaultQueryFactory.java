/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi.query;

import com.google.common.annotations.Beta;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.SubtreeQuery;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Component;

/**
 * @author Robert Varga
 *
 */
@Beta
@MetaInfServices
@Singleton
@Component(immediate = true)
public final class DefaultQueryFactory implements QueryFactory {
    @Override
    public <T extends DataObject> SubtreeQuery<T> querySubtree(final InstanceIdentifier<T> rootPath) {
        return new DefaultSubtreeQuery<>(rootPath);
    }
}
