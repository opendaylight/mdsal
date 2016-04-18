package org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1
 *
 */
public class MainCont1Builder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1> {

    private java.lang.String _importedAugmentedLeaf1;


    public MainCont1Builder() {
    }

    public MainCont1Builder(MainCont1 base) {
        this._importedAugmentedLeaf1 = base.getImportedAugmentedLeaf1();
    }


    public java.lang.String getImportedAugmentedLeaf1() {
        return _importedAugmentedLeaf1;
    }

     
    public MainCont1Builder setImportedAugmentedLeaf1(final java.lang.String value) {
        this._importedAugmentedLeaf1 = value;
        return this;
    }

    public MainCont1 build() {
        return new MainCont1Impl(this);
    }

    private static final class MainCont1Impl implements MainCont1 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1.class;
        }

        private final java.lang.String _importedAugmentedLeaf1;


        private MainCont1Impl(MainCont1Builder base) {
            this._importedAugmentedLeaf1 = base.getImportedAugmentedLeaf1();
        }

        @Override
        public java.lang.String getImportedAugmentedLeaf1() {
            return _importedAugmentedLeaf1;
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
            result = prime * result + Objects.hashCode(_importedAugmentedLeaf1);
        
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1 other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtestimporting.data.MainCont1)obj;
            if (!Objects.equals(_importedAugmentedLeaf1, other.getImportedAugmentedLeaf1())) {
                return false;
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainCont1 [");
            boolean first = true;
        
            if (_importedAugmentedLeaf1 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_importedAugmentedLeaf1=");
                builder.append(_importedAugmentedLeaf1);
             }
            return builder.append(']').toString();
        }
    }

}
