/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Simple type-safe query language based on YANG concepts as manifested by its Java Bindings.
 *
 * <p>
 * The API has two primary entry points:
 * <ul>
 *   <li>{@link org.opendaylight.mdsal.binding.api.query.QueryFactory}, which provides the entrypoint into an
 *       implementation which can build query instances</li>
 *   <li>{@link org.opendaylight.mdsal.binding.api.query.QueryExpression}, which is an immutable expression and can be used with
 *       various binding APIs to perform query execution.</li>
 * </ul>
 */
package org.opendaylight.mdsal.binding.api.query;
