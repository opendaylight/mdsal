/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import ch.vorburger.xtendbeans.AssertBeans;
import org.junit.ComparisonFailure;
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
@Deprecated(since = "8.0.9", forRemoval = true)
public class AssertNonDataObjectsTest {

    public static class SomeBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            SomeBean someBean = (SomeBean) obj;

            return name != null ? name.equals(someBean.name) : someBean.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
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

    @Test
    public void testSomeBeanMismatch() {
        SomeBean expected = new SomeBean();
        expected.setName("hello, world 1");

        SomeBean actual = new SomeBean();
        actual.setName("hello, world 2");

        ComparisonFailure ex = assertThrows(ComparisonFailure.class,
            () -> AssertDataObjects.assertEqualBeans(expected, actual));
        assertThat(ex.getMessage(), startsWith("Expected and actual beans do not match"));
        assertThat(ex.getExpected(), containsString("hello, world 1"));
        assertThat(ex.getActual(), containsString("hello, world 2"));
    }
}
