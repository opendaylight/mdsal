package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group;
import org.opendaylight.yangtools.yang.binding.ChildTreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.SecondGroup;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * container second-group-cont {
 *     leaf second-group-cont-leaf {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/second-group/second-group-cont</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupContBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupContBuilder
 *
 */
public interface SecondGroupCont
    extends
        ChildTreeNode<SecondGroup>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "second-group-cont").intern();

    java.lang.String getSecondGroupContLeaf();

}

