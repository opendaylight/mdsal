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
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
public final class GeneratedTOBuilderImpl extends AbstractGeneratedTypeBuilder<GeneratedTOBuilder> implements
        GeneratedTOBuilder {

    //TODO: implement methods
    @Override
    public GeneratedTOBuilder setExtendsType(GeneratedTransferObject genTransObj) {
        return null;
    }

    @Override
    public GeneratedTOBuilder addEqualsIdentity(GeneratedPropertyBuilder property) {
        return null;
    }

    @Override
    public GeneratedTOBuilder addHashIdentity(GeneratedPropertyBuilder property) {
        return null;
    }

    @Override
    public GeneratedTOBuilder addToStringProperty(GeneratedPropertyBuilder property) {
        return null;
    }

    @Override
    public void setRestrictions(Restrictions restrictions) {

    }

    @Override
    public GeneratedTransferObject toInstance() {
        return new GeneratedTransferObjectImpl(this);
    }

    @Override
    public void setTypedef(boolean isTypedef) {

    }

    @Override
    public void setBaseType(TypeDefinition<?> typeDef) {

    }

    @Override
    public void setIsUnion(boolean isUnion) {

    }

    @Override
    public void setIsUnionBuilder(boolean isUnionTypeBuilder) {

    }

    @Override
    public void setSUID(GeneratedPropertyBuilder suid) {

    }

    @Override
    public GeneratedTOBuilder addEnclosingTransferObject(String name) {
        return null;
    }

    @Override
    public GeneratedTOBuilder addEnclosingTransferObject(GeneratedTOBuilder genTOBuilder) {
        return null;
    }

    @Override
    public GeneratedTOBuilder addComment(String comment) {
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
    public GeneratedTOBuilder setAbstract(boolean isAbstract) {
        return null;
    }

    @Override
    public List<Type> getImplementsTypes() {
        return null;
    }

    @Override
    public GeneratedTOBuilder addImplementsType(Type genType) {
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
    protected GeneratedTOBuilderImpl thisInstance() {
        return this;
    }

    private static final class GeneratedTransferObjectImpl extends AbstractGeneratedType implements GeneratedTransferObject {

        //TODO: implement methods
        public GeneratedTransferObjectImpl(final GeneratedTOBuilderImpl builder) {
            super(builder);
        }

        @Override
        public GeneratedProperty getSUID() {
            return null;
        }

        @Override
        public GeneratedTransferObject getSuperType() {
            return null;
        }

        @Override
        public List<GeneratedProperty> getEqualsIdentifiers() {
            return null;
        }

        @Override
        public List<GeneratedProperty> getHashCodeIdentifiers() {
            return null;
        }

        @Override
        public List<GeneratedProperty> getToStringIdentifiers() {
            return null;
        }

        @Override
        public boolean isTypedef() {
            return false;
        }

        @Override
        public TypeDefinition<?> getBaseType() {
            return null;
        }

        @Override
        public boolean isUnionType() {
            return false;
        }

        @Override
        public boolean isUnionTypeBuilder() {
            return false;
        }

        @Override
        public Restrictions getRestrictions() {
            return null;
        }
    }
}
