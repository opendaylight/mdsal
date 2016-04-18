package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.second.group.SecondGroupCont;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * grouping second-group {
 *     container second-group-cont {
 *         leaf second-group-cont-leaf {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/second-group</i>
 *
 */
public interface SecondGroup
    extends
        TreeNode
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "second-group").intern();

    SecondGroupCont getSecondGroupCont();

}

