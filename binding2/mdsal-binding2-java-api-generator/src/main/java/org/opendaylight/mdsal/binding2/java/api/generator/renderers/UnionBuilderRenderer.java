/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.txt.unionBuilderTemplate;

public class UnionBuilderRenderer extends ClassRenderer {
    /**
     * list of all imported names for template
     */
    private final Map<String, String> importedNames = new HashMap<>();
    private final Map<String, String> generatedParameters = new HashMap<>();

    public UnionBuilderRenderer(final GeneratedTransferObject type) {
        super(type);
    }

    protected String body() {
        importedNames.put("unsupportedOperationException", importedName(UnsupportedOperationException.class));
        for (MethodSignature methodSignature : genTO.getMethodDefinitions()) {
            importedNames.put(methodSignature.getName(), importedName(methodSignature.getReturnType()));
            generatedParameters.put(methodSignature.getName(), generateParameters(methodSignature.getParameters()));
        }
        return unionBuilderTemplate.render(genTO, type.getName(), importedNames, generatedParameters).body();
    }
}