/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.txt.unionBuilderTemplate;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;

public class UnionBuilderRenderer extends ClassRenderer {

    public UnionBuilderRenderer(final GeneratedTransferObject type) {
        super(type);
    }

    protected String body() {
        // list of all imported names for template
        final Map<String, String> importedNames = new HashMap<>();
        final Map<String, String> generatedParameters = new HashMap<>();

        importedNames.put("unsupportedOperationException", importedName(UnsupportedOperationException.class));
        for (MethodSignature methodSignature : genTO.getMethodDefinitions()) {
            importedNames.put(methodSignature.getName(), importedName(methodSignature.getReturnType()));
            generatedParameters.put(methodSignature.getName(), generateParameters(methodSignature.getParameters()));
        }
        return unionBuilderTemplate.render(genTO, getType().getName(), importedNames, generatedParameters).body();
    }
}