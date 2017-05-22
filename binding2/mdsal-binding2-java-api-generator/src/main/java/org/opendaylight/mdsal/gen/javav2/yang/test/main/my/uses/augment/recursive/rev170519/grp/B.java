package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp;

import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.B.B1;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>uses-augment-recursive</b>
 * <pre>
 * grouping B {
 *     container B1 {
 *         leaf leaf-B-B1 {
 *             type string;
 *         }
 *         container B1-inner {
 *             leaf leaf-B-B1 {
 *                 type string;
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>uses-augment-recursive/B</i>
 */
public interface B extends TreeNode {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:uses-augment-recursive", "2017-05-19", "B").intern();

    B1 getB1();

}
