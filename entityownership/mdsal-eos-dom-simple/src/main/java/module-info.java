/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;

module org.opendaylight.mdsal.eos.dom.simple {
    exports org.opendaylight.mdsal.eos.dom.simple.di;

    provides DOMEntityOwnershipService with SimpleDOMEntityOwnershipService;

    requires transitive org.opendaylight.mdsal.eos.dom.api;
    requires org.opendaylight.yangtools.concepts;
    requires org.slf4j;

    // Annotations
    requires static transitive javax.inject;
    requires static org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;
}
