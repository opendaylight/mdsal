/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static java.util.Objects.requireNonNull
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.BINARY_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.DECIMAL64_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.EMPTY_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.INSTANCE_IDENTIFIER
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.INT16_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.INT32_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.INT64_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.INT8_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.STRING_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.UINT16_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.UINT32_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.UINT64_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BaseYangTypes.UINT8_TYPE
import static org.opendaylight.mdsal.binding.model.ri.BindingTypes.SCALAR_TYPE_OBJECT
import static org.opendaylight.mdsal.binding.model.ri.BindingTypes.BITS_TYPE_OBJECT
import static org.opendaylight.mdsal.binding.model.ri.Types.primitiveIntType;
import static org.opendaylight.mdsal.binding.model.ri.Types.primitiveLongType;
import static org.opendaylight.mdsal.binding.model.ri.Types.intArrayType;
import static org.opendaylight.mdsal.binding.model.ri.Types.STRING;
import static extension org.apache.commons.text.StringEscapeUtils.escapeJava
import static extension org.apache.commons.lang3.StringUtils.capitalize;

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import java.util.ArrayList
import java.util.Base64;
import java.util.Collection
import java.util.Comparator
import java.util.List
import java.util.Map
import java.util.Set
import javax.management.ConstructorParameters
import org.gaul.modernizer_maven_annotations.SuppressModernizer
import org.opendaylight.mdsal.binding.model.api.ConcreteType
import org.opendaylight.mdsal.binding.model.api.Constant
import org.opendaylight.mdsal.binding.model.api.Enumeration
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject
import org.opendaylight.mdsal.binding.model.api.Restrictions
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.ri.TypeConstants
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping
import org.opendaylight.yangtools.yang.common.Empty
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Template for generating JAVA class.
 */
@SuppressModernizer
class ClassTemplate extends BaseTemplate {
    static val Comparator<GeneratedProperty> PROP_COMPARATOR = Comparator.comparing([prop | prop.name])
    static val VALUEOF_TYPES = Set.of(
        BOOLEAN_TYPE,
        DECIMAL64_TYPE,
        INT8_TYPE,
        INT16_TYPE,
        INT32_TYPE,
        INT64_TYPE,
        UINT8_TYPE,
        UINT16_TYPE,
        UINT32_TYPE,
        UINT64_TYPE)

    protected val List<GeneratedProperty> properties
    protected val List<GeneratedProperty> finalProperties
    protected val List<GeneratedProperty> parentProperties
    protected val List<GeneratedProperty> allProperties
    protected val Restrictions restrictions

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    protected val List<Enumeration> enums

    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    protected val List<Constant> consts

    protected val GeneratedTransferObject genTO

    val AbstractRangeGenerator<?> rangeGenerator

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        this(new TopLevelJavaGeneratedType(genType), genType)
    }

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(AbstractJavaGeneratedType javaType, GeneratedTransferObject genType) {
        super(javaType, genType)
        this.genTO = genType
        this.properties = genType.properties
        this.finalProperties = GeneratorUtil.resolveReadOnlyPropertiesFromTO(genTO.properties)
        this.parentProperties = GeneratorUtil.getPropertiesOfAllParents(genTO)
        this.restrictions = genType.restrictions

        val sorted = new ArrayList();
        sorted.addAll(properties);
        sorted.addAll(parentProperties);
        sorted.sort(PROP_COMPARATOR);

        this.allProperties = sorted
        this.enums = genType.enumerations
        this.consts = genType.constantDefinitions

        if (restrictions !== null && restrictions.rangeConstraint.present) {
            rangeGenerator = requireNonNull(AbstractRangeGenerator.forType(TypeUtils.encapsulatedValueType(genType)))
        } else {
            rangeGenerator = null
        }
    }

    /**
     * Generates JAVA class source code (class body only).
     *
     * @return string with JAVA class body source code
     */
    def CharSequence generateAsInnerClass() {
        return generateBody(true)
    }

    override protected body() {
        generateBody(false);
    }

    /**
     * Template method which generates class body.
     *
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class source code in JAVA format
     */
    def protected generateBody(boolean isInnerClass) '''
        «type.formatDataForJavaDoc.wrapToDocumentation»
        «annotationDeclaration»
        «IF !isInnerClass»
            «generatedAnnotation»
        «ENDIF»
        «generateClassDeclaration(isInnerClass)» {
            «suidDeclaration»
            «innerClassesDeclarations»
            «enumDeclarations»
            «constantsDeclarations»
            «generateFields»

            «IF restrictions !== null»
                «IF restrictions.lengthConstraint.present»
                    «LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
                        restrictions.lengthConstraint.get, this)»
                «ENDIF»
                «IF restrictions.rangeConstraint.present»
                    «rangeGenerator.generateRangeChecker("_value", restrictions.rangeConstraint.get, this)»
                «ENDIF»
            «ENDIF»

            «constructors»

            «defaultInstance»

            «propertyMethods»

            «IF isBitsTypeObject»
                «generateGettersBTO»

                «generateSettersBTO»

                «generateGetValueBTO»

                «generateValidValuesBTO»

                «generateStringValueBTO»

                «generateValueOfBTO»
            «ENDIF»

            «generateHashCode»

            «generateEquals»

            «generateToString(genTO.toStringIdentifiers)»
        }

    '''

    def protected generateGettersBTO() '''
        «val bitList = validBitList»
        «var i = 0»
        «FOR bit : bitList SEPARATOR "\n"»
            public boolean get«bit.capitalize»() {
                return «BITS_TYPE_OBJECT_HELPERS.importedName».getBit(«bitsFieldName», «i++»);
            }
        «ENDFOR»
    '''

    def protected generateSettersBTO() '''
        «val bitList = validBitList»
        «var i = 0»
        «FOR bit : bitList SEPARATOR "\n"»
            public void set«bit.capitalize»(boolean value) {
                this.«bitsFieldName» = «BITS_TYPE_OBJECT_HELPERS.importedName».setBit(«bitsFieldName», value, «i++»);
            }
        «ENDFOR»
    '''

    /**
     * Template method which generates the method <code>getValue()</code> for typedef,
     * which base type is BitsDefinition.
     *
     * @return string with the <code>getValue()</code> method definition in JAVA format
     */
     @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "FOR with SEPARATOR, not needing for value")
     def protected generateGetValueBTO() '''
        @Override
        public boolean[] getValue() {
            int i = 0;
            return new boolean[]{
                «val bitList = validBitList»
                «FOR bit : bitList SEPARATOR ", "»
                    «BITS_TYPE_OBJECT_HELPERS.importedName».getBit(this.«bitsFieldName», i++)
                «ENDFOR»
            };
        }
     '''

     def protected generateStringValueBTO() '''
         «IF genTO.typedef»
             «val bitList = validBitList»
             public static «JU_LIST.importedName»<«STRING.importedName»> stringValue() {
               return List.of(«FOR bit : bitList SEPARATOR ", "»"«bit»"«ENDFOR»);
             }
         «ENDIF»
     '''

     @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "FOR with SEPARATOR, not needing for value")
     def protected generateValueOfBTO() '''
         «IF genTO.typedef»
             «val bitList = validBitList»
             public static «genTO.name» valueOf(final «JU_LIST.importedName»<«STRING.importedName»> defaultValues) {
                 «JU_LIST.importedName»<«STRING.importedName»> properties = «Lists.importedName».newArrayList(«FOR bit : bitList SEPARATOR ", "»"«bit»"«ENDFOR»);
                 boolean[] bitValues = new boolean[properties.size()];
                 for (var property : properties) {
                     for (var value : defaultValues) {
                         if (!properties.contains(value)) {
                             throw new «IAE.importedName»("Invalid default parameter '" + value + "'");
                         }
                         if (property.equals(value)) {
                             bitValues[properties.indexOf(property)] = true;
                         }
                     }
                 }
                 int i = 0;
                 return new «genTO.name»(
                     «FOR bit : bitList SEPARATOR ", "»
                         bitValues[i++]
                     «ENDFOR»
                 );
             }
         «ENDIF»
     '''

    def private List<String> validBitList() {
      val bitList = newArrayList();
      if (!consts.empty) {
        for (c : consts) {
          if (c.name == TypeConstants.VALID_BITS_NAME) {
            val bits =  c.value as Map<Uint32, String>;
            for (bitVal : bits.values) {
              bitList.add(bitVal);
            }
          }
        }
      }
      return bitList;
    }

    def private bitsFieldName() {
        val props = isInheritedClass ? genTO.superType.properties : this.properties;
        for (prop : props) {
          if (prop.returnType.equals(primitiveIntType) ||
              prop.returnType.equals(primitiveLongType) ||
              prop.returnType.equals(intArrayType)) {
            return prop.fieldName;
          }
        }
        return "";
    }

    def private propertyMethods() {
        if (properties.empty) {
            return ""
        }
        isScalarTypeObject ? scalarTypeObjectValue(properties.get(0)) : defaultProperties
    }

    def private isScalarTypeObject() {
        for (impl : genTO.implements) {
            if (SCALAR_TYPE_OBJECT.identifier.equals(impl.identifier)) {
                return true
            }
        }
        return false
    }

    def private isBitsTypeObject() {
        for (impl : genTO.implements) {
            if (BITS_TYPE_OBJECT.identifier.equals(impl.identifier)) {
                return true
            }
        }
        return false
    }

    def private isInheritedClass() {
        return genTO.superType !== null ? true : false
    }

    def private defaultProperties() '''
        «FOR field : properties SEPARATOR "\n"»
            «field.getterMethod»
            «IF !field.readOnly»
                «field.setterMethod»
            «ENDIF»
        «ENDFOR»
    '''

    def private scalarTypeObjectValue(GeneratedProperty field) '''
        @«OVERRIDE.importedName»
        public «field.returnType.importedName» «BindingMapping.SCALAR_TYPE_OBJECT_GET_VALUE_NAME»() {
            return «field.fieldName»«field.cloneCall»;
        }
    '''

    def protected generateValidValuesBTO() '''
        @Override
        public «ImmutableSet.importedName»<String> validValues() {
            return «TypeConstants.VALID_BITS_NAME»;
        }
    '''

    /**
     * Template method which generates inner classes inside this interface.
     *
     * @return string with the source code for inner classes in JAVA format
     */
    def protected innerClassesDeclarations() '''
        «IF !type.enclosedTypes.empty»
            «FOR innerClass : type.enclosedTypes SEPARATOR "\n"»
                «generateInnerClass(innerClass)»
            «ENDFOR»
        «ENDIF»
    '''

    def protected constructors() '''
        «IF genTO.unionType»
            «genUnionConstructor»
        «ELSEIF genTO.typedef && allProperties.size == 1 && allProperties.get(0).name.equals(TypeConstants.VALUE_PROP)»
            «typedefConstructor»
        «ELSEIF isBitsTypeObject»
            «bitsEmptyConstructor»

            «bitsConstructor»
        «ELSE»
            «allValuesConstructor»
        «ENDIF»

        «IF !allProperties.empty»
            «copyConstructor»
        «ENDIF»
        «IF properties.empty && !parentProperties.empty »
            «parentConstructor»
        «ENDIF»
    '''

     def bitsEmptyConstructor() '''
        «val prop = getPropByName("bits")»
        public «type.name»() {
          «IF prop !== null»
            «IF prop.returnType.equals(primitiveIntType) || prop.returnType.equals(primitiveLongType)»
              this.«bitsFieldName» = 0;
            «ELSEIF prop.returnType.equals(intArrayType)»
              «val size = validBitList.size»
              «val wordSize = 32»
              this.«bitsFieldName» = new int[«size / wordSize + (size % wordSize == 0 ? 0 : 1)»];
            «ENDIF»
          «ELSEIF isInheritedClass»
             super();
          «ENDIF»
        }
     '''

     def bitsConstructor() '''
        «val bitList = validBitList»
        public «type.name»(«FOR bit : bitList SEPARATOR ", "»boolean «bit»«ENDFOR») {
            this.«bitsFieldName» = «BITS_TYPE_OBJECT_HELPERS.importedName».setBits(this.«bitsFieldName», «FOR bit : bitList SEPARATOR ", "»«bit»«ENDFOR»);
        }
     '''

    def allValuesConstructor() '''
    public «type.name»(«allProperties.asArgumentsDeclaration») {
        «IF !parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»
        «FOR p : allProperties»
            «generateRestrictions(type, p.fieldName, p.returnType)»
        «ENDFOR»

        «FOR p : properties»
            «val fieldName = p.fieldName»
            «IF p.returnType.name.endsWith("[]")»
                this.«fieldName» = «fieldName» == null ? null : «fieldName».clone();
            «ELSE»
                this.«fieldName» = «fieldName»;
            «ENDIF»
        «ENDFOR»
    }
    '''

    def private typedefConstructor() '''
    @«ConstructorParameters.importedName»("«TypeConstants.VALUE_PROP»")
    public «type.name»(«allProperties.asArgumentsDeclaration») {
        «IF !parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»
        «FOR p : allProperties»
            «generateRestrictions(type, p.fieldName, p.returnType)»
        «ENDFOR»
        «/*
         * If we have patterns, we need to apply them to the value field. This is a sad consequence of how this code is
         * structured.
         */»
        «CODEHELPERS.importedName».requireValue(_value);
        «genPatternEnforcer("_value")»

        «FOR p : properties»
            «val fieldName = p.fieldName»
            this.«fieldName» = «fieldName»«p.cloneCall»;
        «ENDFOR»
    }
    '''

    def protected genUnionConstructor() '''
    «FOR p : allProperties»
        «val List<GeneratedProperty> other = new ArrayList(properties)»
        «IF other.remove(p)»
            «genConstructor(p, other)»
        «ENDIF»
    «ENDFOR»
    '''

    def protected genConstructor(GeneratedProperty property, Iterable<GeneratedProperty> other) '''
    public «type.name»(«property.returnType.importedName + " " + property.name») {
        «IF !parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»

        «val fieldName = property.fieldName»
        «generateRestrictions(type, fieldName, property.returnType)»

        this.«fieldName» = «property.name»;
        «FOR p : other»
            this.«p.fieldName» = null;
        «ENDFOR»
    }
    '''

    def private genPatternEnforcer(String ref) '''
        «FOR c : consts»
            «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME»
            «CODEHELPERS.importedName».checkPattern(«ref», «Constants.MEMBER_PATTERN_LIST», «Constants.MEMBER_REGEX_LIST»);
            «ENDIF»
        «ENDFOR»
    '''

    def private static paramValue(Type returnType, String paramName) {
        if (returnType instanceof ConcreteType) {
            return paramName
        } else {
            return paramName + ".getValue()"
        }
    }

    def generateRestrictions(Type type, String paramName, Type returnType) '''
        «val restrictions = type.restrictions»
        «IF restrictions !== null»
            «IF restrictions.lengthConstraint.present || restrictions.rangeConstraint.present»
            if («paramName» != null) {
                «IF restrictions.lengthConstraint.present»
                    «LengthGenerator.generateLengthCheckerCall(paramName, paramValue(returnType, paramName))»
                «ENDIF»
                «IF restrictions.rangeConstraint.present»
                    «rangeGenerator.generateRangeCheckerCall(paramName, paramValue(returnType, paramName))»
                «ENDIF»
            }
            «ENDIF»
        «ENDIF»
    '''

    def protected copyConstructor() '''
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public «type.name»(«type.name» source) {
        «IF !parentProperties.empty»
            super(source);
        «ENDIF»
        «FOR p : properties»
            «val fieldName = p.fieldName»
            this.«fieldName» = source.«fieldName»;
        «ENDFOR»
    }
    '''

    def protected parentConstructor() '''
    /**
     * Creates a new instance from «genTO.superType.importedName»
     *
     * @param source Source object
     */
    public «type.name»(«genTO.superType.importedName» source) {
        super(source);
        «genPatternEnforcer("getValue()")»
    }
    '''

    def protected defaultInstance() '''
        «IF genTO.typedef && !allProperties.empty && !genTO.unionType && !isBitsTypeObject»
            «val prop = allProperties.get(0)»
            «val propType = prop.returnType»
            «IF !(INSTANCE_IDENTIFIER.identifier.equals(propType.identifier))»
            public static «genTO.name» getDefaultInstance(final String defaultValue) {
                «IF VALUEOF_TYPES.contains(propType)»
                    return new «genTO.name»(«propType.importedName».valueOf(defaultValue));
                «ELSEIF STRING_TYPE.equals(propType)»
                    return new «genTO.name»(defaultValue);
                «ELSEIF BINARY_TYPE.equals(propType)»
                    return new «genTO.name»(«Base64.importedName».getDecoder().decode(defaultValue));
                «ELSEIF EMPTY_TYPE.equals(propType)»
                    «Preconditions.importedName».checkArgument(defaultValue.isEmpty(), "Invalid value %s", defaultValue);
                    return new «genTO.name»(«Empty.importedName».value());
                «ELSE»
                    return new «genTO.name»(new «propType.importedName»(defaultValue));
                «ENDIF»
            }
            «ENDIF»
        «ENDIF»
    '''

    def protected propsAsArgs(Iterable<GeneratedProperty> properties) '''
        «FOR prop : properties SEPARATOR ","»
            "«prop.name»"
        «ENDFOR»
    '''

    /**
     * Template method which generates JAVA class declaration.
     *
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class declaration in JAVA format
     */
    def protected generateClassDeclaration(boolean isInnerClass) '''
        public«
        IF (isInnerClass)»«
            " static final "»«
        ELSEIF (type.abstract)»«
            " abstract "»«
        ELSE»«
            " "»«
        ENDIF»class «type.name»«
        IF (genTO.superType !== null)»«
            " extends "»«genTO.superType.importedName»«
        ENDIF»
        «IF (!type.implements.empty)»«
            " implements "»«
            FOR type : type.implements SEPARATOR ", "»«
                type.importedName»«
            ENDFOR»«
        ENDIF
    »'''

    /**
     * Template method which generates JAVA enum type.
     *
     * @return string with inner enum source code in JAVA format
     */
    def protected enumDeclarations() '''
        «IF !enums.empty»
            «FOR e : enums SEPARATOR "\n"»
                «new EnumTemplate(javaType.getEnclosedType(e.identifier), e).generateAsInnerClass»
            «ENDFOR»
        «ENDIF»
    '''

    def protected suidDeclaration() '''
        «IF genTO.SUID !== null»
            private static final long serialVersionUID = «genTO.SUID.value»L;
        «ENDIF»
    '''

    def protected annotationDeclaration() '''
        «IF genTO.getAnnotations !== null»
            «FOR e : genTO.getAnnotations»
                @«e.getName»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA constants.
     *
     * @return string with constants in JAVA format
     */
    def protected constantsDeclarations() '''
        «IF !consts.empty»
            «FOR c : consts»
                «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME»
                    «val cValue = c.value as Map<String, String>»
                    «val jurPatternRef = JUR_PATTERN.importedName»
                    public static final «JU_LIST.importedName»<String> «TypeConstants.PATTERN_CONSTANT_NAME» = «ImmutableList.importedName».of(«
                    FOR v : cValue.keySet SEPARATOR ", "»"«v.escapeJava»"«ENDFOR»);
                    «IF cValue.size == 1»
                        private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST» = «jurPatternRef».compile(«TypeConstants.PATTERN_CONSTANT_NAME».get(0));
                        private static final String «Constants.MEMBER_REGEX_LIST» = "«cValue.values.iterator.next.escapeJava»";
                    «ELSE»
                        private static final «jurPatternRef»[] «Constants.MEMBER_PATTERN_LIST» = «CODEHELPERS.importedName».compilePatterns(«TypeConstants.PATTERN_CONSTANT_NAME»);
                        private static final String[] «Constants.MEMBER_REGEX_LIST» = { «
                        FOR v : cValue.values SEPARATOR ", "»"«v.escapeJava»"«ENDFOR» };
                    «ENDIF»
                «ELSEIF c.name == TypeConstants.VALID_BITS_NAME»
                    «val cValue = c.value as Map<Uint32, String>»
                    public static final «ImmutableSet.importedName»<String> «TypeConstants.VALID_BITS_NAME» = «ImmutableSet.importedName».of(«
                    FOR v : cValue.values SEPARATOR ", "»"«v»"«ENDFOR»);
                «ELSE»
                    «emitConstant(c)»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA class attributes.
     *
     * @return string with the class attributes in JAVA format
     */
    def protected generateFields() '''
        «IF !properties.empty»
            «FOR f : properties»
                «IF isBitsTypeObject»protected«ELSE»private«ENDIF»«IF isReadOnly(f) && !isBitsTypeObject» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
    '''

    protected def isReadOnly(GeneratedProperty field) {
        return field.readOnly
    }

    /**
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() {
        val size = genTO.hashCodeIdentifiers.size
        if (size == 0) {
            return ""
        }
        return '''
            @«OVERRIDE.importedName»
            public int hashCode() {
                «IF size != 1»
                    final int prime = 31;
                    int result = 1;
                    «FOR property : genTO.hashCodeIdentifiers»
                        result = prime * result + «property.importedUtilClass».hashCode(«property.fieldName»);
                    «ENDFOR»
                    return result;
                «ELSE»
                    return «CODEHELPERS.importedName».wrapperHashCode(«genTO.hashCodeIdentifiers.get(0).fieldName»);
                «ENDIF»
            }
        '''
    }

    /**
     * Template method which generates the method <code>equals()</code>.
     *
     * @return string with the <code>equals()</code> method definition in JAVA format
     */
    def private generateEquals() '''
        «IF !genTO.equalsIdentifiers.empty»
            @«OVERRIDE.importedName»
            public final boolean equals(java.lang.Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof «type.name»)) {
                    return false;
                }
                final «type.name» other = («type.name») obj;
                «FOR property : genTO.equalsIdentifiers»
                    «val fieldName = property.fieldName»
                    if (!«property.importedUtilClass».equals(«fieldName», other.«fieldName»)) {
                        return false;
                    }
                «ENDFOR»
                return true;
            }
        «ENDIF»
    '''

    def private generateToString(Collection<GeneratedProperty> properties) '''
        «IF !properties.empty»
            @«OVERRIDE.importedName»
            public «STRING.importedName» toString() {
                final var helper = «MOREOBJECTS.importedName».toStringHelper(«type.importedName».class);
                «FOR property : properties»
                    «CODEHELPERS.importedName».appendValue(helper, "«property.name»", «property.fieldName»);
                «ENDFOR»
                return helper.toString();
            }
        «ENDIF»
    '''

    def GeneratedProperty getPropByName(String name) {
        for (GeneratedProperty prop : allProperties) {
            if (prop.name.equals(name)) {
                return prop;
            }
        }
        return null
    }
}
