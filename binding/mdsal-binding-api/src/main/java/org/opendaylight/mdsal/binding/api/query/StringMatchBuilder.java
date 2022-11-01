/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Builder for a match of a String leaf value.
 *
 * @param <T> query result type
 */
@Beta
public non-sealed interface StringMatchBuilder<T extends DataObject> extends ValueMatchBuilder<T, String> {
    /**
     * Match if the leaf exists and its value starts with specified string, i.e. its {@link String#startsWith(String)}
     * would return true.
     *
     * @param str string to match as prefix
     * @return A ValueMatch
     * @throws NullPointerException if str is null
     */
    @NonNull ValueMatch<T> startsWith(String str);

    /**
     * Match if the leaf exists and its value ends with specified string. i.e. its {@link String#endsWith(String)}
     * would return true.
     *
     * @param str string to match as suffix
     * @return A ValueMatch
     * @throws NullPointerException if str is null
     */
    @NonNull ValueMatch<T> endsWith(String str);

    /**
     * Match if the leaf exists and its value contains specified string, i.e. its {@link String#contains(CharSequence)}
     * would return true.
     *
     * @param str the string to search for
     * @return A ValueMatch
     * @throws NullPointerException if str is null
     */
    @NonNull ValueMatch<T> contains(String str);

    /**
     * Match if the leaf exists and its value matches with specified pattern.
     *
     * @param pattern pattern to check against
     * @return A ValueMatch
     * @throws NullPointerException if pattern is null
     */
    @NonNull ValueMatch<T> matchesPattern(Pattern pattern);
}
