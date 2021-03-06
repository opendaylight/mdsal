/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMap.Builder
import java.util.Optional
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedType

/**
 * Template for generating JAVA enumeration type.
 */
class EnumTemplate extends BaseTemplate {
    /**
     * Enumeration which will be transformed to JAVA source code for enumeration
     */
    val Enumeration enums

    /**
     * Constructs instance of this class with concrete <code>enums</code>.
     *
     * @param enums enumeration which will be transformed to JAVA source code
     */
    new(AbstractJavaGeneratedType javaType, Enumeration enums) {
        super(javaType, enums as GeneratedType)
        this.enums = enums
    }

    /**
     * Constructs instance of this class with concrete <code>enums</code>.
     *
     * @param enums enumeration which will be transformed to JAVA source code
     */
    new(Enumeration enums) {
        super(enums as GeneratedType)
        this.enums = enums
    }

    /**
     * Generates only JAVA enumeration source code.
     *
     * @return string with JAVA enumeration source code
     */
    def generateAsInnerClass() {
        return body
    }

    def writeEnumItem(String name, String mappedName, int value, String description) '''
        «IF description !== null»
            «description.trim.encodeAngleBrackets.encodeJavadocSymbols.wrapToDocumentation»
        «ENDIF»
        «mappedName»(«value», "«name»")
    '''

    /**
     * Template method which generates enumeration body (declaration + enumeration items).
     *
     * @return string with the enumeration body
     */
    override body() '''
        «enums.formatDataForJavaDoc.wrapToDocumentation»
        «generatedAnnotation»
        public enum «enums.name» implements «org.opendaylight.yangtools.yang.binding.Enumeration.importedName» {
            «writeEnumeration(enums)»

            private static final «JU_MAP.importedName»<«STRING.importedName», «enums.name»> NAME_MAP;
            private static final «JU_MAP.importedName»<«Integer.importedName», «enums.name»> VALUE_MAP;

            static {
                final «Builder.importedName»<«STRING.importedName», «enums.name»> nb = «ImmutableMap.importedName».builder();
                final «Builder.importedName»<«Integer.importedName», «enums.name»> vb = «ImmutableMap.importedName».builder();
                for («enums.name» enumItem : «enums.name».values()) {
                    vb.put(enumItem.value, enumItem);
                    nb.put(enumItem.name, enumItem);
                }

                NAME_MAP = nb.build();
                VALUE_MAP = vb.build();
            }

            private final «STRING.importedName» name;
            private final int value;

            private «enums.name»(int value, «STRING.importedName» name) {
                this.value = value;
                this.name = name;
            }

            @«OVERRIDE.importedName»
            public «STRING.importedName» getName() {
                return name;
            }

            @«OVERRIDE.importedName»
            public int getIntValue() {
                return value;
            }

            /**
             * Return the enumeration member whose {@link #getName()} matches specified value.
             *
             * @param name YANG assigned name
             * @return corresponding «enums.name» item, if present
             * @throws NullPointerException if name is null
             */
            public static «Optional.importedName»<«enums.name»> forName(«STRING.importedName» name) {
                return «Optional.importedName».ofNullable(NAME_MAP.get(«JU_OBJECTS.importedName».requireNonNull(name)));
            }

            /**
             * Return the enumeration member whose {@link #getIntValue()} matches specified value.
             *
             * @param intValue integer value
             * @return corresponding «enums.name» item, or null if no such item exists
             */
            public static «enums.name» forValue(int intValue) {
                return VALUE_MAP.get(intValue);
            }
        }
    '''

    def writeEnumeration(Enumeration enumeration)
    '''
    «FOR v : enumeration.values SEPARATOR ",\n" AFTER ";"»
    «writeEnumItem(v.name, v.mappedName, v.value, v.description.orElse(null))»«
    ENDFOR»
    '''
}
