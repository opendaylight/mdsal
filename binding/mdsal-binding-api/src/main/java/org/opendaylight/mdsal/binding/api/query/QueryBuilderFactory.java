/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Your basic hammer builder factory pattern.
 */
public interface QueryBuilderFactory extends Immutable {

    <R extends DataRoot> RootQueryBuilder<R> from(Class<R> modelRoot);

    <T extends DataObject> DataObjectQueryBuilder<T> from(InstanceIdentifier<T> rootPath);

    static QueryBuilderFactory test() {
        return null;
    }
}
