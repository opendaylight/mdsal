/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ModificationContextNodeBuilderTest extends ModificationContextNodeBuilder {

    @Test
    public void basicTest() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(
                        TestUtils.DOM_DATA_TREE_IDENTIFIER, TestUtils.DOM_DATA_TREE_SHARD_PRODUCER);
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(foreignShardModificationContext);
        super.addBoundary(TestUtils.PATH_ARGUMENT, writeableSubshardBoundaryNode);

        assertNotNull(super.getInterior(TestUtils.PATH_ARGUMENT));
    }
}