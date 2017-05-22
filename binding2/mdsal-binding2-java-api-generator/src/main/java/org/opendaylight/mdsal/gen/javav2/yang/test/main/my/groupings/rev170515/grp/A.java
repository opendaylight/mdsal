package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.grp;

import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>groupings</b>
 * <pre>
 * grouping A {
 *     container A1 {
 *         leaf leaf-A-A1 {
 *             type string;
 *         }
 *         container A1-inner {
 *             leaf leaf-A-A1 {
 *                 type string;
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>groupings/A</i>
 */
public interface A extends TreeNode {

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("yang:test:main:my:groupings", "2017-05-15", "A").intern();

    A1 getA1();

}
