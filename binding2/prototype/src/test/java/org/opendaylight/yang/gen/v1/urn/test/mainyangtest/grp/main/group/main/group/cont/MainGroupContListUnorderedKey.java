package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont;
import org.opendaylight.yangtools.yang.binding.Identifier;
import java.util.Objects;


public class MainGroupContListUnorderedKey
 implements Identifier<MainGroupContListUnordered> {
    private static final long serialVersionUID = 3614369169157466116L;
    private final java.lang.String _name2;


    public MainGroupContListUnorderedKey(java.lang.String _name2) {


        this._name2 = _name2;
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public MainGroupContListUnorderedKey(MainGroupContListUnorderedKey source) {
        this._name2 = source._name2;
    }


    public java.lang.String getName2() {
        return _name2;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(_name2);
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MainGroupContListUnorderedKey other = (MainGroupContListUnorderedKey) obj;
        if (!Objects.equals(_name2, other._name2)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnorderedKey.class.getSimpleName()).append(" [");
        boolean first = true;

        if (_name2 != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_name2=");
            builder.append(_name2);
         }
        return builder.append(']').toString();
    }
}

