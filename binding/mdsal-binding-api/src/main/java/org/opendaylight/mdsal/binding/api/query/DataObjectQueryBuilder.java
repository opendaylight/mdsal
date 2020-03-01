/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Beta
public interface DataObjectQueryBuilder<T extends DataObject> extends QueryBuilder<T> {

    static final class Test {

        static void foo() throws QueryStructureException, QueryExecutionException {
            final Entity entity = QueryBuilderFactory.test()
                    .from(InstanceIdentifier.create(Entity.class))
                    .build()
                    // Execution start
                    .getResult()
                    // Execution fetchd
                    .getValue();
        }
    }
}
