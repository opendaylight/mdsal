package org.opendaylight.mdsal.dom.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

public interface DOMCursorAwareWriteTransaction {

    /**
     * Create a new {@link DataTreeModificationCursor} at specified path. May fail if specified path
     * does not exist. It is a programming error to use normal
     *
     *
     * @param path Path at which the cursor is to be anchored
     * @return A new cursor, or null if the path does not exist.
     * @throws IllegalStateException if there is another cursor currently open, or the modification
     *         is already {@link #ready()}.
     */
    @Nullable
    DataTreeModificationCursor createCursor(@Nonnull DOMDataTreeIdentifier path);

}
