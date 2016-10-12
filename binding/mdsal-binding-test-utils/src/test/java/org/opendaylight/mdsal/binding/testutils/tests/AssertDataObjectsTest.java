/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils.tests;

import static org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType.OPERATIONAL;

import java.util.Map;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.binding.testutils.AssertDataObjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.list.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.list.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Tests the {@link AssertDataObjects} utility.
 *
 * @author Michael Vorburger
 */
public class AssertDataObjectsTest extends AbstractDataBrokerTest {

    private static final String HEADER = "import static extension org.opendaylight.mdsal.binding.testutils."
            + "XtendBuilderExtensions.operator_doubleGreaterThan\n\n";

    @Test
    public void testAssertDataObjectsWithTopLevelListKey() {
        AssertDataObjects.assertEqualByText("new TopLevelListKey(\"test\")\n", new TopLevelListKey("test"));
    }

    @Test
    public void testAssertDataObjectsWithEmptyTop() {
        AssertDataObjects.assertEqualByText(HEADER + "new TopBuilder\n", ExampleYangObjects.topEmpty().getValue());
    }

    @Test
    public void testAssertDataObjectsWithComplexTopWithKey() {
        AssertDataObjects.assertEqualByText(HEADER + "new TopBuilder >> [\n"
                + "    topLevelList = #[\n"
                + "        new TopLevelListBuilder >> [\n"
                + "            name = \"foo\"\n"
                + "            addAugmentation(TreeComplexUsesAugment, new TreeComplexUsesAugmentBuilder >> [\n"
                + "                containerWithUses = new ContainerWithUsesBuilder >> [\n"
                + "                    leafFromGrouping = \"foo\"\n"
                + "                ]\n"
                + "            ])\n"
                + "        ]\n"
                + "    ]\n"
                + "]", ExpectedObjects.top());
    }

    @Test
    public void testAssertDataObjectsWithTopLevelList() {
        AssertDataObjects.assertEqualBeans(ExpectedObjects.topLevelList(),
                ExampleYangObjects.topLevelList().getValue());
        AssertDataObjects.assertEqualByText(HEADER + "new TopLevelListBuilder >> [\n"
                + "    name = \"foo\"\n"
                + "    addAugmentation(TreeComplexUsesAugment, new TreeComplexUsesAugmentBuilder >> [\n"
                + "        containerWithUses = new ContainerWithUsesBuilder >> [\n"
                + "            leafFromGrouping = \"foo\"\n"
                + "        ]\n"
                + "    ])\n"
                + "]", ExampleYangObjects.topLevelList().getValue());
    }

    @Test
    public void testAssertDataObjectsWithDataBroker() throws Exception {
        WriteTransaction initialTx = getDataBroker().newWriteOnlyTransaction();
        put(initialTx, OPERATIONAL, ExampleYangObjects.topEmpty());
        put(initialTx, OPERATIONAL, ExampleYangObjects.topLevelList());
        initialTx.submit().checkedGet();

        ReadOnlyTransaction readTx = getDataBroker().newReadOnlyTransaction();
        InstanceIdentifier<Top> id = InstanceIdentifier.create(Top.class);
        Top actualTop = readTx.read(OPERATIONAL, id).checkedGet().get();

        AssertDataObjects.assertEqualBeans(ExpectedObjects.top(), actualTop);

        String expectedTopText = "import static extension org.opendaylight.mdsal.binding.testutils."
                + "XtendBuilderExtensions.operator_doubleGreaterThan\n\n"
                + "new TopBuilder >> [\n"
                + "    topLevelList = #[\n"
                + "        new TopLevelListBuilder >> [\n"
                + "            name = \"foo\"\n"
                + "            addAugmentation(TreeComplexUsesAugment, new TreeComplexUsesAugmentBuilder >> [\n"
                + "                containerWithUses = new ContainerWithUsesBuilder >> [\n"
                + "                    leafFromGrouping = \"foo\"\n"
                + "                ]\n"
                + "            ])\n"
                + "        ]\n"
                + "    ]\n"
                + "]";
        AssertDataObjects.assertEqualByText(expectedTopText, actualTop);
    }

    <T extends DataObject> void put(WriteTransaction tx, LogicalDatastoreType store,
            Map.Entry<InstanceIdentifier<T>, T> obj) {
        tx.put(OPERATIONAL, obj.getKey(), obj.getValue());
    }

}
