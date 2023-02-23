/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.binding.YangExtension;
import org.opendaylight.yangtools.yang.binding.YangFeature;

/**
 * Transformator of the data from the virtual form to JAVA source code. The result source code represents JAVA class.
 * For generating of the source code is used the template written in XTEND language.
 */
public final class TOGenerator implements CodeGenerator {
    private static final @NonNull JavaTypeName EXTENSION_TYPE = JavaTypeName.create(YangExtension.class);
    private static final @NonNull JavaTypeName FEATURE_TYPE = JavaTypeName.create(YangFeature.class);

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code is generated according to the template
     * source code template which is written in XTEND language.
     */
    @Override
    public String generate(final Type type) {
        if (type instanceof GeneratedTransferObject genTO) {
            if (genTO.isUnionType()) {
                return new UnionTemplate(genTO).generate();
            } else if (genTO.isTypedef()) {
                return new ClassTemplate(genTO).generate();
            } else {
                ClassTemplate template;
                var dataRoot = BindingTypes.extractYangExtensionDataRoot(genTO);
                if (dataRoot != null) {
                    template = new YangConstructTemplate(genTO, EXTENSION_TYPE, dataRoot);
                }
                dataRoot = BindingTypes.extractYangFeatureDataRoot(genTO);
                if (dataRoot != null) {
                    template = new YangConstructTemplate(genTO, FEATURE_TYPE, dataRoot);
                } else {
                    template = new ListKeyTemplate(genTO);
                }
                return template.generate();
            }
        }
        return "";
    }

    @Override
    public boolean isAcceptable(final Type type) {
        return type instanceof GeneratedTransferObject;
    }

    @Override
    public String getUnitName(final Type type) {
        return type.getName();
    }
}
