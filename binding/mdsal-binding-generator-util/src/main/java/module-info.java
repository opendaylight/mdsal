/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.generator.util {
    exports org.opendaylight.mdsal.binding.generator.util;
    exports org.opendaylight.mdsal.binding.model.util;
    exports org.opendaylight.mdsal.binding.model.util.generated.type.builder;

    // FIXME: 8.0.0: is this a service?
    // provides TypeProvider with BaseYangTypesProvider;

    requires transitive org.opendaylight.mdsal.binding.generator.api;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.model.util;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
}
