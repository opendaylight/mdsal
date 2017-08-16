/*
 * Copyright (c) 2017 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class that checks whether {@link String} can represent Ipv6Address
 */
public final class Ipv6Matcher {

    private static final List<Pattern> PATTERNS =
        Ipv6Address.PATTERN_CONSTANTS.stream().map(a -> Pattern.compile(a)).collect(Collectors.toList());

    public static boolean matches(String candidate) {
        return PATTERNS.stream().anyMatch(pattern -> pattern.matcher(candidate).matches());
    }
}
