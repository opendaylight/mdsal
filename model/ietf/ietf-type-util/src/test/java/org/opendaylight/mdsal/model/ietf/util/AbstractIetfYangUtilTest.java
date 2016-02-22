/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import org.junit.Test;

public class AbstractIetfYangUtilTest {
    public static final class MacClass {
        final String _value;

        public MacClass(final String value) {
            this._value = Preconditions.checkNotNull(value);
        }

        public MacClass(final MacClass template) {
            this._value = template._value;
        }
    }

    private static final class MacUtil extends AbstractIetfYangUtil<MacClass> {
        MacUtil() {
            super(MacClass.class);
        }

        @Override
        protected String getValue(final MacClass macAddress) {
            return macAddress._value;
        }
    }

    private static final MacUtil UTIL = new MacUtil();
    private static final byte[] BYTES = new byte[] { 1, 2, 30, 90, -5, -120 };
    private static final String CANON = "01:02:1e:5a:fb:88";

    @Test
    public void testBytesToMac() {
        final MacClass mac = UTIL.macAddressFor(BYTES);
        assertEquals(CANON, mac._value);
    }

    @Test
    public void testMacToBytes() {
        final byte[] bytes1 = UTIL.bytesFor(new MacClass(CANON));
        assertTrue(Arrays.equals(BYTES, bytes1));

        final byte[] bytes2 = UTIL.bytesFor(new MacClass("01:02:1E:5a:Fb:88"));
        assertTrue(Arrays.equals(BYTES, bytes2));
    }
}
