package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters;

import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ImportOnlyModule.Revision;

public final class ImportOnlyModuleRevisionBuilder {
    private static final Revision EMPTY_REVISION = new Revision("");

    private ImportOnlyModuleRevisionBuilder() {

    }

    /**
     * Return an empty {@link Revision}.
     *
     * @return An empty Revision.
     */
    public static Revision emptyRevision() {
        return EMPTY_REVISION;
    }

    /**
     * Create a {@link Revision} from an optional {@link org.opendaylight.yangtools.yang.common.Revision}.
     *
     * @param revision Optional {@link org.opendaylight.yangtools.yang.common.Revision}
     * @return A Revision
     * @throws NullPointerException if revision is null
     */
    public static Revision fromYangCommon(final Optional<org.opendaylight.yangtools.yang.common.Revision> revision) {
        return revision.map(rev -> new Revision(new RevisionIdentifier(rev.toString()))).orElse(EMPTY_REVISION);
    }

    /**
     * Create an optional {@link org.opendaylight.yangtools.yang.common.Revision} from a {@link Revision}.
     *
     * @param revision A Revision
     * @return Optional {@link org.opendaylight.yangtools.yang.common.Revision}
     * @throws NullPointerException if revision is null
     */
    public static Optional<org.opendaylight.yangtools.yang.common.Revision> toYangCommon(final Revision revision) {
        final @Nullable RevisionIdentifier id = revision.getRevisionIdentifier();
        return id != null ? Optional.of(org.opendaylight.yangtools.yang.common.Revision.of(id.getValue()))
                : Optional.empty();
    }

    public static Revision getDefaultInstance(final String defaultValue) {
        return defaultValue.isEmpty() ? EMPTY_REVISION : new Revision(new RevisionIdentifier(defaultValue));
    }
}
