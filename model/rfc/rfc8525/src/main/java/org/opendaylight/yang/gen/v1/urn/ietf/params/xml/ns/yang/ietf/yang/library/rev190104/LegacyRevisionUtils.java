/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * Utility methods for converting legacy RFC7895 {@code modules-state} Revision to and from various representations.
 */
@SuppressWarnings("deprecation")
public final class LegacyRevisionUtils {
    private static final CommonLeafs.@NonNull Revision EMPTY_REVISION = new CommonLeafs.Revision("");

    private LegacyRevisionUtils() {
        // Hidden on purpose
    }

    /**
     * Return an empty {@link CommonLeafs.Revision}.
     *
     * @return An empty Revision.
     */
    public static CommonLeafs.@NonNull Revision emptyRevision() {
        return EMPTY_REVISION;
    }

    public static CommonLeafs.@NonNull Revision fromString(final String defaultValue) {
        return defaultValue.isEmpty() ? EMPTY_REVISION
            : new CommonLeafs.Revision(new RevisionIdentifier(defaultValue));
    }

    /**
     * Create a {@link CommonLeafs.Revision} from an optional {@link Revision}.
     *
     * @param revision Optional {@link Revision}
     * @return A Revision
     * @throws NullPointerException if {@code revision} is null
     */
    public static CommonLeafs.@NonNull Revision fromYangCommon(final Optional<Revision> revision) {
        return revision.map(rev -> new CommonLeafs.Revision(new RevisionIdentifier(rev.toString())))
            .orElse(EMPTY_REVISION);
    }

    /**
     * Create an optional {@link Revision} from a {@link CommonLeafs.Revision}.
     *
     * @param revision A Revision
     * @return Optional {@link Revision}
     * @throws NullPointerException if {@code revision} is null
     */
    public static Optional<Revision> toYangCommon(final CommonLeafs.Revision revision) {
        final var id = revision.getRevisionIdentifier();
        return id != null ? Optional.of(Revision.of(id.getValue())) : Optional.empty();
    }
}

