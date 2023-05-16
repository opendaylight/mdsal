/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.eos.binding.dom.adapter {
    exports org.opendaylight.mdsal.eos.binding.dom.adapter;

    requires transitive net.bytebuddy;
    requires transitive net.bytebuddy.agent;
    requires transitive org.mockito;
    requires org.opendaylight.mdsal.binding.dom.adapter;
    requires org.opendaylight.mdsal.eos.binding.api;
    requires org.opendaylight.mdsal.eos.dom.api;
    requires org.opendaylight.yangtools.concepts;
    requires org.osgi.service.component.annotations;
}