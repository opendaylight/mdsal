/*
 *
 *  * Copyright (c) 2016. Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.IDENTIFIABLE;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;

public class ListResolver {
    private TypeProvider typeProvider;
    private SchemaContext schemaContext;
    private Map<Module, ModuleContext> genCtx;

    public ListResolver(SchemaContext schemaContext, TypeProvider typeProvider, Map<Module, ModuleContext> genCtx) {
        this.typeProvider = typeProvider;
        this.schemaContext = schemaContext;
        this.genCtx = genCtx;
    }

}