/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

/**
 * A combination of {@link ReadOperations} and {@link WriteOperations} as supported by {@link ReadWriteTransaction},
 * without a tie-in into lifecycle management.
 */
public interface ReadWriteOperations extends ReadOperations, WriteOperations {

}
