/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.api;

import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * An example of how configuration injection integrates with Service Component Runtime
 */
@Component
public final class ExampleTestImplementation {
    @InitialConfiguration
    static final Cont INITIAL_CONT = new ContBuilder().setVlanId(new VlanId(VlanId.Enumeration.Any)).build();

    @Activate
    public ExampleTestImplementation(@Reference final ConfigurationSnapshot<Cont> configuration) {

    }
}
