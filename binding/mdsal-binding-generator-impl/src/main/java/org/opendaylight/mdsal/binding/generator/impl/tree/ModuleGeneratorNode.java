/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

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
    }

    public void assignJavaTypeNames() {
        // First things first: assign a Java package for our namespace
        final QNameModule namespace = statement().localQNameModule();
        final String packageName = BindingMapping.getRootPackageName(namespace);
        setJavaPackage(packageName);

        // FIXME: "Data" should be a well-known constant
        final String simpleName = BindingMapping.getClassName(statement().argument().getLocalName()) + "Data";
        final JavaTypeName typeName = JavaTypeName.create(packageName, simpleName);
        setTypeName(typeName);

        // We are now fully set up, cascade down to children
        assignChildNames();
    }
}
