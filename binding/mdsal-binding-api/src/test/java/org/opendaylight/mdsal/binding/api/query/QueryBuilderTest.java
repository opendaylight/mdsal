/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class QueryBuilderTest {
    @Mock
    private QueryFactory factory;

    @Test
    public void bar() throws QueryStructureException, QueryExecutionException {
        final QueryRoot<Top> root = factory.newQueryRoot(InstanceIdentifier.builder(Top.class).build());

        final QueryResultType<TopLevelList> type = root.newResultTypeBuilder()
                .child(TopLevelList.class)
                .build();

        final ValueMatch<TopLevelList> predicate = type.newMatchBuilder().leaf(TopLevelList::getName).contains("foo");

//
//
//        final TopLevelList result = factory.newQuery(root, type)
//                // Execution start
//                .getResult()
//                // Execution fetch
//                .getValue();
    }
}
