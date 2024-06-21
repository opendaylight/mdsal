/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.yanglib.api.YangLibSupportFactory;
import org.opendaylight.mdsal.yanglib.rfc8525.YangLibrarySupportFactory;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

module org.opendaylight.mdsal.yanglib.rfc8525 {
    // FIXME: just the DI bits
    exports org.opendaylight.mdsal.yanglib.rfc8525;

    provides YangLibSupportFactory with YangLibrarySupportFactory;

    uses BindingCodecTreeFactory;
    uses BindingRuntimeGenerator;
    uses YangParserFactory;

    requires transitive org.opendaylight.yangtools.binding.data.codec.api;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.mdsal.yanglib.api;

    requires com.google.common;
    requires org.opendaylight.yangtools.binding.runtime.spi;
    requires org.opendaylight.yangtools.binding.spec;
    requires org.opendaylight.yangtools.yang.data.api;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.opendaylight.yang.gen.ietf.datastores.rfc8342;
    requires org.opendaylight.yang.gen.ietf.inet.types.rfc6991;
    requires org.opendaylight.yang.gen.ietf.yang.library.rfc8525;
    requires org.opendaylight.yang.gen.ietf.yang.types.rfc6991;
    requires org.slf4j;

    // Annotations
    requires static javax.inject;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;
}
