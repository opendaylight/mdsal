/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers.ClassRenderer;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers.UnionBuilderRenderer;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers.UnionRenderer;
import org.opendaylight.mdsal.binding.javav2.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.UnitName;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Transformer of the data from the virtual form to JAVA source code. The
 * result source code represents JAVA class. For generating of the source code
 * is used the template written in Twirl (Scala based) language.
 */
@Beta
public class TOGenerator implements CodeGenerator {

    @Override
    public String generate(Type type) {
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject genTO = (GeneratedTransferObject) type;
            if (genTO.isUnionType()) {
                return new UnionRenderer(genTO).generateTemplate();
            } else if (genTO.isUnionTypeBuilder()) {
                return new UnionBuilderRenderer(genTO).generateTemplate();
            } else {
                return new ClassRenderer(genTO).generateTemplate();
            }
        }
        return "";
    }

    @Override
    public boolean isAcceptable(Type type) {
        return type instanceof GeneratedTransferObject;
    }

    @Override
    public Identifier getUnitName(Type type) {
        return new UnitName(type.getName());
    }
}