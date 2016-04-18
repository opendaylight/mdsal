package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group;
import org.opendaylight.yangtools.yang.binding.ChildTreeNode;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.SecondGroup;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group.main.group.cont.MainGroupContListUnordered;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group.main.group.cont.MainGroupContListOrdered;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.MainGroup;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group.main.group.cont.MainGroupContChoice;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * container main-group-cont {
 *     container second-group-cont {
 *         leaf second-group-cont-leaf {
 *             type string;
 *         }
 *     }
 *     leaf main-group-cont-leaf {
 *         type string;
 *     }
 *     list main-group-cont-list-ordered {
 *         key "name-1"
 *         leaf name-1 {
 *             type string;
 *         }
 *         leaf type-1 {
 *             type string;
 *         }
 *     }
 *     list main-group-cont-list-unordered {
 *         key "name-2"
 *         leaf name-2 {
 *             type string;
 *         }
 *         leaf type-2 {
 *             type string;
 *         }
 *     }
 *     choice main-group-cont-choice {
 *         case a {
 *             leaf case-1 {
 *                 type string;
 *             }
 *         }
 *         case b {
 *             leaf case2-1 {
 *                 type string;
 *             }
 *         }
 *     }
 *     leaf leaf-ref-test {
 *         type leafref;
 *     }
 *     uses second-group;
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/main-group/main-group-cont</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group.MainGroupContBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group.MainGroupContBuilder
 *
 */
public interface MainGroupCont
    extends
        ChildTreeNode<MainGroup>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grouping.main.group.MainGroupCont>,
    SecondGroup
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "main-group-cont").intern();

    java.lang.String getMainGroupContLeaf();
    
    List<MainGroupContListOrdered> getMainGroupContListOrdered();
    
    List<MainGroupContListUnordered> getMainGroupContListUnordered();
    
    MainGroupContChoice getMainGroupContChoice();
    
    java.lang.String getLeafRefTest();

}

