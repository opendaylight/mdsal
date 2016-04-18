package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont;
import org.opendaylight.yangtools.yang.binding.ChildTreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.IdentifiableListItem;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * list main-group-cont-list-unordered {
 *     key "name-2"
 *     leaf name-2 {
 *         type string;
 *     }
 *     leaf type-2 {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/main-group/main-group-cont/main-group-cont-list-unordered</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnorderedBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnorderedBuilder
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnorderedKey
 *
 */
public interface MainGroupContListUnordered
    extends
        ChildTreeNode<MainGroupCont>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered>,
        IdentifiableListItem<MainGroupContListUnorderedKey>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "main-group-cont-list-unordered").intern();

    java.lang.String getName2();
    
    java.lang.String getType2();
    
    /**
     * Returns Primary Key of Yang List Type
     *
     */
    MainGroupContListUnorderedKey identifier();

}

