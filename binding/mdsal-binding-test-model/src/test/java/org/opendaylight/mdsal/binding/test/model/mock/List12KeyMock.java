package org.opendaylight.mdsal.binding.test.model.mock;
import com.google.common.base.MoreObjects;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.Identifier;

/**
 * This class represents the key of {@link List12} class.
 *
 * @see List12
 *
 */
@Generated("mdsal-binding-generator")
public class List12KeyMock
        implements Identifier<List12Mock> {
    private static final long serialVersionUID = 4712004729913052166L;
    private final Integer _attrInt;


    /**
     * Constructs an instance.
     *
     * @param _attrInt the entity attrInt
     * @throws NullPointerException if any of the arguments are null
     */
    public List12KeyMock(@NonNull Integer _attrInt) {
        this._attrInt = CodeHelpers.requireKeyProp(_attrInt, "attrInt");
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public List12KeyMock(List12KeyMock source) {
        this._attrInt = source._attrInt;
    }


    /**
     * Return attrInt, guaranteed to be non-null.
     *
     * @return {@code Integer} attrInt, guaranteed to be non-null.
     */
    public @NonNull Integer getAttrInt() {
        return _attrInt;
    }


    @Override
    public int hashCode() {
        return CodeHelpers.wrapperHashCode(_attrInt);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj || obj instanceof List12KeyMock other
                && Objects.equals(_attrInt, other._attrInt);
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of
                        .migration.test.model.rev150210.aug.grouping.list1.List12Key.class);
        CodeHelpers.appendValue(helper, "attrInt", _attrInt);
        return helper.toString();
    }
}


