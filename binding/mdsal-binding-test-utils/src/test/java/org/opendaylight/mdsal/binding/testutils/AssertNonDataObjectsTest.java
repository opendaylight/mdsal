/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import ch.vorburger.xtendbeans.AssertBeans;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Tests that the {@link AssertDataObjects} utility also works for any Java
 * object that is not a {@link DataObject}, like the {@link AssertBeans} which
 * it's based on. There is absolutely no particular reason why it wouldn't,
 * because {@link AssertDataObjects} is essentially just a customization of
 * {@link AssertBeans} - this is just to make sure none of the base
 * functionality gets broken in the customization.
 *
 * @author Michael Vorburger
 */
public class AssertNonDataObjectsTest {

    public static class SomeBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testString() {
        AssertDataObjects.assertEqualBeans("hello, world", "hello, world");
    }

    @Test
    public void testSomeBean() {
        SomeBean first = new SomeBean();
        first.setName("hello, world");

        SomeBean second = new SomeBean();
        second.setName("hello, world");

        AssertDataObjects.assertEqualBeans(first, second);
    }

}
