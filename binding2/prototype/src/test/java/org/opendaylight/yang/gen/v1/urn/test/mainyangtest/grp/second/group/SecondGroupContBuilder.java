package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont
 *
 */
public class SecondGroupContBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont> {

    private java.lang.String _secondGroupContLeaf;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> augmentation = Collections.emptyMap();

    public SecondGroupContBuilder() {
    }

    public SecondGroupContBuilder(SecondGroupCont base) {
        this._secondGroupContLeaf = base.getSecondGroupContLeaf();
        if (base instanceof SecondGroupContImpl) {
            SecondGroupContImpl impl = (SecondGroupContImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public java.lang.String getSecondGroupContLeaf() {
        return _secondGroupContLeaf;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public SecondGroupContBuilder setSecondGroupContLeaf(final java.lang.String value) {
        this._secondGroupContLeaf = value;
        return this;
    }
    
    public SecondGroupContBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public SecondGroupContBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public SecondGroupCont build() {
        return new SecondGroupContImpl(this);
    }

    private static final class SecondGroupContImpl implements SecondGroupCont {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont.class;
        }

        private final java.lang.String _secondGroupContLeaf;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> augmentation = Collections.emptyMap();

        private SecondGroupContImpl(SecondGroupContBuilder base) {
            this._secondGroupContLeaf = base.getSecondGroupContLeaf();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.String getSecondGroupContLeaf() {
            return _secondGroupContLeaf;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
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
            result = prime * result + Objects.hashCode(_secondGroupContLeaf);
            result = prime * result + Objects.hashCode(augmentation);
        
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont)obj;
            if (!Objects.equals(_secondGroupContLeaf, other.getSecondGroupContLeaf())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                SecondGroupContImpl otherImpl = (SecondGroupContImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("SecondGroupCont [");
            boolean first = true;
        
            if (_secondGroupContLeaf != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_secondGroupContLeaf=");
                builder.append(_secondGroupContLeaf);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
