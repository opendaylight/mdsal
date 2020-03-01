/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import org.junit.Test;
import org.opendaylight.mdsal.query.binding.api.Query;
import org.opendaylight.mdsal.query.binding.api.QueryExecutionException;
import org.opendaylight.mdsal.query.binding.api.QueryFactory;
import org.opendaylight.mdsal.query.binding.api.QueryStructureException;
import org.opendaylight.mdsal.query.binding.api.Result;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class QueryBuilderTest {
    private final QueryFactory factory = new DefaultQueryFactory();

    @Test
    public void bar() throws QueryStructureException, QueryExecutionException {
        final Query<TopLevelList> query = factory.querySubtree(InstanceIdentifier.builder(Top.class).build())
                .extractChild(TopLevelList.class)
                .matching()
                    .leaf(TopLevelList::getName).contains("foo")
                .build();

        // Execution start
        final Result<TopLevelList> result = query.getResult();
        // Execution fetch
        final TopLevelList value = result.getValue();

    }
}
