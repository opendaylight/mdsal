/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An opaque query expression. A query execution results in a {@link QueryResult}, which is composed of zero or more
 * objects of the same type. Implementations of this interface are expected to be effectively-immutable and therefore
 * thread-safe and reusable.
 *
 * <p>
 * While this interface does not expose any useful methods, it represents a well-defined concept, which is composed of
 * three distinct parts:
 * <ul>
 *   <li>root path, which defines the subtree on which the expression is executed</li>
 *   <li>select path, which is a strict subset of the root path and defines which objects should be selected</li>
 *   <li>a set of predicates, which are to be evaluated on selected objects</li>
 * </ul>
 * When the expression is evaluated, its QueryResult will contain only those selected objects which also match all
 * predicates in the expression.
 *
 * <p>
 * For the purposes of illustration of how these three parts work together, let's imagine the following simple model:
 *
 * <pre>
 *   module foo {
 *     list foo {
 *       key name;
 *
 *       leaf name {
 *         type string;
 *       }
 *
 *       leaf alias {
 *         type string;
 *       }
 *
 *       container bar {
 *         list baz {
 *           key id;
 *
 *           leaf id {
 *             type uint64;
 *           }
 *
 *           leaf value {
 *             type string;
 *           }
 *         }
 *       }
 *     }
 *   }
 * </pre>
 *
 * <p>
 * We have two nested lists, each having two leaves -- one addressable as a key, one a plain property. There is a number
 * of different queries we could perform on such a model:
 * <ol>
 *   <li>select all {@code baz}es which have {@code value="foo"}</li>
 *   <li>select all {@code baz}es under {@code foo[name="xyzzy"]}, which have {@code value="foo"}</li>
 *   <li>select all {@code foo}s which have {@code alias="xyzzy"}</li>
 *   <li>select all {@code foo}s which have {@code alias="xyzzy"} and contain a {@code baz[value="foo"]}</li>
 * </ol>
 *
 * <p>
 * Note how the first and second options differ in what is being searched:
 * <ul>
 *   <li>search for all {@code baz} entries needs to traverse all {@code foo} entries</li>
 *   <li>search for all {@code baz} entries for {@code foo[name="xyzzy"]} needs to traverse only a single
 *       directly-addressable entry.</li>
 * </ul>
 * The distinction here is made by selecting different root paths: the first will specify the entire {@code foo} list,
 * while the second will select a specific {@code foo} entry.
 *
 * @param <T> Result object type
 */
@Beta
public interface QueryExpression<T extends DataObject> extends Immutable {

}
