/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.BindingContract;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.ScalarTypeObject;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
public final class Naming {

    public static final @NonNull String VERSION = "0.6";

    // Note: these are not just JLS keywords, but rather character sequences which are reserved in codegen contexts
    public static final ImmutableSet<String> JAVA_RESERVED_WORDS = ImmutableSet.of(
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.9 except module-info.java constructs
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "_",
        // "open", "module", "requires", "transitive", "exports, "opens", "to", "uses", "provides", "with",

        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.10.3
        "false", "true",
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.10.7
        "null",
        // https://docs.oracle.com/javase/specs/jls/se10/html/jls-3.html#jls-3.9
        "var",
        // https://docs.oracle.com/javase/specs/jls/se14/html/jls-3.html#jls-3.9
        "yield",
        // https://docs.oracle.com/javase/specs/jls/se16/html/jls-3.html#jls-3.9
        "record");

    public static final @NonNull String DATA_ROOT_SUFFIX = "Data";
    @Deprecated(since = "11.0.0", forRemoval = true)
    public static final @NonNull String RPC_SERVICE_SUFFIX = "Service";
    @Deprecated(since = "10.0.3", forRemoval = true)
    public static final @NonNull String NOTIFICATION_LISTENER_SUFFIX = "Listener";
    public static final @NonNull String BUILDER_SUFFIX = "Builder";
    public static final @NonNull String KEY_SUFFIX = "Key";
    // ietf-restconf:yang-data, i.e. YangDataName
    public static final @NonNull String NAME_STATIC_FIELD_NAME = "NAME";
    // everything that can have a QName (e.g. identifier bound to a namespace)
    public static final @NonNull String QNAME_STATIC_FIELD_NAME = "QNAME";
    // concrete extensible contracts, for example 'feature', 'identity' and similar
    public static final @NonNull String VALUE_STATIC_FIELD_NAME = "VALUE";
    public static final @NonNull String PACKAGE_PREFIX = "org.opendaylight.yang.gen.v1";
    public static final @NonNull String AUGMENTATION_FIELD = "augmentation";

    private static final Splitter CAMEL_SPLITTER = Splitter.on(CharMatcher.anyOf(" _.-/").precomputed())
            .omitEmptyStrings().trimResults();
    private static final Pattern COLON_SLASH_SLASH = Pattern.compile("://", Pattern.LITERAL);
    private static final String QUOTED_DOT = Matcher.quoteReplacement(".");
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    public static final @NonNull String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    public static final @NonNull String MODULE_INFO_QNAMEOF_METHOD_NAME = "qnameOf";
    public static final @NonNull String MODULE_INFO_YANGDATANAMEOF_METHOD_NAME = "yangDataNameOf";
    public static final @NonNull String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";

    /**
     * Name of {@link Augmentable#augmentation(Class)}.
     */
    public static final @NonNull String AUGMENTABLE_AUGMENTATION_NAME = "augmentation";

    /**
     * Name of {@link Identifiable#key()}.
     */
    public static final @NonNull String IDENTIFIABLE_KEY_NAME = "key";

    /**
     * Name of {@link BindingContract#implementedInterface()}.
     */
    public static final @NonNull String BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME = "implementedInterface";

    /**
     * Name of default {@link Object#hashCode()} implementation for instantiated DataObjects. Each such generated
     * interface contains this static method.
     */
    public static final @NonNull String BINDING_HASHCODE_NAME = "bindingHashCode";

    /**
     * Name of default {@link Object#equals(Object)} implementation for instantiated DataObjects. Each such generated
     * interface contains this static method.
     */
    public static final @NonNull String BINDING_EQUALS_NAME = "bindingEquals";

    /**
     * Name of default {@link Object#toString()} implementation for instantiated DataObjects. Each such generated
     * interface contains this static method.
     */
    public static final @NonNull String BINDING_TO_STRING_NAME = "bindingToString";

    /**
     * Name of {@link Action#invoke(InstanceIdentifier, RpcInput)}.
     */
    public static final @NonNull String ACTION_INVOKE_NAME = "invoke";

    /**
     * Name of {@link Rpc#invoke(org.opendaylight.yangtools.yang.binding.RpcInput)}.
     */
    public static final @NonNull String RPC_INVOKE_NAME = "invoke";

    /**
     * Name of {@link ScalarTypeObject#getValue()}.
     */
    public static final @NonNull String SCALAR_TYPE_OBJECT_GET_VALUE_NAME = "getValue";

    /**
     * Prefix for normal getter methods.
     */
    public static final @NonNull String GETTER_PREFIX = "get";

    /**
     * Prefix for non-null default wrapper methods. These methods always wrap a corresponding normal getter.
     */
    public static final @NonNull String NONNULL_PREFIX = "nonnull";

    /**
     * Prefix for require default wrapper methods. These methods always wrap a corresponding normal getter
     * of leaf objects.
     */
    public static final @NonNull String REQUIRE_PREFIX = "require";
    public static final @NonNull String RPC_INPUT_SUFFIX = "Input";
    public static final @NonNull String RPC_OUTPUT_SUFFIX = "Output";

    private static final Interner<String> PACKAGE_INTERNER = Interners.newWeakInterner();
    @Regex
    private static final String ROOT_PACKAGE_PATTERN_STRING =
            "(org.opendaylight.yang.gen.v1.[a-z0-9_\\.]*?\\.(?:rev[0-9][0-9][0-1][0-9][0-3][0-9]|norev))";
    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);

    private Naming() {
        // Hidden on purpose
    }

    public static @NonNull String getClassName(final String localName) {
        return toFirstUpper(toCamelCase(localName));
    }

    public static @NonNull String getMethodName(final String yangIdentifier) {
        return toFirstLower(toCamelCase(yangIdentifier));
    }

    public static @NonNull String getMethodName(final QName name) {
        return getMethodName(name.getLocalName());
    }

    public static @NonNull String getPropertyName(final String yangIdentifier) {
        final String potential = toFirstLower(toCamelCase(yangIdentifier));
        if ("class".equals(potential)) {
            return "xmlClass";
        }
        return potential;
    }

    private static @NonNull String toCamelCase(final String rawString) {
        StringBuilder builder = new StringBuilder();
        for (String comp : CAMEL_SPLITTER.split(rawString)) {
            builder.append(toFirstUpper(comp));
        }
        return checkNumericPrefix(builder.toString());
    }

    private static @NonNull String checkNumericPrefix(final String rawString) {
        if (rawString.isEmpty()) {
            return rawString;
        }
        final char firstChar = rawString.charAt(0);
        return firstChar >= '0' && firstChar <= '9' ? "_" + rawString : rawString;
    }

    /**
     * Returns the {@link String} {@code s} with an {@link Character#isUpperCase(char) upper case} first character.
     *
     * @param str the string that should get an upper case first character.
     * @return the {@link String} {@code str} with an upper case first character.
     */
    public static @NonNull String toFirstUpper(final @NonNull String str) {
        if (str.isEmpty()) {
            return str;
        }
        if (Character.isUpperCase(str.charAt(0))) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase(Locale.ENGLISH);
        }
        return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
    }

    /**
     * Returns the {@link String} {@code s} with a {@link Character#isLowerCase(char) lower case} first character. This
     * function is null-safe.
     *
     * @param str the string that should get an lower case first character. May be <code>null</code>.
     * @return the {@link String} {@code str} with an lower case first character or <code>null</code> if the input
     *         {@link String} {@code str} was empty.
     */
    private static @NonNull String toFirstLower(final @NonNull String str) {
        if (str.isEmpty()) {
            return str;
        }
        if (Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase(Locale.ENGLISH);
        }
        return str.substring(0, 1).toLowerCase(Locale.ENGLISH) + str.substring(1);
    }

    /**
     * Returns the {@link String} {@code s} with a '$' character as suffix.
     *
     * @param qname RPC QName
     * @return The RPC method name as determined by considering the localname against the JLS.
     * @throws NullPointerException if {@code qname} is null
     * @deprecated This method is used only by either deprecated methods or deprecated classes.
     */
    @Deprecated
    public static @NonNull String getRpcMethodName(final @NonNull QName qname) {
        final String methodName = getMethodName(qname);
        return JAVA_RESERVED_WORDS.contains(methodName) ? methodName + "$" : methodName;
    }

    /**
     * Builds class name representing yang-data template name which is not yang identifier compliant.
     *
     * @param templateName template name
     * @return Java class name
     * @throws NullPointerException if {@code templateName} is {@code null}
     * @throws IllegalArgumentException if (@code templateName} is empty
     */
    // TODO: take YangDataName once we have it readily available
    public static String mapYangDataName(final String templateName) {
        return mapEnumAssignedName(templateName);
    }

    // See https://docs.oracle.com/javase/specs/jls/se16/html/jls-3.html#jls-3.8
    // TODO: we are being conservative here, but should differentiate TypeIdentifier and UnqualifiedMethodIdentifier,
    //       which have different exclusions
    public static boolean isValidJavaIdentifier(final String str) {
        return !str.isEmpty() && !JAVA_RESERVED_WORDS.contains(str)
                && Character.isJavaIdentifierStart(str.codePointAt(0))
                && str.codePoints().skip(1).allMatch(Character::isJavaIdentifierPart);
    }

    public static String mapEnumAssignedName(final String assignedName) {
        checkArgument(!assignedName.isEmpty());

        // Mapping rules:
        // - if the string is a valid java identifier and does not contain '$', use it as-is
        if (assignedName.indexOf('$') == -1 && isValidJavaIdentifier(assignedName)) {
            return assignedName;
        }

        // - otherwise prefix it with '$' and replace any invalid character (including '$') with '$XX$', where XX is
        //   hex-encoded unicode codepoint (including plane, stripping leading zeroes)
        final StringBuilder sb = new StringBuilder().append('$');
        assignedName.codePoints().forEachOrdered(codePoint -> {
            if (codePoint == '$' || !Character.isJavaIdentifierPart(codePoint)) {
                sb.append('$').append(Integer.toHexString(codePoint).toUpperCase(Locale.ROOT)).append('$');
            } else {
                sb.appendCodePoint(codePoint);
            }
        });
        return sb.toString();
    }
}
