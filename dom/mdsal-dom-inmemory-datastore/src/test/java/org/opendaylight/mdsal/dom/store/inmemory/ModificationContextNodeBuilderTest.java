/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.PATH_ARGUMENT;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.WRITEABLE_MODIFICATION_NODE;

import java.util.Map;
import org.junit.Test;

public class ModificationContextNodeBuilderTest extends ModificationContextNodeBuilder {

    @Test
    public void basicTest() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(foreignShardModificationContext);
        super.addBoundary(PATH_ARGUMENT, writeableSubshardBoundaryNode);

        assertNotNull(super.getInterior(PATH_ARGUMENT));
        assertEquals(WRITEABLE_MODIFICATION_NODE, super.build());
    }

    @Override
    WriteableModificationNode build(Map children) {
        return WRITEABLE_MODIFICATION_NODE;
    }
}