/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.replaceAllIllegalChars;
import static org.opendaylight.mdsal.binding.javav2.generator.util.FormattingUtils.formatToParagraph;

import com.google.common.annotations.Beta;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.util.Optional;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.javav2.model.api.TypeComment;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * Utility methods for creating {@link TypeComment}s.
 */
@Beta
@NonNullByDefault
public final class TypeComments {
    private static final Escaper ENTITY_ESCAPER = Escapers.builder()
            .addEscape('<', "&lt;")
            .addEscape('>', "&gt;")
            .addEscape('&', "&amp;")
            .addEscape('@', "&#64;").build();
    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);

    private TypeComments() {
        //Defeat initialization.
    }

    /**
     * Create a {@link TypeComment} for a javadoc-compliant text snippet. This snippet must be eligible for direct
     * inclusion in a Java comment without further escaping.
     *
     * @param javadoc Pre-formatted javadoc snippet
     * @return {@link TypeComment}, or empty if the snippet was empty
     */
    public static Optional<TypeComment> javadoc(final String javadoc) {
        return javadoc.isEmpty() ? Optional.empty() : Optional.of(() -> javadoc);
    }

    /**
     * Create a {@link TypeComment} for a {@link DocumentedNode}'s description string.
     *
     * @param node Documented node containing the description to be processed
     * @return {@link TypeComment}, or empty if the node's description was empty or non-present.
     */
    public static Optional<TypeComment> description(final DocumentedNode node) {
        final String description = node.getDescription().orElse("");
        return description.isEmpty() ? Optional.empty() : Optional.of(() -> replaceAllIllegalChars(
            formatToParagraph(
                TAIL_COMMENT_PATTERN.matcher(ENTITY_ESCAPER.escape(description)).replaceAll("&#42;&#47;"), 0)));
    }
}
