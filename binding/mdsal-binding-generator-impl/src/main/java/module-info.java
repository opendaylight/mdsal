/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;

module org.opendaylight.mdsal.binding.generator.impl {
    // FIXME: 8.0.0: do not export this package (move public stuff to .di)
    exports org.opendaylight.mdsal.binding.generator.impl;
    exports org.opendaylight.mdsal.binding.yang.types;

    provides BindingGenerator with DefaultBindingGenerator;
    provides BindingRuntimeGenerator with DefaultBindingRuntimeGenerator;

    requires transitive org.opendaylight.mdsal.binding.generator.api;
    requires transitive org.opendaylight.mdsal.binding.generator.util;
    requires transitive org.opendaylight.mdsal.binding.runtime.api;
    requires org.opendaylight.mdsal.binding.spec.util;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static javax.inject;
    requires static metainf.services;
    requires static org.osgi.service.component.annotations;
}
