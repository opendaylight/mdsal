package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1;

import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>uses-augment-recursive</b>
 * <pre>
 * container C1-inner {
 *     leaf leaf-C-C1 {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>uses-augment-recursive/C/C1/C1-inner</i>
 * <p>To create instances of this class use {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1InnerBuilder}.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1InnerBuilder
 */
public interface C1Inner extends TreeChildNode<C1,Item<C1>>, Instantiable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>, Augmentable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner> {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:uses-augment-recursive", "2017-05-19", "C1-inner").intern();

    java.lang.String getLeafCC1();

}
