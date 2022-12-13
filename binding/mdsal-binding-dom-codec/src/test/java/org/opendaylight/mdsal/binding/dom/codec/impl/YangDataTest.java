/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataContainer;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataDemoData;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataList;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataListBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp.container.ContainerFromGroup;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp.container.ContainerFromGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp.list.ListFromGroup;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.grp.list.ListFromGroupBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

public class YangDataTest extends AbstractBindingCodecTest {

    private static final String STRING_VALUE = "string-value";
    private static final Uint32 UINT32_VALUE = Uint32.valueOf(12345);


    @Test
    public void yangDataSubstructures() {

        thereAndBackAgain(
            // yang-data > container
            InstanceIdentifier.create(YangDataContainer.class),
            new YangDataContainerBuilder().setStr(STRING_VALUE).build());

        thereAndBackAgain(
            //  yang-data > uses > grp > container
            InstanceIdentifier.builderOfInherited(YangDataDemoData.class, ContainerFromGroup.class).build(),
            new ContainerFromGroupBuilder().setStr(STRING_VALUE).build());

        thereAndBackAgain(
            // yang-data > list
            InstanceIdentifier.create(YangDataList.class),
            new YangDataListBuilder().setStr(STRING_VALUE).build());

        thereAndBackAgain(
            // yang-data > uses > group > list
            InstanceIdentifier.builderOfInherited(YangDataDemoData.class, ListFromGroup.class).build(),
            new ListFromGroupBuilder().setNum(UINT32_VALUE).build());

    }

}
