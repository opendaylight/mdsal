/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import com.google.common.annotations.Beta;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Interface allowing customization of classes after loading.
 *
 * @deprecated Code generation is a concert separate from type mapping and is an implementation detail.
 */
@Beta
@Deprecated
@FunctionalInterface
public interface ClassCustomizer {
    /**
     * Customize a class.
     *
     * @param cls Class to be customized
     * @throws CannotCompileException when a javassist error occurs
     * @throws NotFoundException when a javassist error occurs
     */
    void customizeClass(CtClass cls) throws CannotCompileException, NotFoundException;
}
