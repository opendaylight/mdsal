/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import ch.vorburger.xtendbeans.AssertBeans;
import org.junit.ComparisonFailure;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assertion utilities for YANG {@link DataObject}s.
 *
 * <p>This compares two {@link DataObject}s by creating a textual representation of them,
 * and comparing them.  If they are not equals, then the thrown ComparisonFailure provides
 * for a very highly readable comparison due to a syntax which immediately makes the difference obvious.
 *
 * <p>The syntax used happens to be valid Xtend code, and as such could be directly copy/pasted
 * into an *.xtend source file of an expected object definition.  This is optional though; this
 * utility can very well be used with any object, not necessarily created by Xtend source code.
 *
 * <p>This also works for any Java object that is not a {@link DataObject},
 * like the {@link AssertBeans} which this is based upon.
 *
 * @see AssertBeans for more background
 *
 * @author Michael Vorburger
 */
public final class AssertDataObjects {

    private static final Logger LOG = LoggerFactory.getLogger(AssertDataObjects.class);

    private static final XtendYangBeanGenerator GENERATOR = new XtendYangBeanGenerator();

    private AssertDataObjects() {
    }

    /**
     * Assert that an actual YANG DataObject (DataContainer) is equals to an expected one.
     *
     * <p>The argument types are intentionally of type Object instead of YANG DataContainer or DataObject.
     * This is important so that this can be directly used on e.g. a List or Map etc. of DataObjects.
     *
     * @param expected the expected object
     * @param actual the actual object to check against <code>expected</code>
     *
     * @see AssertBeans#assertEqualBeans(Object, Object)
     */
    public static void assertEqualBeans(Object expected, Object actual) throws ComparisonFailure {
        String expectedText = GENERATOR.getExpression(expected);
        assertEqualByText(expectedText, actual);
    }

    // package local method used only in the self tests of this utility (not intended for usage by client code)
    static void assertEqualByText(String expectedText, Object actual) throws ComparisonFailure {
        String actualText = GENERATOR.getExpression(actual);
        if (!expectedText.equals(actualText)) {
            String diff = DiffUtil.diff(expectedText, actualText);
            LOG.warn("diff for ComparisonFailure about to be thrown:\n" + diff);
            throw new ComparisonFailure("Expected and actual beans do not match", expectedText, actualText);
        }
    }

}
