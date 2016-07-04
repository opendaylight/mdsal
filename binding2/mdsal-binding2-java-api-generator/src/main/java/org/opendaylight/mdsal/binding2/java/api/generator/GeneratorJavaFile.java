/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generates files with JAVA source code for every specified type.
 */
@Beta
public final class GeneratorJavaFile {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorJavaFile.class);

    /**
     * List of <code>CodeGenerator</code> instances.
     */
    private final List<CodeGenerator> generators = new ArrayList<>();

    /**
     * Set of <code>Type</code> instances for which the JAVA code is generated.
     */
    private final Collection<? extends Type> types;

    /**
     * BuildContext used for instantiating files
     */
    private final BuildContext buildContext;

    /**
     * Creates instance of this class with the set of <code>types</code> for
     * which the JAVA code is generated.
     *
     * The instances of concrete JAVA code generator are created.
     *
     * @param buildContext
     *            build context to use for accessing files
     * @param types
     *            set of types for which JAVA code should be generated
     */
    public GeneratorJavaFile(final BuildContext buildContext, final Collection<? extends Type> types) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
        this.types = Preconditions.checkNotNull(types);
        generators.add(new InterfaceGenerator());
        //TODO: finish generators
//        generators.add(new TOGenerator());
//        generators.add(new EnumGenerator());
//        generators.add(new BuilderGenerator());
    }

}
