/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.yangtools.yang.common.Uint16;

public class MatchWritableObjectTest {
    @Test
    public void testFindCriticalAlarms() throws Exception {
        final var matcher = DOMQueryPredicate.Match.exists();
        final var matcher1 = DOMQueryPredicate.Match.greaterThan(Uint16.ONE);
        final var matcher2 = DOMQueryPredicate.Match.greaterThanOrEqual(Uint16.TWO);
        final var matcher3 = DOMQueryPredicate.Match.lessThan(Uint16.TEN);
        final var matcher4 = DOMQueryPredicate.Match.lessThanOrEqual(Uint16.ZERO);
        final var matchAll = matcher1.and(matcher2).and(matcher3).and(matcher4).and(matcher);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        matchAll.writeTo(outputStream);

        outputStream.flush();
        outputStream.close();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final DataInputStream inputStream = new DataInputStream(byteArrayInputStream);

        final var matcherDe = DOMQueryPredicate.Match.readFrom(inputStream);
        final String match = "allOf(gt(1), gte(2), lt(10), lte(0), exists())";
        assertEquals(matcherDe.toString(), match);
    }

    @Test
    public void testFindCriticalAlarms1() throws Exception {
        final var matcher = DOMQueryPredicate.Match.valueEquals(Uint16.ONE);
        final var matcher1 = DOMQueryPredicate.Match.stringMatches(Pattern.compile("AA"));
        final var matcher2 = DOMQueryPredicate.Match.stringContains("BB");
        final var matcher3 = DOMQueryPredicate.Match.stringEndsWith("CC");
        final var matcher4 = DOMQueryPredicate.Match.stringStartsWith("DD");
        final var matcher5 = matcher2.negate();
        final var matchAny = matcher1.or(matcher2).or(matcher3).or(matcher4).or(matcher).or(matcher5);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        matchAny.writeTo(outputStream);

        outputStream.flush();
        outputStream.close();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final DataInputStream inputStream = new DataInputStream(byteArrayInputStream);

        final var matcherDe = DOMQueryPredicate.Match.readFrom(inputStream);
        final var match = "anyOf(matches(AA), contains(BB), endsWith(CC), startsWith(DD), eq(1), not(contains(BB)))";
        assertEquals(matcherDe.toString(), match);
    }

}
