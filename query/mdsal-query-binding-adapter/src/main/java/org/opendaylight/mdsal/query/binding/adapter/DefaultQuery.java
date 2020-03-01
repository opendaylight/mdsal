/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import org.opendaylight.mdsal.query.binding.api.Query;
import org.opendaylight.mdsal.query.binding.api.Result;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultQuery<T extends DataObject> implements Query<T> {

    DefaultQuery(final AdaptingQueryBuilder builder) {

    }

    @Override
    public Result<T> getResult() {
        // TODO Auto-generated method stub
        return null;
    }

}
