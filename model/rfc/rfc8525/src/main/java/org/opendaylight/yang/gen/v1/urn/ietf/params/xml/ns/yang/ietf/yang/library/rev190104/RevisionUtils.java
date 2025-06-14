package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ImportOnlyModule;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * Utility methods for converting legacy RFC8525 Revision to and from various representations.
 */
public final class RevisionUtils {
    private static final ImportOnlyModule.@NonNull Revision EMPTY_REVISION = new ImportOnlyModule.Revision("");

    private RevisionUtils() {
        // Hidden on purpose
    }

    /**
     * Return an empty {@link ImportOnlyModule.Revision}.
     *
     * @return An empty Revision.
     */
    public static ImportOnlyModule.@NonNull Revision emptyRevision() {
        return EMPTY_REVISION;
    }

    public static ImportOnlyModule.@NonNull Revision fromString(final String defaultValue) {
        return defaultValue.isEmpty() ? EMPTY_REVISION
            : new ImportOnlyModule.Revision(new RevisionIdentifier(defaultValue));
    }

    /**
     * Create a {@link ImportOnlyModule.Revision} from an optional {@link Revision}.
     *
     * @param revision Optional {@link Revision}
     * @return A Revision
     * @throws NullPointerException if revision is null
     */
    public static ImportOnlyModule.@NonNull Revision fromYangCommon(final Optional<Revision> revision) {
        return revision.map(rev -> new ImportOnlyModule.Revision(new RevisionIdentifier(rev.toString())))
            .orElse(EMPTY_REVISION);
    }

    /**
     * Create an optional {@link Revision} from a {@link ImportOnlyModule.Revision}.
     *
     * @param revision A Revision
     * @return Optional {@link Revision}
     * @throws NullPointerException if revision is null
     */
    public static Optional<Revision> toYangCommon(final ImportOnlyModule.Revision revision) {
        final var id = revision.getRevisionIdentifier();
        return id != null ? Optional.of(Revision.of(id.getValue())) : Optional.empty();
    }
}
