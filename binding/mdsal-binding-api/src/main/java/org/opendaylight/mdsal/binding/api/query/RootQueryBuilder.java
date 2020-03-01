/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.OdlGeneralEntityData;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * @author Robert Varga
 *
 */
public interface RootQueryBuilder<R extends DataRoot> extends QueryBuilder<R> {


    static final class Test {

        static void foo() throws QueryStructureException, QueryExecutionException {
            final OdlGeneralEntityData root = QueryBuilderFactory.test()
                    .from(OdlGeneralEntityData.class)
                    .build()
                    // Execution start
                    .getResult()
                    // Execution fetchd
                    .getValue();
        }
    }
}
