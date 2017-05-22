package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data;

import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp.A;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.UsesAugmentRecursiveData;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>uses-augment-recursive</b>
 * <pre>
 * list L {
 *     key ";
 *     container A1 {
 *         container A1-inner {
 *             container B1 {
 *                 container B1-inner {
 *                     container C1 {
 *                         container C1-inner {
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 *     uses A;
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>uses-augment-recursive/L</i>
 * <p>To create instances of this class use {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.LBuilder}.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.LBuilder
 */
public interface L extends TreeChildNode<UsesAugmentRecursiveData,Item<UsesAugmentRecursiveData>>, Instantiable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>, Augmentable<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L>, A {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:uses-augment-recursive", "2017-05-19", "L").intern();

}
