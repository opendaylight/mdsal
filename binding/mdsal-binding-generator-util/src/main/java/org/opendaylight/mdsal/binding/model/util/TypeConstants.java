/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

/**
 *
 * Contains constants used in relations with <code>Type</code>.
 */
public final class TypeConstants {

    /**
     * Name of the class constant which holds the list of the regular expression strings compatible with
     * {@link java.util.regex.Pattern}.
     */
    public static final String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";

    /**
     * Name of the class constant which holds the list of the XSD regular expression strings.
     */
    public static final String REGEX_CONSTANT_NAME = "REGEX_CONSTANTS";

    /**
     * Creation of new instance is prohibited.
     */
    private TypeConstants() {
    }
}
