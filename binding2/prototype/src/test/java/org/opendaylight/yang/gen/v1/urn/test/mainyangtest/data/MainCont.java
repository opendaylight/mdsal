package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data;
import org.opendaylight.yangtools.yang.binding.ChildTreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.MainYangTestData;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * container main-cont {
 *     container main-group-cont {
 *         leaf main-group-cont-leaf {
 *             type string;
 *         }
 *         list main-group-cont-list-ordered {
 *             key "name-1"
 *             leaf name-1 {
 *                 type string;
 *             }
 *             leaf type-1 {
 *                 type string;
 *             }
 *         }
 *         list main-group-cont-list-unordered {
 *             key "name-2"
 *             leaf name-2 {
 *                 type string;
 *             }
 *             leaf type-2 {
 *                 type string;
 *             }
 *         }
 *         choice main-group-cont-choice {
 *             case a {
 *                 leaf case-1 {
 *                     type string;
 *                 }
 *             }
 *             case b {
 *                 leaf case2-1 {
 *                     type string;
 *                 }
 *             }
 *         }
 *         leaf leaf-ref-test {
 *             type leafref;
 *         }
 *         container second-group-cont {
 *             leaf second-group-cont-leaf {
 *                 type string;
 *             }
 *         }
 *         uses second-group;
 *     }
 *     leaf main-augmented-leaf {
 *         type string;
 *     }
 *     leaf imported-augmented-leaf-1 {
 *         type string;
 *     }
 *     leaf imported-augmented-leaf-2 {
 *         type string;
 *     }
 *     augment \(urn:test:mainYangTest)main-cont {
 *         status CURRENT;
 *         leaf main-augmented-leaf {
 *             type string;
 *         }
 *     }
 *     augment \(urn:test:mainYangTest)main-cont {
 *         status CURRENT;
 *         leaf imported-augmented-leaf-1 {
 *             type string;
 *         }
 *     }
 *     augment \(urn:test:mainYangTest)main-cont {
 *         status CURRENT;
 *         leaf imported-augmented-leaf-2 {
 *             type string;
 *         }
 *     }
 *     uses main-group;
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/main-cont</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainContBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainContBuilder
 *
 */
public interface MainCont
    extends
        ChildTreeNode<MainYangTestData>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>,
    MainGroup
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "main-cont").intern();


}

