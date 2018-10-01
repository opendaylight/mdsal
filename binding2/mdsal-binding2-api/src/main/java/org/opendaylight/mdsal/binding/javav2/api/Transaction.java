/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.api;

import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * A common parent for all transactions which operate on a conceptual data tree.
 * See derived transaction types for more concrete behavior:
 * <ul>
 * <li>{@link ReadTransaction} - Read capabilities, user is able to read data from data tree</li>
 * <li>{@link WriteTransaction} - Write capabilities, user is able to propose changes to data tree</li>
 * </ul>
 *
 * <b>Implementation Note:</b> This interface is not intended to be implemented by users of MD-SAL.
 */
public interface Transaction extends Identifiable<Object> {

}
