/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaClassCompleteness;
import org.opendaylight.mdsal.binding.model.api.Type;

public final class InterfaceGenerator implements CodeGenerator {
    @Override
    public boolean isAcceptable(final Type type) {
        return type instanceof GeneratedType genType && genType.classCompleteness() == JavaClassCompleteness.ABSTRACT
            && !(genType instanceof GeneratedTransferObject) && !(genType instanceof Enumeration);
    }

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code
     * is generated according to the template source code template which is
     * written in XTEND language.
     */
    @Override
    public String generate(final Type type) {
        return type instanceof GeneratedType genType ? new InterfaceTemplate(genType).generate() : "";
    }

    @Override
    public String getUnitName(final Type type) {
        return type.getName();
    }
}
