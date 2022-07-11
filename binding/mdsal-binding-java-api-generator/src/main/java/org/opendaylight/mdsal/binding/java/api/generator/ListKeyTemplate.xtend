/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedType
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;

/**
 * Template for generating JAVA class.
 */
final class ListKeyTemplate extends ClassTemplate {
    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
    }

    override allValuesConstructor() '''
        /**
         * Constructs an instance.
         *
         «FOR p : allProperties»
            * @param «p.fieldName» the entity «p.getName»
         «ENDFOR»
         * @throws NullPointerException if any of the arguments are null
         */
        public «type.name»(«allProperties.asNonNullArgumentsDeclaration») {
            «FOR p : allProperties»
                «val fieldName = p.fieldName»
                this.«fieldName» = «CODEHELPERS.importedName».requireKeyProp(«fieldName», "«p.name»")«p.cloneCall»;
            «ENDFOR»
            «FOR p : properties»
                «generateRestrictions(type, p.fieldName, p.returnType)»
            «ENDFOR»
        }
    '''

    override getterMethod(GeneratedProperty field) '''
        /**
         * Return «field.getName», guaranteed to be non-null.
         *
         * @return {@code «field.returnType.importedName»} «field.getName», guaranteed to be non-null.
         */
        public «field.returnType.importedNonNull» «field.getterMethodName»() {
            return «field.fieldName»«field.cloneCall»;
        }
    '''

    override protected String formatDataForJavaDoc(GeneratedType type) {
        return '''
            This class represents the key of {@link «gatClassName(type)»} class.

            @see «gatClassName(type)»
        '''
    }

    private def String gatClassName(GeneratedType field) {
        val String name = field.getName();
        if (name.endsWith(BindingMapping.KEY_SUFFIX)) {
            return name.substring(0, name.length() - BindingMapping.KEY_SUFFIX.length());
        }
        return name;
    }
}
