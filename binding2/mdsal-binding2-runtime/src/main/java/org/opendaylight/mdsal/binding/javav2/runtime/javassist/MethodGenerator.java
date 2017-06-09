/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.javassist;

import com.google.common.annotations.Beta;
import javassist.CannotCompileException;
import javassist.CtMethod;

/**
 * Interface allowing generator new methods.
 */
@Beta
public interface MethodGenerator {

    /**
     * Process method.
     *
     * @param method
     *            - method to be generated
     * @throws CannotCompileException
     */
    void process(CtMethod method) throws CannotCompileException;
}
