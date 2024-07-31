/*
 * Copyright (c) 2023-2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

/**
 * Path Argument types used in Potassium encoding. These are encoded as a single byte, two bits of which are reserved
 * for PathArgument type itself:
 * <pre>
 *   7 6 5 4 3 2 1 0
 *  +-+-+-+-+-+-+-+-+
 *  |           |Typ|
 *  +-+-+-+-+-+-+-+-+
 * </pre>
 * There are three type defined:
 * <ul>
 *   <li>{@link #NODE_IDENTIFIER}, which encodes a QName:
 *     <pre>
 *       7 6 5 4 3 2 1 0
 *      +-+-+-+-+-+-+-+-+
 *      |0 0 0 0| Q |0 1|
 *      +-+-+-+-+-+-+-+-+
 *     </pre>
 *     Here {@code Q} refers to one of the four possible QName encodings: {@link #QNAME_DEF}, {@link #QNAME_REF_1B},
 *     {@link #QNAME_REF_2B} or {@link #QNAME_REF_4B}.
 *   <li>{@link #NODE_IDENTIFIER_WITH_PREDICATES}, which encodes a QName same way NodeIdentifier does:
 *     <pre>
 *       7 6 5 4 3 2 1 0
 *      +-+-+-+-+-+-+-+-+
 *      | Size  | Q |1 0|
 *      +-+-+-+-+-+-+-+-+
 *      </pre>
 *      but additionally encodes number of predicates contained using {@link #SIZE_0} through {@link #SIZE_4}. If that
 *      number cannot be expressed, {@link #SIZE_1B}, {@value #SIZE_2B} and {@link #SIZE_4B} indicate number and format
 *      of additional bytes that hold number of predicates.
 *
 *      <p>
 *      This is then followed by the specified number of QName/Object key/value pairs based on {@link PotassiumValue}
 *      encoding.
 *   </li>
 *   <li>{@link #NODE_WITH_VALUE}, which encodes a QName same way NodeIdentifier does:
 *     <pre>
 *       7 6 5 4 3 2 1 0
 *      +-+-+-+-+-+-+-+-+
 *      |0 0 0 0| Q |1 1|
 *      +-+-+-+-+-+-+-+-+
 *     </pre>
 *     but is additionally followed by a single encoded value, as per {@link PotassiumValue}.
 *   </li>
 *   </li>
 * </ul>
 */
final class PotassiumPathArgument {
    // 2 bits reserved for type...
    // 0x00 reserved
    static final byte NODE_IDENTIFIER                 = 0x01;
    static final byte NODE_IDENTIFIER_WITH_PREDICATES = 0x02;
    static final byte NODE_WITH_VALUE                 = 0x03;
    static final byte TYPE_MASK                       = NODE_WITH_VALUE;

    // For NodeIdentifierWithPredicates we also carry the number of subsequent path arguments. The case of 0-12
    // arguments is indicated directly, otherwise there is 1-4 bytes carrying the reference.
    static final byte SIZE_1B                         = (byte) 0xA0;
    static final byte SIZE_2B                         = (byte) 0xC0;
    static final byte SIZE_4B                         = (byte) 0xE0;
    static final byte SIZE_MASK                       = (byte) 0xF0;
    static final byte SIZE_SHIFT                      = 4;

    private PotassiumPathArgument() {

    }
}
