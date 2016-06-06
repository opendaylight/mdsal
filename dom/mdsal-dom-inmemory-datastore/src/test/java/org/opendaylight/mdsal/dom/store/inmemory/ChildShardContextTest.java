/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class ChildShardContextTest implements TestUtils {

    @Test
    public void basicTest() throws Exception {
        final WriteableDOMDataTreeShard writeableDOMDataTreeShard = mock(WriteableDOMDataTreeShard.class);
        final ChildShardContext childShardContext =
                new ChildShardContext(DOM_DATA_TREE_IDENTIFIER, writeableDOMDataTreeShard);

        assertEquals(childShardContext.getPrefix(), DOM_DATA_TREE_IDENTIFIER);
        assertEquals(childShardContext.getShard(), writeableDOMDataTreeShard);
    }
}