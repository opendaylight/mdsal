/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
public final class GeneratedTypeBuilderImpl extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    //TODO: implement all methods
    @Override
    public GeneratedType toInstance() {
        return new GeneratedTypeImpl(this);
    }

    @Override
    public GeneratedTOBuilder addEnclosingTransferObject(String name) {
        return null;
    }

    @Override
    public GeneratedTypeBuilder addEnclosingTransferObject(GeneratedTOBuilder genTOBuilder) {
        return null;
    }

    @Override
    public GeneratedTypeBuilder addComment(String comment) {
        return null;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(String packageName, String name) {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public GeneratedTypeBuilder setAbstract(boolean isAbstract) {
        return null;
    }

    @Override
    public List<Type> getImplementsTypes() {
        return null;
    }

    @Override
    public GeneratedTypeBuilder addImplementsType(Type genType) {
        return null;
    }

    @Override
    public Constant addConstant(Type type, String name, Object value) {
        return null;
    }

    @Override
    public EnumBuilder addEnumeration(String name) {
        return null;
    }

    @Override
    public List<MethodSignatureBuilder> getMethodDefinitions() {
        return null;
    }

    @Override
    public MethodSignatureBuilder addMethod(String name) {
        return null;
    }

    @Override
    public boolean containsMethod(String methodName) {
        return false;
    }

    @Override
    public List<GeneratedPropertyBuilder> getProperties() {
        return null;
    }

    @Override
    public GeneratedPropertyBuilder addProperty(String name) {
        return null;
    }

    @Override
    public boolean containsProperty(String name) {
        return false;
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void setModuleName(String moduleName) {

    }

    @Override
    public void setSchemaPath(Iterable<QName> schemaPath) {

    }

    @Override
    public void setReference(String reference) {

    }

    @Override
    protected GeneratedTypeBuilderImpl thisInstance() {
        return this;
    }

    private static final class GeneratedTypeImpl extends AbstractGeneratedType {

        public GeneratedTypeImpl(final GeneratedTypeBuilderImpl builder) {
            super(builder);
        }
    }
}
