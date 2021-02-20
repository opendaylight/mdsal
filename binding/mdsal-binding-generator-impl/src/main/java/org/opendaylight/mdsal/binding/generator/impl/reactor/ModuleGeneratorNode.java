/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * Generator node corresponding to a {@code module} statement. These nodes are roots for generating types for a
 * particular {@link QNameModule} as mapped into the root package.
 */
public final class ModuleGeneratorNode extends AbstractCompositeGeneratorNode<ModuleEffectiveStatement> {
    public ModuleGeneratorNode(final ModuleEffectiveStatement statement) {
        super(statement);
        setJavaPackage(BindingMapping.getRootPackageName(statement().localQNameModule()));
    }

    // Step one: link type definitions
    public void linkTypedefs(final TypedefResolver linker) {




    }



    public void assignJavaTypeNames() {
        // First things first: assign simple class names. These do not necessarily reflect nesting, just make sure we
        // do not overlap constructs at the same level.
        setSimpleName(preferredName());
        setChildSimpleNames();




        final JavaTypeName typeName = JavaTypeName.create(javaPackage(), simpleName);

        setTypeName(typeName);

    }

    @Override
    String preferredName() {
        // FIXME: "Data" should be a well-known constant
        return BindingMapping.getClassName(statement().argument().getLocalName()) + "Data";
    }
}
