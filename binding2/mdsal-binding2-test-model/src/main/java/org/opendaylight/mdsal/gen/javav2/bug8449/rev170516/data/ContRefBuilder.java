/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.data;

import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.data.Cont.Ref;

/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 */
public class ContRefBuilder {

    public static Ref getDefaultInstance (java.lang.String defaultValue) {
        throw new java.lang.UnsupportedOperationException("Not yet implemented");
    }

}
