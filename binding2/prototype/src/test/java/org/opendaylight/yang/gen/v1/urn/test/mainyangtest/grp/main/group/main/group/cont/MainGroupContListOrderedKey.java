package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont;
import org.opendaylight.yangtools.yang.binding.Identifier;
import java.util.Objects;


public class MainGroupContListOrderedKey
 implements Identifier<MainGroupContListOrdered> {
    private static final long serialVersionUID = -7926731649945418745L;
    private final java.lang.String _name1;


    public MainGroupContListOrderedKey(java.lang.String _name1) {
    
    
        this._name1 = _name1;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public MainGroupContListOrderedKey(MainGroupContListOrderedKey source) {
        this._name1 = source._name1;
    }


    public java.lang.String getName1() {
        return _name1;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(_name1);
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
        MainGroupContListOrderedKey other = (MainGroupContListOrderedKey) obj;
        if (!Objects.equals(_name1, other._name1)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrderedKey.class.getSimpleName()).append(" [");
        boolean first = true;
    
        if (_name1 != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_name1=");
            builder.append(_name1);
         }
        return builder.append(']').toString();
    }
}

