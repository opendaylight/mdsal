package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A;

import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp.A;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>groupings</b>
 * <pre>
 * container A1 {
 *     leaf leaf-A-A1 {
 *         type string;
 *     }
 *     container A1-inner {
 *         leaf leaf-A-A1 {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>groupings/A/A1</i>
 * <p>To create instances of this class use {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1Builder}.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1Builder
 */
public interface A1 extends TreeChildNode<A,Item<A>>, Instantiable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1>, Augmentable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1> {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:groupings", "2017-05-15", "A1").intern();

    java.lang.String getLeafAA1();

    A1Inner getA1Inner();

}
