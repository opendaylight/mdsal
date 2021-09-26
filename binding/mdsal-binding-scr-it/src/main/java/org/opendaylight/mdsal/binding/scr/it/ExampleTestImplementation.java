/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.it;

import org.opendaylight.mdsal.binding.scr.api.ConfigurationSnapshot;
import org.opendaylight.mdsal.binding.scr.api.InitialConfiguration;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of how configuration injection integrates with Service Component Runtime.
 */
@Component(immediate = true)
public final class ExampleTestImplementation {
    @InitialConfiguration
    public static final Cont INITIAL_CONT = new ContBuilder().setVlanId(new VlanId(VlanId.Enumeration.Any)).build();

    private static final Logger LOG = LoggerFactory.getLogger(ExampleTestImplementation.class);

    private final Cont lastest;

    @Activate
    public ExampleTestImplementation(@Reference final ConfigurationSnapshot<Cont> snapshot) {
        lastest = snapshot.configuration();
        LOG.info("O hai, wud U likez soem {}?", lastest);
    }

    public Cont latest() {
        return lastest;
    }
}
