package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont;
import org.opendaylight.yangtools.yang.binding.InterfaceTyped;
import org.opendaylight.yangtools.yang.common.QName;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>mainYangTest</b>
 * <pre>
 * choice main-group-cont-choice {
 *     case a {
 *         leaf case-1 {
 *             type string;
 *         }
 *     }
 *     case b {
 *         leaf case2-1 {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>mainYangTest/main-group/main-group-cont/main-group-cont-choice</i>
 *
 */
public interface MainGroupContChoice
    extends
        InterfaceTyped
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:test:mainYangTest",
        "2016-01-01", "main-group-cont-choice").intern();


}

