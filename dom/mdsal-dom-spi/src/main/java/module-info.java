/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.dom.spi.rev240120.YangModelBindingProviderImpl;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;

module org.opendaylight.mdsal.dom.spi {
    exports org.opendaylight.mdsal.dom.spi;
    exports org.opendaylight.mdsal.dom.spi.query;
    exports org.opendaylight.mdsal.dom.spi.store;
    exports org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.dom.spi.rev240120;
    exports org.opendaylight.yang.svc.v1.urn.opendaylight.params.xml.ns.yang.mdsal.dom.spi.rev240120;

    provides YangModelBindingProvider with YangModelBindingProviderImpl;

    requires transitive org.opendaylight.mdsal.common.api;
    requires transitive org.opendaylight.mdsal.dom.api;
    requires transitive org.opendaylight.yangtools.yang.binding;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.data.tree.api;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires com.google.common;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.odlext.model.api;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.data.tree.spi;
    requires org.opendaylight.yang.gen.ietf.restconf.rfc8040;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static java.compiler;
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
}
