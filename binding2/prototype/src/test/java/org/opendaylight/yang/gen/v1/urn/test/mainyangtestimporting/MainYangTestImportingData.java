package org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer;
import org.opendaylight.yangtools.yang.binding.TreeRoot;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTestImporting</b>
 * <pre>
 * module mainYangTestImporting {
 *     yang-version 1;
 *     namespace "urn:test:mainYangTestImporting";
 *     prefix "myti";
 *
 *     import mainYangTest { prefix "imp"; }
 *     revision 2016-01-01 {
 *         description "";
 *     }
 *
 *     container importing-container {
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
 *             leaf imported-augmented-leaf-3 {
 *                 type string;
 *             }
 *             augment \(urn:test:mainYangTestImporting)importing-container\(urn:test:mainYangTestImporting)main-group-cont {
 *                 status CURRENT;
 *                 leaf imported-augmented-leaf-3 {
 *                     type string;
 *                 }
 *             }
 *             uses second-group;
 *         }
 *         uses main-group;
 *     }
 *
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
 *     augment \(urn:test:mainYangTestImporting)importing-container\(urn:test:mainYangTestImporting)main-group-cont {
 *         status CURRENT;
 *         leaf imported-augmented-leaf-3 {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 *
 */
public interface MainYangTestImportingData
    extends
        TreeRoot
{




    ImportingContainer getImportingContainer();

}

