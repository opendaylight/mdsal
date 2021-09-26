/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * @author nite
 *
 */
final class ModuleConfiguration<M extends DataRoot> implements Immutable {
    private final ClassToInstanceMap<Class<? extends ChildOf<M>>> children;
    private final Class<M> moduleClass;

    ModuleConfiguration() {

    }


}
