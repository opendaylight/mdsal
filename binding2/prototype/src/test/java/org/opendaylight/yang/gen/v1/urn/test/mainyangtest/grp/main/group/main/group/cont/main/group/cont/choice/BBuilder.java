package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B
 *
 */
public class BBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B> {

    private java.lang.String _case21;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> augmentation = Collections.emptyMap();

    public BBuilder() {
    }

    public BBuilder(B base) {
        this._case21 = base.getCase21();
        if (base instanceof BImpl) {
            BImpl impl = (BImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public java.lang.String getCase21() {
        return _case21;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public BBuilder setCase21(final java.lang.String value) {
        this._case21 = value;
        return this;
    }
    
    public BBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public BBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public B build() {
        return new BImpl(this);
    }

    private static final class BImpl implements B {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B.class;
        }

        private final java.lang.String _case21;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> augmentation = Collections.emptyMap();

        private BImpl(BBuilder base) {
            this._case21 = base.getCase21();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.String getCase21() {
            return _case21;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_case21);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B)obj;
            if (!Objects.equals(_case21, other.getCase21())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                BImpl otherImpl = (BImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.B>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("B [");
            boolean first = true;
        
            if (_case21 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_case21=");
                builder.append(_case21);
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
