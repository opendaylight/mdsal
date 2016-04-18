package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContChoice;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * case b {
 *     leaf case2-1 {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/main-group/main-group-cont/main-group-cont-choice/b</i>
 *
 */
public interface B
    extends
        TreeNode,
    Augmentable<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>,
    MainGroupContChoice
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "b").intern();

    java.lang.String getCase21();

}

