/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.query.Query;
import org.opendaylight.mdsal.binding.api.query.QueryExecutionException;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.QueryStructureException;
import org.opendaylight.mdsal.binding.api.query.Result;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
