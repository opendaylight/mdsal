package org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1
 *
 */
public class MainGroupCont1Builder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1> {

    private java.lang.String _importedAugmentedLeaf3;


    public MainGroupCont1Builder() {
    }

    public MainGroupCont1Builder(MainGroupCont1 base) {
        this._importedAugmentedLeaf3 = base.getImportedAugmentedLeaf3();
    }


    public java.lang.String getImportedAugmentedLeaf3() {
        return _importedAugmentedLeaf3;
    }

     
    public MainGroupCont1Builder setImportedAugmentedLeaf3(final java.lang.String value) {
        this._importedAugmentedLeaf3 = value;
        return this;
    }

    public MainGroupCont1 build() {
        return new MainGroupCont1Impl(this);
    }

    private static final class MainGroupCont1Impl implements MainGroupCont1 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1.class;
        }

        private final java.lang.String _importedAugmentedLeaf3;


        private MainGroupCont1Impl(MainGroupCont1Builder base) {
            this._importedAugmentedLeaf3 = base.getImportedAugmentedLeaf3();
        }

        @Override
        public java.lang.String getImportedAugmentedLeaf3() {
            return _importedAugmentedLeaf3;
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
            result = prime * result + Objects.hashCode(_importedAugmentedLeaf3);
        
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1 other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainGroupCont1)obj;
            if (!Objects.equals(_importedAugmentedLeaf3, other.getImportedAugmentedLeaf3())) {
                return false;
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainGroupCont1 [");
            boolean first = true;
        
            if (_importedAugmentedLeaf3 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_importedAugmentedLeaf3=");
                builder.append(_importedAugmentedLeaf3);
             }
            return builder.append(']').toString();
        }
    }

}
