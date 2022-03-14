/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;

/**
 * Global view of an {@link AugmentableRuntimeType}, with all concrete specializations available through
 * {@link #referencingAugments()}. This is important when dealing with sharing incurred by Binding Spec's reuse of
 * constructs defined in a {@code grouping}.
 *
 * <p>
 * As an example, consider {@link ChoiceRuntimeType} and {@link CaseRuntimeType} relationship to {@link GeneratedType}s
 * in the following model:
 * <pre>
 *   <code>
 *     grouping grp {
 *       container foo {
 *         choice bar;
 *       }
 *     }
 *
 *     container foo {
 *       uses grp;
 *     }
 *
 *     container bar {
 *       uses grp;
 *     }
 *
 *     augment /foo/foo/bar {
 *       case baz
 *     }
 *
 *     augment /bar/foo/bar {
 *       case xyzzy;
 *     }
 *   </code>
 * </pre>
 * YANG view of what is valid in {@code /foo/foo/bar} differs from what is valid in {@code /bar/foo/bar}, but this
 * difference is not reflected in generated Java constructs. More notably, the two augments being in different modules.
 * Since {@code choice bar}'s is part of a reusable construct, {@code grouping one}, DataObjects' copy builders can
 * propagate them without translating them to the appropriate manifestation -- and they can do nothing about that as
 * they lack the complete view of the effective model.
 *
 * <p>
 * The correct approach to resolving this ambiguity is that we arrive at a {@link ChoiceRuntimeType} via the
 * {@link CompositeRuntimeType#bindingChild(org.opendaylight.mdsal.binding.model.api.JavaTypeName)} axis lookup
 * and this corresponds to the YANG-exact view of the data. We then attempt to interpret the DataObject layout in that
 * context. If we find that we have mismatched objects, we need to lookup the {@link ChoiceRuntimeType#getIdentifier()}
 * via {@link BindingRuntimeTypes#findSchema(org.opendaylight.mdsal.binding.model.api.JavaTypeName)}. In this particular
 * example that lookup will return a {@link ChoiceRuntimeType} corresponding to the original declaration site, e.g.
 * {@code //grp/foo/bar}, and it will implement this interface and report the two {@code augment} specializations via
 * {@link #referencingAugments()}.
 */
@Beta
public interface ReferencedAugmentableRuntimeType extends AugmentableRuntimeType {
    /**
     * Return the {@link AugmentRuntimeType}s extending any instantiation of this type.
     *
     * @return {@link AugmentRuntimeType}s extending any instantiation of this type.
     */
    @NonNull List<AugmentRuntimeType> referencingAugments();
}
