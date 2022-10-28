/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package mdsal.binding.json.codec;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId.Enumeration;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class BindingJSONCodecTest {
    @Test
    public void testSimpleContainer() {
        final var cont = new ContBuilder().setVlanId(new Cont.VlanId(Enumeration.forValue(30))).build();
        DataObject dataObject = cont;
        if (dataObject instanceof Cont) {
            Cont test = (Cont) dataObject;
            System.out.println(test.getVlanId());
        }
    }

}
