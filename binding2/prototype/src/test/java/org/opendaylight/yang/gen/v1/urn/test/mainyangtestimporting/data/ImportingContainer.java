package org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data;
import org.opendaylight.yangtools.yang.binding.ChildTreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.MainYangTestImportingData;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTestImporting</b>
 * <pre>
 * container importing-container {
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
 *         leaf imported-augmented-leaf-3 {
 *             type string;
 *         }
 *         augment \(urn:test:mainYangTestImporting)importing-container\(urn:test:mainYangTestImporting)main-group-cont {
 *             status CURRENT;
 *             leaf imported-augmented-leaf-3 {
 *                 type string;
 *             }
 *         }
 *         uses second-group;
 *     }
 *     uses main-group;
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTestImporting/importing-container</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainerBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainerBuilder
 *
 */
public interface ImportingContainer
    extends
        ChildTreeNode<MainYangTestImportingData>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.ImportingContainer>,
    MainGroup
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTestImporting",
        "2016-01-01", "importing-container").intern();


}

