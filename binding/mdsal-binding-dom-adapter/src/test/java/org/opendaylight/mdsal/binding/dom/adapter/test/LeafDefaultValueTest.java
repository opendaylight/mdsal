/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BigIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BigIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BigUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BigUintContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BinaryContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BinaryContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BitsContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BitsContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BooleanContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.BooleanContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.DecimalContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.DecimalContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.EnumContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.EnumContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.IdentityrefContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.IdentityrefContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.NormalIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.NormalIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.NormalUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.NormalUintContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.SmallIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.SmallIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.SmallUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.SmallUintContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.StringContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.StringContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.TinyIntContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.TinyIntContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.TinyUintContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.rev700101.TinyUintContainerBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LeafDefaultValueTest extends AbstractDataBrokerTest {

    private static final InstanceIdentifier<TinyIntContainer> TINY_INT_NODE_PATH = InstanceIdentifier.create
            (TinyIntContainer.class);
    private static final InstanceIdentifier<SmallIntContainer> SMALL_INT_NODE_PATH = InstanceIdentifier.create
            (SmallIntContainer.class);
    private static final InstanceIdentifier<NormalIntContainer> NORMAL_INT_NODE_PATH = InstanceIdentifier.create
            (NormalIntContainer.class);
    private static final InstanceIdentifier<BigIntContainer> BIG_INT_NODE_PATH = InstanceIdentifier.create
            (BigIntContainer.class);

    private static final InstanceIdentifier<TinyUintContainer> TINY_UINT_NODE_PATH = InstanceIdentifier.create
            (TinyUintContainer.class);
    private static final InstanceIdentifier<SmallUintContainer> SMALL_UINT_NODE_PATH = InstanceIdentifier.create
            (SmallUintContainer.class);
    private static final InstanceIdentifier<NormalUintContainer> NORMAL_UINT_NODE_PATH = InstanceIdentifier.create
            (NormalUintContainer.class);
    private static final InstanceIdentifier<BigUintContainer> BIG_UINT_NODE_PATH = InstanceIdentifier.create
            (BigUintContainer.class);

    private static final InstanceIdentifier<DecimalContainer> DECIMAL_NODE_PATH = InstanceIdentifier.create
            (DecimalContainer.class);

    private static final InstanceIdentifier<StringContainer> STRING_NODE_PATH = InstanceIdentifier.create
            (StringContainer.class);

    private static final InstanceIdentifier<BooleanContainer> BOOLEAN_NODE_PATH = InstanceIdentifier.create
            (BooleanContainer.class);

    private static final InstanceIdentifier<EnumContainer> ENUM_NODE_PATH = InstanceIdentifier.create(EnumContainer
            .class);

    private static final InstanceIdentifier<BitsContainer> BITS_NODE_PATH = InstanceIdentifier.create(BitsContainer
            .class);

    private static final InstanceIdentifier<BinaryContainer> BINARY_NODE_PATH = InstanceIdentifier.create
            (BinaryContainer.class);

    private static final InstanceIdentifier<IdentityrefContainer> IDENTITYREF_NODE_PATH = InstanceIdentifier.create
            (IdentityrefContainer.class);

    @Test
    public void testTinyIntDefaultValue() throws ExecutionException, InterruptedException, TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TINY_INT_NODE_PATH, new TinyIntContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<TinyIntContainer> tinyIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                TINY_INT_NODE_PATH).get();

        assertTrue(tinyIntContainerNode.isPresent());

        TinyIntContainer tIntCont = tinyIntContainerNode.get();
        assertEquals(-18, tIntCont.getTinyIntLeaf().getValue().byteValue());
        assertEquals(-18, tIntCont.getTinyIntLeaf2().getValue().byteValue());
        assertEquals(-15, tIntCont.getTinyIntLeaf3().getValue().byteValue());
        assertEquals(-18, tIntCont.getTinyIntLeaf4().getValue().byteValue());
        assertEquals(-120, tIntCont.getTinyIntLeaf5().byteValue());
        assertEquals(null, tIntCont.getTinyIntLeaf6());
    }

    @Test
    public void testSmallIntDefaultValue() throws ExecutionException, InterruptedException, TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, SMALL_INT_NODE_PATH, new SmallIntContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<SmallIntContainer> smallIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                SMALL_INT_NODE_PATH).get();

        assertTrue(smallIntContainerNode.isPresent());

        SmallIntContainer sIntCont = smallIntContainerNode.get();
        assertEquals(-20000, sIntCont.getSmallIntLeaf().getValue().shortValue());
        assertEquals(-20000, sIntCont.getSmallIntLeaf2().getValue().shortValue());
        assertEquals(-15000, sIntCont.getSmallIntLeaf3().getValue().shortValue());
        assertEquals(-20000, sIntCont.getSmallIntLeaf4().getValue().shortValue());
        assertEquals(-5000, sIntCont.getSmallIntLeaf5().shortValue());
        assertEquals(null, sIntCont.getSmallIntLeaf6());
    }

    @Test
    public void testNormalIntDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NORMAL_INT_NODE_PATH, new NormalIntContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<NormalIntContainer> normalIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                NORMAL_INT_NODE_PATH).get();

        assertTrue(normalIntContainerNode.isPresent());

        NormalIntContainer nIntCont = normalIntContainerNode.get();
        assertEquals(-200000, nIntCont.getNormalIntLeaf().getValue().intValue());
        assertEquals(-200000, nIntCont.getNormalIntLeaf2().getValue().intValue());
        assertEquals(-130000, nIntCont.getNormalIntLeaf3().getValue().intValue());
        assertEquals(-200000, nIntCont.getNormalIntLeaf4().getValue().intValue());
        assertEquals(-95000, nIntCont.getNormalIntLeaf5().intValue());
        assertEquals(null, nIntCont.getNormalIntLeaf6());
    }

    @Test
    public void testBigIntDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BIG_INT_NODE_PATH, new BigIntContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BigIntContainer> bigIntContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BIG_INT_NODE_PATH).get();

        assertTrue(bigIntContainerNode.isPresent());

        BigIntContainer bIntCont = bigIntContainerNode.get();
        assertEquals(-3300000000L, bIntCont.getBigIntLeaf().getValue().longValue());
        assertEquals(-3300000000L, bIntCont.getBigIntLeaf2().getValue().longValue());
        assertEquals(-2800000000L, bIntCont.getBigIntLeaf3().getValue().longValue());
        assertEquals(-3300000000L, bIntCont.getBigIntLeaf4().getValue().longValue());
        assertEquals(-2500000000L, bIntCont.getBigIntLeaf5().longValue());
        assertEquals(null, bIntCont.getBigIntLeaf6());
    }

    @Test
    public void testTinyUintDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TINY_UINT_NODE_PATH, new TinyUintContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<TinyUintContainer> tinyUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                TINY_UINT_NODE_PATH).get();

        assertTrue(tinyUintContainerNode.isPresent());

        TinyUintContainer tUintCont = tinyUintContainerNode.get();
        assertEquals(150, tUintCont.getTinyUintLeaf().getValue().shortValue());
        assertEquals(150, tUintCont.getTinyUintLeaf2().getValue().shortValue());
        assertEquals(170, tUintCont.getTinyUintLeaf3().getValue().shortValue());
        assertEquals(150, tUintCont.getTinyUintLeaf4().getValue().shortValue());
        assertEquals(155, tUintCont.getTinyUintLeaf5().shortValue());
        assertEquals(null, tUintCont.getTinyUintLeaf6());
    }

    @Test
    public void testSmallUintDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, SMALL_UINT_NODE_PATH, new SmallUintContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<SmallUintContainer> smallUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                SMALL_UINT_NODE_PATH).get();

        assertTrue(smallUintContainerNode.isPresent());

        SmallUintContainer sUintCont = smallUintContainerNode.get();
        assertEquals(35000, sUintCont.getSmallUintLeaf().getValue().intValue());
        assertEquals(35000, sUintCont.getSmallUintLeaf2().getValue().intValue());
        assertEquals(45000, sUintCont.getSmallUintLeaf3().getValue().intValue());
        assertEquals(35000, sUintCont.getSmallUintLeaf4().getValue().intValue());
        assertEquals(62000, sUintCont.getSmallUintLeaf5().intValue());
        assertEquals(null, sUintCont.getSmallUintLeaf6());
    }

    @Test
    public void testNormalUintDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NORMAL_UINT_NODE_PATH, new NormalUintContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<NormalUintContainer> normalUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                NORMAL_UINT_NODE_PATH).get();

        assertTrue(normalUintContainerNode.isPresent());

        NormalUintContainer nUintCont = normalUintContainerNode.get();
        assertEquals(100000, nUintCont.getNormalUintLeaf().getValue().longValue());
        assertEquals(100000, nUintCont.getNormalUintLeaf2().getValue().longValue());
        assertEquals(250000, nUintCont.getNormalUintLeaf3().getValue().longValue());
        assertEquals(100000, nUintCont.getNormalUintLeaf4().getValue().longValue());
        assertEquals(150000, nUintCont.getNormalUintLeaf5().longValue());
        assertEquals(null, nUintCont.getNormalUintLeaf6());
    }

    @Test
    public void testBigUintDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BIG_UINT_NODE_PATH, new BigUintContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BigUintContainer> bigUintContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BIG_UINT_NODE_PATH).get();

        assertTrue(bigUintContainerNode.isPresent());

        BigUintContainer bUintCont = bigUintContainerNode.get();
        assertEquals(5000000000L, bUintCont.getBigUintLeaf().getValue().longValue());
        assertEquals(5000000000L, bUintCont.getBigUintLeaf2().getValue().longValue());
        assertEquals(5800000000L, bUintCont.getBigUintLeaf3().getValue().longValue());
        assertEquals(5000000000L, bUintCont.getBigUintLeaf4().getValue().longValue());
        assertEquals(6500000000L, bUintCont.getBigUintLeaf5().longValue());
        assertEquals(null, bUintCont.getBigUintLeaf6());
    }

    @Test
    public void testDecimalDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, DECIMAL_NODE_PATH, new DecimalContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<DecimalContainer> decimalContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                DECIMAL_NODE_PATH).get();

        assertTrue(decimalContainerNode.isPresent());

        DecimalContainer decimalCont = decimalContainerNode.get();
        assertEquals(66.66, decimalCont.getDecimalLeaf().getValue().doubleValue(), 0.001);
        assertEquals(66.66, decimalCont.getDecimalLeaf2().getValue().doubleValue(), 0.001);
        assertEquals(99.9, decimalCont.getDecimalLeaf3().getValue().doubleValue(), 0.01);
        assertEquals(66.66, decimalCont.getDecimalLeaf4().getValue().doubleValue(), 0.001);
        assertEquals(120.55, decimalCont.getDecimalLeaf5().doubleValue(), 0.001);
        assertEquals(null, decimalCont.getDecimalLeaf6());
    }

    @Test
    public void testStringDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, STRING_NODE_PATH, new StringContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<StringContainer> stringContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                STRING_NODE_PATH).get();

        assertTrue(stringContainerNode.isPresent());

        StringContainer stringCont = stringContainerNode.get();
        assertEquals("unspecified string", stringCont.getStringLeaf().getValue());
        assertEquals("unspecified string", stringCont.getStringLeaf2().getValue());
        assertEquals("unknown", stringCont.getStringLeaf3().getValue());
        assertEquals("unspecified string", stringCont.getStringLeaf4().getValue());
        assertEquals("whatever", stringCont.getStringLeaf5());
        assertNull(stringCont.getStringLeaf6());
    }

    @Test
    public void testBooleanDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BOOLEAN_NODE_PATH, new BooleanContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BooleanContainer> booleanContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BOOLEAN_NODE_PATH).get();

        assertTrue(booleanContainerNode.isPresent());

        BooleanContainer boolCont = booleanContainerNode.get();
        assertTrue(boolCont.isBooleanLeaf());
        assertNull(boolCont.isBooleanLeaf2());
    }

    @Test
    public void testEnumerationDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, ENUM_NODE_PATH, new EnumContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<EnumContainer> enumContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                ENUM_NODE_PATH).get();

        assertTrue(enumContainerNode.isPresent());

        EnumContainer enumCont = enumContainerNode.get();
        assertEquals("Second", enumCont.getEnumLeaf().name());
        assertEquals(2, enumCont.getEnumLeaf().getIntValue());
    }

    @Test
    public void testBitsDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BITS_NODE_PATH, new BitsContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BitsContainer> bitsContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BITS_NODE_PATH).get();

        assertTrue(bitsContainerNode.isPresent());

        BitsContainer bitsCont = bitsContainerNode.get();
        assertFalse(bitsCont.getBitsLeaf().isBitZero());
        assertTrue(bitsCont.getBitsLeaf().isBitOne());
        assertFalse(bitsCont.getBitsLeaf().isBitTwo());
        assertNull(bitsCont.getBitsLeaf2());
    }

    @Test
    public void testBinaryDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, BINARY_NODE_PATH, new BinaryContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<BinaryContainer> binaryContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                BINARY_NODE_PATH).get();

        assertTrue(binaryContainerNode.isPresent());

        BinaryContainer binCont = binaryContainerNode.get();
        byte [] expectedBytes = {104, 101, 108, 108, 111};
        byte [] actualBytes = binCont.getBinaryLeaf();

        assertTrue(Arrays.equals(expectedBytes, actualBytes));
    }

    @Test
    public void testIdentityrefDefaultValue() throws ExecutionException, InterruptedException,
            TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, IDENTITYREF_NODE_PATH, new IdentityrefContainerBuilder().build());
        writeTx.submit().checkedGet();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<IdentityrefContainer> identityrefContainerNode = readTx.read(LogicalDatastoreType.OPERATIONAL,
                IDENTITYREF_NODE_PATH).get();

        assertTrue(identityrefContainerNode.isPresent());

        IdentityrefContainer idrefCont = identityrefContainerNode.get();
        assertNull(idrefCont.getIdentityrefLeaf());
        assertEquals("MyDerivedIdentity", idrefCont.getIdentityrefLeaf2().getSimpleName());
        assertEquals("MyDerivedIdentity", idrefCont.getIdentityrefLeaf3().getSimpleName());
        assertEquals("MyDerivedIdentity2", idrefCont.getIdentityrefLeaf4().getSimpleName());
    }
}
