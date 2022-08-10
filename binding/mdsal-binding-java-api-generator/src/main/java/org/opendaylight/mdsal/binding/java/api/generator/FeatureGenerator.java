/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaClassCompleteness;
import org.opendaylight.mdsal.binding.model.api.Type;

final class FeatureGenerator implements CodeGenerator {
    @Override
    public String generate(final Type type) {
        return type instanceof GeneratedType genType ? new FeatureTemplate(genType).generate() : "";
    }

    @Override
    public boolean isAcceptable(final Type type) {
        return type instanceof GeneratedType genType && genType.classCompleteness() == JavaClassCompleteness.FINAL
            && !(genType instanceof Enumeration);
    }

    @Override
    public String getUnitName(final Type type) {
        return type.getName();
    }
}
