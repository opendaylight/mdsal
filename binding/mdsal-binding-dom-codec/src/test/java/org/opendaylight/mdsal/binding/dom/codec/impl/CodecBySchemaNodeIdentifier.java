/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.ContChoice;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.cont.choice.ContBase;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.grp.GrpCont;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

public class CodecBySchemaNodeIdentifier extends AbstractBindingCodecTest {
    @Test
    public void testDataObjectNodeCounterpart() {
        final YangInstanceIdentifier listId = YangInstanceIdentifier.builder(YangInstanceIdentifier.of(Top.QNAME))
                .node(TopLevelList.QNAME)
                .nodeWithKey(TopLevelList.QNAME, QName.create(TopLevelList.QNAME, "name"), "foo")
                .build();

        final PathArgument augmentationId =
                AugmentationIdentifier.create(Set.of(ListViaUses.QNAME,
                        QName.create(ListViaUses.QNAME, "container-with-uses")));

        assertEquals(codecContext.getSubtreeCodec(SchemaNodeIdentifier.Absolute.of(Top.QNAME, TopLevelList.QNAME)),
                codecContext.getSubtreeCodec(listId));

        final YangInstanceIdentifier yangLeafId =
                listId.node(augmentationId).node(ListViaUses.QNAME);

        final SchemaNodeIdentifier.Absolute augmentedLeafId = SchemaNodeIdentifier.Absolute.of(Top.QNAME,
                        TopLevelList.QNAME,
                        QName.create(ListViaUses.QNAME, "list-via-uses"));

        assertEquals(codecContext.getSubtreeCodec(augmentedLeafId), codecContext.getSubtreeCodec(yangLeafId));
    }

    @Test
    public void testChoiceNodeContextCounterPart() {
        BindingCodecTreeNode schemaNodeCodec = codecContext.getSubtreeCodec(SchemaNodeIdentifier.Absolute.of(Cont.QNAME,
                ContChoice.QNAME, ContBase.QNAME,GrpCont.QNAME));


        BindingCodecTreeNode yangInstanceCodec = codecContext
                .getSubtreeCodec(YangInstanceIdentifier.create(YangInstanceIdentifier.NodeIdentifier.create(Cont.QNAME),
                        YangInstanceIdentifier.NodeIdentifier.create(ContChoice.QNAME),
                        YangInstanceIdentifier.NodeIdentifier.create(GrpCont.QNAME)));

        assertEquals(schemaNodeCodec, yangInstanceCodec);
    }
}
