/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ExampleYangObjects {
    private ExampleYangObjects() {

    }

    public static AbstractMap.SimpleImmutableEntry<InstanceIdentifier<Top>, Top> topEmpty() {
        return new SimpleImmutableEntry<>(InstanceIdentifier.create(Top.class), new TopBuilder().build());
    }

    public static AbstractMap.SimpleImmutableEntry<InstanceIdentifier<TopLevelList>, TopLevelList> topLevelList() {
        TreeComplexUsesAugment fooAugment = new TreeComplexUsesAugmentBuilder()
                .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build()).build();
        return new SimpleImmutableEntry<>(
                ListsBindingUtils.path(ListsBindingUtils.TOP_FOO_KEY),
                    ListsBindingUtils.topLevelList(ListsBindingUtils.TOP_FOO_KEY, fooAugment));
    }
}
