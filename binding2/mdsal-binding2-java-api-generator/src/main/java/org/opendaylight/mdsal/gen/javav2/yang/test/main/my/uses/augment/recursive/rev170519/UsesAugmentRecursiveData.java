package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519;

import org.opendaylight.mdsal.binding.javav2.spec.base.TreeRoot;
import java.util.List;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.L;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>uses-augment-recursive</b>
 * <pre>
 * module uses-augment-recursive {
 *     yang-version 1;
 *     namespace "yang:test:main:my:uses-augment-recursive";
 *     prefix "r";
 *     import groupings { prefix "grp"; revision-date 2017-05-15; }
 *     revision 2017-05-19;
 *     list L {
 *         key ";
 *         container A1 {
 *             container A1-inner {
 *                 container B1 {
 *                     container B1-inner {
 *                         container C1 {
 *                             container C1-inner {
 *                             }
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *         uses A;
 *     }
 *     grouping C {
 *         container C1 {
 *             leaf leaf-C-C1 {
 *                 type string;
 *             }
 *             container C1-inner {
 *                 leaf leaf-C-C1 {
 *                     type string;
 *                 }
 *             }
 *         }
 *     }
 *     grouping B {
 *         container B1 {
 *             leaf leaf-B-B1 {
 *                 type string;
 *             }
 *             container B1-inner {
 *                 leaf leaf-B-B1 {
 *                     type string;
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
public interface UsesAugmentRecursiveData extends TreeRoot {

    List<L> getL();

}
