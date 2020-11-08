/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.dom.impl.DOMClusterSingletonServiceProviderImpl;

module org.opendaylight.mdsal.singleton.dom.impl {
    exports org.opendaylight.mdsal.singleton.dom.impl.di;

    provides ClusterSingletonServiceProvider with DOMClusterSingletonServiceProviderImpl;

    requires transitive org.opendaylight.mdsal.singleton.common.api;
    requires transitive org.opendaylight.mdsal.eos.dom.api;
    requires org.opendaylight.mdsal.eos.common.api;
    requires org.opendaylight.yangtools.concepts;
    requires org.slf4j;

    uses DOMEntityOwnershipService;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static javax.inject;
    requires static metainf.services;
    requires static org.checkerframework.checker.qual;
    requires static org.osgi.service.component.annotations;
}
