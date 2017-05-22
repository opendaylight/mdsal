package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.b.B1;

import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.B.B1;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>uses-augment-recursive</b>
 * <pre>
 * container B1-inner {
 *     leaf leaf-B-B1 {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>uses-augment-recursive/B/B1/B1-inner</i>
 * <p>To create instances of this class use {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.b.B1.B1InnerBuilder}.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.b.B1.B1InnerBuilder
 */
public interface B1Inner extends TreeChildNode<B1,Item<B1>>, Instantiable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.b.B1.B1Inner>, Augmentable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.b.B1.B1Inner> {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:uses-augment-recursive", "2017-05-19", "B1-inner").intern();

    java.lang.String getLeafBB1();

}
