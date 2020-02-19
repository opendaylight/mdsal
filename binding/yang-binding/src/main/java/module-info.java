/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.yang.binding {
    exports org.opendaylight.yangtools.yang.binding;
    exports org.opendaylight.yangtools.yang.binding.annotations;

    requires transitive com.google.common;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.util;

    requires static transitive org.eclipse.jdt.annotation;
}
