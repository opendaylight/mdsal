/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.BuilderVisibility;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Structured comment of a particular class member. This is aimed towards unifying the layout of a particular type.
 */
@Beta
@Value.Immutable
@Value.Style(builderVisibility = BuilderVisibility.PUBLIC, implementationNestedInBuilder = true)
public interface TypeMemberComment extends Immutable {
    /**
     * Return the member contract description. This string, if present will represent the equivalent of the words you
     * are just reading. This forms what is usually:
     * <ul>
     *   <li>hand-written with careful explanation</li>
     *   <li>describing the general contract outline, what the member does/holds/etc. For methods this might be pre-
     *       and post-conditions.</li>
     * </ul>
     *
     * @return The equivalent of the above blurb.
     */
    Optional<String> contractDescription();

    /**
     * Return the member reference description. This description is passed unmodified, pre-formatted in a single block.
     * It is expected to look something like the following paragraph:
     *
     * <p>
     * <pre>
     *   <code>
     *     A 32-bit bit unsigned word. Individual bits are expected to be interpreted as follows:
     *
     *       31
     *     +----+ ...
     *   </code>
     * </pre>
     *
     * @return The equivalent of the above pre-formmated paragraph.
     */
    Optional<String> referenceDescription();

    /**
     * Return the type signature of this type member. This is only applicable for methods, use of anywhere else is
     * expected to either be ignored, or processed as is. As a matter of example, this method has a signature starting
     * right after this period<b>.</b>
     *
     * @return Return the signature description, just like these words right here
     */
    Optional<String> typeSignature();

    static TypeMemberCommentBuilder builder() {
        return new TypeMemberCommentBuilder();
    }

    static TypeMemberComment contractOf(final String contractDescription) {
        return new TypeMemberCommentBuilder().contractDescription(contractDescription).build();
    }

    static TypeMemberComment referenceOf(final String referenceDescription) {
        return new TypeMemberCommentBuilder().referenceDescription(referenceDescription).build();
    }

}
