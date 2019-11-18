/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import ch.vorburger.xtendbeans.AssertBeans;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link AugmentableExtension}.
 *
 * @author Michael Vorburger
 */
public class AugmentableExtensionTest extends AbstractDataBrokerTest {

    private final AugmentableExtension augmentableExtension = new AugmentableExtension();

    @Test
    public void testAugmentableExtensionOnYangObjectByBuilder() {
        TopLevelList topLevelList = ExampleYangObjects.topLevelList().getValue();
        Map<Class<? extends Augmentation<?>>, Augmentation<?>> augmentations = augmentableExtension
                .getAugmentations(topLevelList);
        AssertBeans.assertEqualByText("#{\n"
                + "    org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test"
                +        ".augment.rev140709.TreeComplexUsesAugment -> (new TreeComplexUsesAugmentBuilder => [\n"
                + "        containerWithUses = (new ContainerWithUsesBuilder => [\n"
                + "            leafFromGrouping = \"foo\"\n"
                + "        ]).build()\n"
                + "    ]).build()\n"
                + "}", augmentations);
    }

    @Test
    public void testAugmentableExtensionWithDataBroker() throws Exception {
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        put(writeTx, OPERATIONAL, ExampleYangObjects.topLevelList());
        writeTx.commit().get();

        ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        InstanceIdentifier<Top> id = InstanceIdentifier.create(Top.class);
        Top actualTop = readTx.read(OPERATIONAL, id).get().get();
        AssertBeans.assertEqualByText("#{\n}", augmentableExtension.getAugmentations(actualTop));

        TopLevelList topLevelList = actualTop.getTopLevelList().values().iterator().next();
        AssertDataObjects.assertEqualByText("#{\n"
                + "    TreeComplexUsesAugment -> new TreeComplexUsesAugmentBuilder >> [\n"
                + "        containerWithUses = new ContainerWithUsesBuilder >> [\n"
                + "            leafFromGrouping = \"foo\"\n"
                + "        ]\n"
                + "    ]\n"
                + "}", augmentableExtension.getAugmentations(topLevelList));
    }

    <T extends DataObject> void put(WriteTransaction tx, LogicalDatastoreType store,
            Map.Entry<InstanceIdentifier<T>, T> obj) {
        tx.put(OPERATIONAL, obj.getKey(), obj.getValue());
    }

}
