package org.opendaylight.yang.gen.v1.urn.test.mainyangtest;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont;
import org.opendaylight.yangtools.yang.binding.TreeRoot;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * module mainYangTest {
 *     yang-version 1;
 *     namespace "urn:test:mainYangTest";
 *     prefix "myt";
 *
 *     revision 2016-01-01 {
 *         description "";
 *     }
 *
 *     container main-cont {
 *         container main-group-cont {
 *             leaf main-group-cont-leaf {
 *                 type string;
 *             }
 *             list main-group-cont-list-ordered {
 *                 key "name-1"
 *                 leaf name-1 {
 *                     type string;
 *                 }
 *                 leaf type-1 {
 *                     type string;
 *                 }
 *             }
 *             list main-group-cont-list-unordered {
 *                 key "name-2"
 *                 leaf name-2 {
 *                     type string;
 *                 }
 *                 leaf type-2 {
 *                     type string;
 *                 }
 *             }
 *             choice main-group-cont-choice {
 *                 case a {
 *                     leaf case-1 {
 *                         type string;
 *                     }
 *                 }
 *                 case b {
 *                     leaf case2-1 {
 *                         type string;
 *                     }
 *                 }
 *             }
 *             leaf leaf-ref-test {
 *                 type leafref;
 *             }
 *             container second-group-cont {
 *                 leaf second-group-cont-leaf {
 *                     type string;
 *                 }
 *             }
 *             uses second-group;
 *         }
 *         leaf main-augmented-leaf {
 *             type string;
 *         }
 *         leaf imported-augmented-leaf-1 {
 *             type string;
 *         }
 *         leaf imported-augmented-leaf-2 {
 *             type string;
 *         }
 *         augment \(urn:test:mainYangTest)main-cont {
 *             status CURRENT;
 *             leaf main-augmented-leaf {
 *                 type string;
 *             }
 *         }
 *         augment \(urn:test:mainYangTest)main-cont {
 *             status CURRENT;
 *             leaf imported-augmented-leaf-1 {
 *                 type string;
 *             }
 *         }
 *         augment \(urn:test:mainYangTest)main-cont {
 *             status CURRENT;
 *             leaf imported-augmented-leaf-2 {
 *                 type string;
 *             }
 *         }
 *         uses main-group;
 *     }
 *
 *     grouping second-group {
 *         container second-group-cont {
 *             leaf second-group-cont-leaf {
 *                 type string;
 *             }
 *         }
 *     }
 *     grouping main-group {
 *         container main-group-cont {
 *             container second-group-cont {
 *                 leaf second-group-cont-leaf {
 *                     type string;
 *                 }
 *             }
 *             leaf main-group-cont-leaf {
 *                 type string;
 *             }
 *             list main-group-cont-list-ordered {
 *                 key "name-1"
 *                 leaf name-1 {
 *                     type string;
 *                 }
 *                 leaf type-1 {
 *                     type string;
 *                 }
 *             }
 *             list main-group-cont-list-unordered {
 *                 key "name-2"
 *                 leaf name-2 {
 *                     type string;
 *                 }
 *                 leaf type-2 {
 *                     type string;
 *                 }
 *             }
 *             choice main-group-cont-choice {
 *                 case a {
 *                     leaf case-1 {
 *                         type string;
 *                     }
 *                 }
 *                 case b {
 *                     leaf case2-1 {
 *                         type string;
 *                     }
 *                 }
 *             }
 *             leaf leaf-ref-test {
 *                 type leafref;
 *             }
 *             uses second-group;
 *         }
 *     }
 *
 *     augment \(urn:test:mainYangTest)main-cont {
 *         status CURRENT;
 *         leaf main-augmented-leaf {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 *
 */
public interface MainYangTestData
    extends
        TreeRoot
{




    MainCont getMainCont();

}

