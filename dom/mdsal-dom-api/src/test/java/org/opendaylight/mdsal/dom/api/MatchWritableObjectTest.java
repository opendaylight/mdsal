/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class MatchWritableObjectTest {
    @Test
    public void testMatchAll() throws Exception {
        final var matcher = DOMQueryPredicate.Match.exists();
        final var matcher1 = DOMQueryPredicate.Match.greaterThan(5L);
        final var matcher2 = DOMQueryPredicate.Match.greaterThanOrEqual((short) 5);
        final var matcher3 = DOMQueryPredicate.Match.valueEquals(true);
        final var matcher4 = DOMQueryPredicate.Match.lessThanOrEqual(0x7F);
        final var matcher5 = DOMQueryPredicate.Match.lessThanOrEqual(QName.create("urn:test", "2017-01-01", "cont"));
        final var matcher6 = DOMQueryPredicate.Match.valueEquals(Empty.value());
        final var matcher7 = DOMQueryPredicate.Match.lessThanOrEqual(Decimal64.minValueIn(2));
        final var matcher8 = DOMQueryPredicate.Match.lessThanOrEqual(Uint16.ZERO);
        final var matcher9 = DOMQueryPredicate.Match.greaterThan(true);

        final var matchAll = matcher1.and(matcher).and(matcher2).and(matcher3).and(matcher4).and(matcher5).and(matcher6)
            .and(matcher7).and(matcher8).and(matcher9);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var outputStream = new DataOutputStream(byteArrayOutputStream);

        matchAll.writeTo(outputStream);

        outputStream.flush();
        outputStream.close();

        final var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final var inputStream = new DataInputStream(byteArrayInputStream);

        final var matcherDe = DOMQueryPredicate.Match.readFrom(inputStream);
        final var match = "allOf(gt(5), exists(), gte(5), eq(true), lte(127), lte((urn:test?revision=2017-01-01)cont),"
            + " eq(empty), lte(-92233720368547758.08), lte(0), gt(true))";
        assertEquals(match, matcherDe.toString());
    }

    @Test
    public void testMatchAny() throws Exception {
        final var matcher = DOMQueryPredicate.Match.valueEquals(Uint16.ONE);
        final var matcher1 = DOMQueryPredicate.Match.stringMatches(Pattern.compile("AA"));
        final var matcher2 = DOMQueryPredicate.Match.stringContains("BB");
        final var matcher3 = DOMQueryPredicate.Match.stringEndsWith("CC");
        final var matcher4 = DOMQueryPredicate.Match.stringStartsWith("DD");
        final var matcher5 = matcher2.negate();
        final var matchAny = matcher1.or(matcher2).or(matcher3).or(matcher4).or(matcher).or(matcher5);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var outputStream = new DataOutputStream(byteArrayOutputStream);

        matchAny.writeTo(outputStream);

        outputStream.flush();
        outputStream.close();

        final var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final var inputStream = new DataInputStream(byteArrayInputStream);

        final var matcherDe = DOMQueryPredicate.Match.readFrom(inputStream);
        final var match = "anyOf(matches(AA), contains(BB), endsWith(CC), startsWith(DD), eq(1), not(contains(BB)))";
        assertEquals(match, matcherDe.toString());
    }

    @Test
    public void testYangInstanceIdentifier() throws Exception {
        final ImmutableList.Builder<PathArgument> builder = ImmutableList.builderWithExpectedSize(3);
        builder.add(NodeIdentifier.create(QName.create("namespace", "aaa")));

        final ImmutableMap.Builder<QName, Object> builder1 = ImmutableMap.builderWithExpectedSize(2);
        builder1.put(QName.create("namespace", "bbb-1"), "value-b-1");
        builder1.put(QName.create("namespace", "bbb-2"), "value-b-2");
        builder.add(NodeIdentifierWithPredicates.of(QName.create("namespace", "bbb"), builder1.build()));

        builder.add(new YangInstanceIdentifier.NodeWithValue<>(QName.create("namespace", "ccc"), Uint16.ONE));

        var yangId1 = YangInstanceIdentifier.of();
        var yangId2 = YangInstanceIdentifier.of(builder.build());
        final var matcher1 = DOMQueryPredicate.Match.valueEquals(yangId1);
        final var matcher2 = DOMQueryPredicate.Match.valueEquals(yangId2);
        final var matchAny = matcher1.or(matcher2);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var outputStream = new DataOutputStream(byteArrayOutputStream);

        matchAny.writeTo(outputStream);

        outputStream.flush();
        outputStream.close();

        final var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final var inputStream = new DataInputStream(byteArrayInputStream);

        final var matcherDe = DOMQueryPredicate.Match.readFrom(inputStream);
        final var match = "anyOf(eq(/), eq(/(namespace)aaa/bbb[{(namespace)bbb-1=value-b-1,"
                + " (namespace)bbb-2=value-b-2}]/ccc[1]))";
        assertEquals(match, matcherDe.toString());
    }


}
