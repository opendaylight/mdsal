/*
 * Copyright (c) 2018 ZTE inc, and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.common;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public class BindingMappingBaseTest {

    @Test
    public void basicTest() {
        assertTrue(BindingMappingBase.getRawRootPackageName(QNameModule.create(URI.create("test:URI"),
            Revision.of("2017-10-26")), Optional.of(SemVer.valueOf("0.0.1")), "gen.prefix")
            .equals("gen.prefix.test.URI.0.0.1"));
        assertTrue(BindingMappingBase.getRawRootPackageName(QNameModule.create(URI.create("test:URI"),
            Revision.of("2017-10-26")), Optional.empty(), "gen.prefix")
            .equals("gen.prefix.test.URI.rev171026"));
    }

}