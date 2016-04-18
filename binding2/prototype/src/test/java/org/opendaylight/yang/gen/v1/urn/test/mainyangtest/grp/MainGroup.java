package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * grouping main-group {
 *     container main-group-cont {
 *         container second-group-cont {
 *             leaf second-group-cont-leaf {
 *                 type string;
 *             }
 *         }
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
 *         uses second-group;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/main-group</i>
 *
 */
public interface MainGroup
    extends
        TreeNode
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "main-group").intern();

    MainGroupCont getMainGroupCont();

}

