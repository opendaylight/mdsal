/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test._2.rev160111.MyDerivedImportedIdentity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BigIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BigIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BigUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BigUintContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BinaryContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BinaryContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BitsContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BitsContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BooleanContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.BooleanContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.DecimalContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.DecimalContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.EnumContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.EnumContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.IdentityrefContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.IdentityrefContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.MyDerivedIdentity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.MyDerivedIdentity2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.NormalIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.NormalIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.NormalUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.NormalUintContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.SmallIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.SmallIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.SmallUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.SmallUintContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.StringContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.StringContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.TinyIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.TinyIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.TinyUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.TinyUintContainerBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LeafDefaultValueTest extends AbstractDataBrokerTest {

    private static final InstanceIdentifier<TinyIntContainer> TINY_INT_NODE_PATH
            = InstanceIdentifier.create(TinyIntContainer.class);
    private static final InstanceIdentifier<SmallIntContainer> SMALL_INT_NODE_PATH
            = InstanceIdentifier.create(SmallIntContainer.class);
    private static final InstanceIdentifier<NormalIntContainer> NORMAL_INT_NODE_PATH
            = InstanceIdentifier.create(NormalIntContainer.class);
    private static final InstanceIdentifier<BigIntContainer> BIG_INT_NODE_PATH
            = InstanceIdentifier.create(BigIntContainer.class);

    private static final InstanceIdentifier<TinyUintContainer> TINY_UINT_NODE_PATH
            = InstanceIdentifier.create(TinyUintContainer.class);
    private static final InstanceIdentifier<SmallUintContainer> SMALL_UINT_NODE_PATH
            = InstanceIdentifier.create(SmallUintContainer.class);
    private static final InstanceIdentifier<NormalUintContainer> NORMAL_UINT_NODE_PATH
            = InstanceIdentifier.create(NormalUintContainer.class);
    private static final InstanceIdentifier<BigUintContainer> BIG_UINT_NODE_PATH
            = InstanceIdentifier.create(BigUintContainer.class);

    private static final InstanceIdentifier<DecimalContainer> DECIMAL_NODE_PATH
            = InstanceIdentifier.create(DecimalContainer.class);

    private static final InstanceIdentifier<StringContainer> STRING_NODE_PATH
            = InstanceIdentifier.create(StringContainer.class);

    private static final InstanceIdentifier<BooleanContainer> BOOLEAN_NODE_PATH
            = InstanceIdentifier.create(BooleanContainer.class);

    private static final InstanceIdentifier<EnumContainer> ENUM_NODE_PATH
            = InstanceIdentifier.create(EnumContainer.class);

    private static final InstanceIdentifier<BitsContainer> BITS_NODE_PATH
            = InstanceIdentifier.create(BitsContainer.class);

    private static final InstanceIdentifier<BinaryContainer> BINARY_NODE_PATH
            = InstanceIdentifier.create(BinaryContainer.class);

    private static final InstanceIdentifier<IdentityrefContainer> IDENTITYREF_NODE_PATH
            = InstanceIdentifier.create(IdentityrefContainer.class);

    @Test
    public void testTinyIntDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TINY_INT_NODE_PATH, new TinyIntContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<TinyIntContainer> tinyIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                TINY_INT_NODE_PATH).get();

        assertTrue(tinyIntContainerNode.isPresent());

        TinyIntContainer tinyIntContainer = tinyIntContainerNode.orElseThrow();
        assertEquals(-18, tinyIntContainer.getTinyIntLeaf().getValue().byteValue());
        assertEquals(-18, tinyIntContainer.getTinyIntLeaf2().getValue().byteValue());
        assertEquals(-15, tinyIntContainer.getTinyIntLeaf3().getValue().byteValue());
        assertEquals(-18, tinyIntContainer.getTinyIntLeaf4().getValue().byteValue());
        assertEquals(-120, tinyIntContainer.getTinyIntLeaf5().byteValue());
        assertEquals(null, tinyIntContainer.getTinyIntLeaf6());
    }

    @Test
    public void testSmallIntDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, SMALL_INT_NODE_PATH, new SmallIntContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<SmallIntContainer> smallIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                SMALL_INT_NODE_PATH).get();

        assertTrue(smallIntContainerNode.isPresent());

        SmallIntContainer smallIntContainer = smallIntContainerNode.orElseThrow();
        assertEquals(-20000, smallIntContainer.getSmallIntLeaf().getValue().shortValue());
        assertEquals(-20000, smallIntContainer.getSmallIntLeaf2().getValue().shortValue());
        assertEquals(-15000, smallIntContainer.getSmallIntLeaf3().getValue().shortValue());
        assertEquals(-20000, smallIntContainer.getSmallIntLeaf4().getValue().shortValue());
        assertEquals(-5000, smallIntContainer.getSmallIntLeaf5().shortValue());
        assertEquals(null, smallIntContainer.getSmallIntLeaf6());
    }

    @Test
    public void testNormalIntDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NORMAL_INT_NODE_PATH, new NormalIntContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<NormalIntContainer> normalIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                NORMAL_INT_NODE_PATH).get();

        assertTrue(normalIntContainerNode.isPresent());

        NormalIntContainer normalIntContainer = normalIntContainerNode.orElseThrow();
        assertEquals(-200000, normalIntContainer.getNormalIntLeaf().getValue().intValue());
        assertEquals(-200000, normalIntContainer.getNormalIntLeaf2().getValue().intValue());
        assertEquals(-130000, normalIntContainer.getNormalIntLeaf3().getValue().intValue());
        assertEquals(-200000, normalIntContainer.getNormalIntLeaf4().getValue().intValue());
        assertEquals(-95000, normalIntContainer.getNormalIntLeaf5().intValue());
        assertEquals(null, normalIntContainer.getNormalIntLeaf6());
    }

    @Test
    public void testBigIntDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BIG_INT_NODE_PATH, new BigIntContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BigIntContainer> bigIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BIG_INT_NODE_PATH).get();

        assertTrue(bigIntContainerNode.isPresent());

        BigIntContainer bigIntContainer = bigIntContainerNode.orElseThrow();
        assertEquals(-3300000000L, bigIntContainer.getBigIntLeaf().getValue().longValue());
        assertEquals(-3300000000L, bigIntContainer.getBigIntLeaf2().getValue().longValue());
        assertEquals(-2800000000L, bigIntContainer.getBigIntLeaf3().getValue().longValue());
        assertEquals(-3300000000L, bigIntContainer.getBigIntLeaf4().getValue().longValue());
        assertEquals(-2500000000L, bigIntContainer.getBigIntLeaf5().longValue());
        assertEquals(null, bigIntContainer.getBigIntLeaf6());
    }

    @Test
    public void testTinyUintDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TINY_UINT_NODE_PATH, new TinyUintContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<TinyUintContainer> tinyUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                TINY_UINT_NODE_PATH).get();

        assertTrue(tinyUintContainerNode.isPresent());

        TinyUintContainer tinyUintContainer = tinyUintContainerNode.orElseThrow();
        assertEquals(150, tinyUintContainer.getTinyUintLeaf().getValue().shortValue());
        assertEquals(150, tinyUintContainer.getTinyUintLeaf2().getValue().shortValue());
        assertEquals(170, tinyUintContainer.getTinyUintLeaf3().getValue().shortValue());
        assertEquals(150, tinyUintContainer.getTinyUintLeaf4().getValue().shortValue());
        assertEquals(155, tinyUintContainer.getTinyUintLeaf5().shortValue());
        assertEquals(null, tinyUintContainer.getTinyUintLeaf6());
    }

    @Test
    public void testSmallUintDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, SMALL_UINT_NODE_PATH, new SmallUintContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<SmallUintContainer> smallUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                SMALL_UINT_NODE_PATH).get();

        assertTrue(smallUintContainerNode.isPresent());

        SmallUintContainer smallUintContainer = smallUintContainerNode.orElseThrow();
        assertEquals(35000, smallUintContainer.getSmallUintLeaf().getValue().intValue());
        assertEquals(35000, smallUintContainer.getSmallUintLeaf2().getValue().intValue());
        assertEquals(45000, smallUintContainer.getSmallUintLeaf3().getValue().intValue());
        assertEquals(35000, smallUintContainer.getSmallUintLeaf4().getValue().intValue());
        assertEquals(62000, smallUintContainer.getSmallUintLeaf5().intValue());
        assertEquals(null, smallUintContainer.getSmallUintLeaf6());
    }

    @Test
    public void testNormalUintDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NORMAL_UINT_NODE_PATH, new NormalUintContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<NormalUintContainer> normalUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                NORMAL_UINT_NODE_PATH).get();

        assertTrue(normalUintContainerNode.isPresent());

        NormalUintContainer normalUintContainer = normalUintContainerNode.orElseThrow();
        assertEquals(100000, normalUintContainer.getNormalUintLeaf().getValue().longValue());
        assertEquals(100000, normalUintContainer.getNormalUintLeaf2().getValue().longValue());
        assertEquals(250000, normalUintContainer.getNormalUintLeaf3().getValue().longValue());
        assertEquals(100000, normalUintContainer.getNormalUintLeaf4().getValue().longValue());
        assertEquals(150000, normalUintContainer.getNormalUintLeaf5().longValue());
        assertEquals(null, normalUintContainer.getNormalUintLeaf6());
    }

    @Test
    public void testBigUintDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BIG_UINT_NODE_PATH, new BigUintContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BigUintContainer> bigUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BIG_UINT_NODE_PATH).get();

        assertTrue(bigUintContainerNode.isPresent());

        BigUintContainer bigUintContainer = bigUintContainerNode.orElseThrow();
        assertEquals(5000000000L, bigUintContainer.getBigUintLeaf().getValue().longValue());
        assertEquals(5000000000L, bigUintContainer.getBigUintLeaf2().getValue().longValue());
        assertEquals(5800000000L, bigUintContainer.getBigUintLeaf3().getValue().longValue());
        assertEquals(5000000000L, bigUintContainer.getBigUintLeaf4().getValue().longValue());
        assertEquals(6500000000L, bigUintContainer.getBigUintLeaf5().longValue());
        assertEquals(null, bigUintContainer.getBigUintLeaf6());
    }

    @Test
    public void testDecimalDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, DECIMAL_NODE_PATH, new DecimalContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<DecimalContainer> decimalContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                DECIMAL_NODE_PATH).get();

        assertTrue(decimalContainerNode.isPresent());

        DecimalContainer decimalCont = decimalContainerNode.orElseThrow();
        assertEquals(66.66, decimalCont.getDecimalLeaf().getValue().doubleValue(), 0.001);
        assertEquals(66.66, decimalCont.getDecimalLeaf2().getValue().doubleValue(), 0.001);
        assertEquals(99.99, decimalCont.getDecimalLeaf3().getValue().doubleValue(), 0.001);
        assertEquals(66.66, decimalCont.getDecimalLeaf4().getValue().doubleValue(), 0.001);
        assertEquals(120.55, decimalCont.getDecimalLeaf5().doubleValue(), 0.001);
        assertEquals(null, decimalCont.getDecimalLeaf6());
    }

    @Test
    public void testStringDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, STRING_NODE_PATH, new StringContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<StringContainer> stringContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                STRING_NODE_PATH).get();

        assertTrue(stringContainerNode.isPresent());

        StringContainer stringCont = stringContainerNode.orElseThrow();
        assertEquals("unspecified string", stringCont.getStringLeaf().getValue());
        assertEquals("unspecified string", stringCont.getStringLeaf2().getValue());
        assertEquals("unknown", stringCont.getStringLeaf3().getValue());
        assertEquals("unspecified string", stringCont.getStringLeaf4().getValue());
        assertEquals("whatever", stringCont.getStringLeaf5());
        assertNull(stringCont.getStringLeaf6());
    }

    @Test
    public void testBooleanDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BOOLEAN_NODE_PATH, new BooleanContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BooleanContainer> booleanContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BOOLEAN_NODE_PATH).get();

        assertTrue(booleanContainerNode.isPresent());

        BooleanContainer boolCont = booleanContainerNode.orElseThrow();
        assertTrue(boolCont.getBooleanLeaf());
        assertNull(boolCont.getBooleanLeaf2());
    }

    @Test
    public void testEnumerationDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, ENUM_NODE_PATH, new EnumContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<EnumContainer> enumContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                ENUM_NODE_PATH).get();

        assertTrue(enumContainerNode.isPresent());

        EnumContainer enumCont = enumContainerNode.orElseThrow();
        assertEquals("Second", enumCont.getEnumLeaf().name());
        assertEquals(2, enumCont.getEnumLeaf().getIntValue());
    }

    @Test
    public void testBitsDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BITS_NODE_PATH, new BitsContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BitsContainer> bitsContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BITS_NODE_PATH).get();

        assertTrue(bitsContainerNode.isPresent());

        BitsContainer bitsCont = bitsContainerNode.orElseThrow();
        assertFalse(bitsCont.getBitsLeaf().getBitZero());
        assertTrue(bitsCont.getBitsLeaf().getBitOne());
        assertFalse(bitsCont.getBitsLeaf().getBitTwo());
        assertNull(bitsCont.getBitsLeaf2());
    }

    @Test
    public void testBinaryDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BINARY_NODE_PATH, new BinaryContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BinaryContainer> binaryContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BINARY_NODE_PATH).get();

        assertTrue(binaryContainerNode.isPresent());

        BinaryContainer binCont = binaryContainerNode.orElseThrow();
        byte [] expectedBytes = {104, 101, 108, 108, 111};
        byte [] actualBytes = binCont.getBinaryLeaf();

        assertTrue(Arrays.equals(expectedBytes, actualBytes));
    }

    @Test
    public void testIdentityrefDefaultValue() throws ExecutionException, InterruptedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, IDENTITYREF_NODE_PATH, new IdentityrefContainerBuilder().build());
        writeTx.commit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<IdentityrefContainer> identityrefContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                IDENTITYREF_NODE_PATH).get();

        assertTrue(identityrefContainerNode.isPresent());

        IdentityrefContainer idrefCont = identityrefContainerNode.orElseThrow();
        assertNull(idrefCont.getIdentityrefLeaf());
        assertSame(MyDerivedIdentity.VALUE, idrefCont.getIdentityrefLeaf2());
        assertSame(MyDerivedIdentity.VALUE, idrefCont.getIdentityrefLeaf3());
        assertSame(MyDerivedIdentity2.VALUE, idrefCont.getIdentityrefLeaf4());
        assertSame(MyDerivedImportedIdentity.VALUE, idrefCont.getIdentityrefLeaf5());
        assertSame(MyDerivedIdentity.VALUE, idrefCont.getIdentityrefLeaf6());
        assertNull(idrefCont.getIdentityrefLeaf7());
    }
}
