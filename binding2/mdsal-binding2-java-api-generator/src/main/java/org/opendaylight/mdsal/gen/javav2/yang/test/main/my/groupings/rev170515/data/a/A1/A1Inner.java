package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1;

import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>groupings</b>
 * <pre>
 * container A1-inner {
 *     leaf leaf-A-A1 {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>groupings/A/A1/A1-inner</i>
 * <p>To create instances of this class use {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1InnerBuilder}.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1InnerBuilder
 */
public interface A1Inner extends TreeChildNode<A1,Item<A1>>, Instantiable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>, Augmentable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner> {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:groupings", "2017-05-15", "A1-inner").intern();

    java.lang.String getLeafAA1();

}
