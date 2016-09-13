/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.java.api.generator.renderers.EnumRenderer;
import org.opendaylight.mdsal.binding2.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.UnitName;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Transforms data from virtual form to JAVA source code. Resulting source code represents JAVA
 * interface. Source code generation process is supported by interface template written
 * in Twirl (Scala based) language.
 */
@Beta
public class EnumGenerator implements CodeGenerator {

    @Override
    public String generate(Type type) {
        if (type instanceof Enumeration) {
            final Enumeration enums = (Enumeration) type;
            return new EnumRenderer(enums).generateTemplate();
        }
        return "";
    }

    @Override
    public boolean isAcceptable(Type type) {
        return type instanceof Enumeration;
    }

    @Override
    public Identifier getUnitName(Type type) {
        return new UnitName(type.getName());
    }
}