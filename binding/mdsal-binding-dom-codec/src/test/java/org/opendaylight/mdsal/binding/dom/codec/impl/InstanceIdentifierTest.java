/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal._355.norev.OspfStatLsdbBrief;
import org.opendaylight.yang.gen.v1.mdsal._355.norev.OspfStatLsdbBriefKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

public class InstanceIdentifierTest extends AbstractBindingCodecTest {
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<ContainerWithUses> BA_TREE_COMPLEX1 = BA_TOP_LEVEL_LIST
            .augmentationChild(ContainerWithUses.class);
    private static final InstanceIdentifier<ListViaUses> BA_TREE_COMPLEX2 = BA_TOP_LEVEL_LIST
            .augmentationChild(ListViaUses.class, new ListViaUsesKey("bar"));

    @Test
    public void testComplexAugmentationSerialization() {
        final YangInstanceIdentifier yii1 = codecContext.toYangInstanceIdentifier(BA_TREE_COMPLEX1);
        assertEquals(BA_TREE_COMPLEX1, codecContext.fromYangInstanceIdentifier(yii1));
        final YangInstanceIdentifier yii2 = codecContext.toYangInstanceIdentifier(BA_TREE_COMPLEX2);
        assertEquals(BA_TREE_COMPLEX2, codecContext.fromYangInstanceIdentifier(yii2));
    }

    @Test
    public void testCamelCaseKeys() {
        final InstanceIdentifier<?> result = codecContext.fromYangInstanceIdentifier(YangInstanceIdentifier.create(
            NodeIdentifier.create(OspfStatLsdbBrief.QNAME),
            NodeIdentifierWithPredicates.of(OspfStatLsdbBrief.QNAME, ImmutableMap.of(
                QName.create(OspfStatLsdbBrief.QNAME, "AreaIndex"), 1,
                QName.create(OspfStatLsdbBrief.QNAME, "LsaType"), Uint8.valueOf(2),
                QName.create(OspfStatLsdbBrief.QNAME, "LsId"), 3,
                QName.create(OspfStatLsdbBrief.QNAME, "AdvRtr"), "foo"))));
        assertTrue(result instanceof KeyedInstanceIdentifier);
        final Identifier<?> key = ((KeyedInstanceIdentifier<?, ?>) result).getKey();
        assertEquals(new OspfStatLsdbBriefKey("foo", 1, 3, Uint8.valueOf(2)), key);
    }
}
