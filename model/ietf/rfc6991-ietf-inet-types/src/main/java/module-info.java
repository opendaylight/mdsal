/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Binding classes generated for <a href="https://www.rfc-editor.org/rfc/rfc6991#page-15">RFC6991 ietf-inet-types</a>.
 */
module org.opendaylight.yang.gen.ietf.inet.types.rfc6991 {
    exports org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

    // FIXME: For reflective instantiation. Should be removed once we have unsafe costructurs
    opens org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715
    to org.opendaylight.mdsal.binding.spec.util;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.binding;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.mdsal.model.ietf.type.util;

    // Annotations
    requires static transitive java.compiler;
    requires static transitive java.management;
}