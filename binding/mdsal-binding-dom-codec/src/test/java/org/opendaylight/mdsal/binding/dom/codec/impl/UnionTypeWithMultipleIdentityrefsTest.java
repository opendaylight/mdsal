/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.junit.Test
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.IdentOne;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.UnionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.union.test.rev220428.UnionWithMultiIdentityrefData;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class UnionTypeWithMultipleIdentityrefsTest extends AbstractBindingCodecTest {

    public static final QName NODE_QNAME = QName.create("urn:opendaylight:yang:union:test",
            "2022-04-28", "union-with-multi-identityref");
    public static final QName NODE_LEAF_QNAME = QName.create(NODE_QNAME, "test-union-leaf");

    @Test
    public void leafNodeToBindingInstanceTest() {
        UnionType unionType = new UnionType(IdentOne.VALUE);

        LeafNode testNode =  ImmutableNodes.leafNode(NODE_LEAF_QNAME, unionType);
//        codecContext.toNormalizedNode(YangInstanceIdentifier.NodeIdentifier.create(NODE_LEAF_QNAME), testNode);
//        codecContext
//                .toNormalizedNode(InstanceIdentifier.builder(UnionType.class).build(), testNode)
//                .getValue();
    }
}
