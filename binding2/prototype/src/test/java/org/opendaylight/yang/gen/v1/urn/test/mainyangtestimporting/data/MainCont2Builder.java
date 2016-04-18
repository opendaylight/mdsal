package org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2
 *
 */
public class MainCont2Builder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2> {

    private java.lang.String _importedAugmentedLeaf2;


    public MainCont2Builder() {
    }

    public MainCont2Builder(MainCont2 base) {
        this._importedAugmentedLeaf2 = base.getImportedAugmentedLeaf2();
    }


    public java.lang.String getImportedAugmentedLeaf2() {
        return _importedAugmentedLeaf2;
    }

     
    public MainCont2Builder setImportedAugmentedLeaf2(final java.lang.String value) {
        this._importedAugmentedLeaf2 = value;
        return this;
    }

    public MainCont2 build() {
        return new MainCont2Impl(this);
    }

    private static final class MainCont2Impl implements MainCont2 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2.class;
        }

        private final java.lang.String _importedAugmentedLeaf2;


        private MainCont2Impl(MainCont2Builder base) {
            this._importedAugmentedLeaf2 = base.getImportedAugmentedLeaf2();
        }

        @Override
        public java.lang.String getImportedAugmentedLeaf2() {
            return _importedAugmentedLeaf2;
        }

        private int hash = 0;
        private volatile boolean hashValid = false;
        
        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }
        
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(_importedAugmentedLeaf2);
        
            hash = result;
            hashValid = true;
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TreeNode)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2 other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont2)obj;
            if (!Objects.equals(_importedAugmentedLeaf2, other.getImportedAugmentedLeaf2())) {
                return false;
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainCont2 [");
            boolean first = true;
        
            if (_importedAugmentedLeaf2 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_importedAugmentedLeaf2=");
                builder.append(_importedAugmentedLeaf2);
             }
            return builder.append(']').toString();
        }
    }

}
