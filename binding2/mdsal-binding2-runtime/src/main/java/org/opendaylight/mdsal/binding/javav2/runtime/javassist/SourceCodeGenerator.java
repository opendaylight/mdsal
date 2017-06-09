/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.javassist;

import com.google.common.annotations.Beta;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

/**
 * Interface for a class that that generates readable source code for a runtime generated class.
 * The appendField/appendMethod methods append source code to a temporary output. When outputGeneratedSource
 * is called, the entire class source code is generated and outputted.
 */
@Beta
public interface SourceCodeGenerator {

    /**
     * Appends the given class field and value to the temporary output.
     */
    void appendField(CtField field, String value);

    /**
     * Appends the given method and source code body to the temporary output.
     */
    void appendMethod(CtMethod method, String code);

    /**
     * Generates the full source code for the given class and outputs it.
     */
    void outputGeneratedSource(CtClass ctClass);
}
