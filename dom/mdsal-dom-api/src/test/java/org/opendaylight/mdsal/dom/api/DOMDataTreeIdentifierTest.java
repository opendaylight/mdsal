package org.opendaylight.mdsal.dom.api;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * Created by peter.nosal
 */
public class DOMDataTreeIdentifierTest {
    private static final QNameModule TEST_MODULE = QNameModule.create(URI.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store"), null);

    private static final String REF_LISTS = "ref-lists";
    private static final String TEST_LISTS = "test-lists";
    private static final String COMPARE_FIRST_LISTS = "A-test-lists";
    private static final String COMPARE_SECOND_LISTS = "B-test-lists";

    private static final YangInstanceIdentifier REF_YII_IID = YangInstanceIdentifier.create(
            new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, REF_LISTS)));
    private static final YangInstanceIdentifier TEST_YII_IID = YangInstanceIdentifier.create(
            new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, TEST_LISTS)));

    private static final DOMDataTreeIdentifier REF_TREE =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID);
    private static final DOMDataTreeIdentifier TEST_DIFF_TREE =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TEST_YII_IID);


    @Test
    public void hashCodeTest(){
        assertEquals("hashCode",
                REF_TREE.hashCode(), new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID).hashCode());

        assertNotEquals("hashCode", REF_TREE.hashCode(), TEST_DIFF_TREE.hashCode());
    }

    @Test
    public void equalsTest(){
        assertTrue("Equals same",
                REF_TREE.equals(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)));

        assertEquals("Different", false, REF_TREE.equals(TEST_DIFF_TREE));
    }

    @Test
    public void compareToTest(){
        final YangInstanceIdentifier COMPARE_FIRST_IID = YangInstanceIdentifier.create(
                new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, COMPARE_FIRST_LISTS)));
        final YangInstanceIdentifier COMPARE_SECOND_IID = YangInstanceIdentifier.create(
                new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, COMPARE_SECOND_LISTS)));

        assertEquals("Compare same to same",
                REF_TREE.compareTo(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)), 0);

        assertTrue("Compare first to second",
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_FIRST_IID).compareTo(
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_SECOND_IID)) < 0);

        assertTrue("Compare second to first",
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_SECOND_IID).compareTo(
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_FIRST_IID)) > 0);
    }

    @Test
    public void containsTest(){
        assertTrue("Contains",
                REF_TREE.contains(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)));

        assertEquals("Not contains", false, REF_TREE.contains(TEST_DIFF_TREE));
    }

    @Test
    public void toStringTest(){
        assertTrue("ToString",  REF_TREE.toString().contains(REF_TREE.getRootIdentifier().toString())
                            &&  REF_TREE.toString().contains(REF_TREE.getDatastoreType().toString()));
    }
}