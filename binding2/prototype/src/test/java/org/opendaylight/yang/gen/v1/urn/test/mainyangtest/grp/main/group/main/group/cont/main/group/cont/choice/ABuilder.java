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
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A
 *
 */
public class ABuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A> {

    private java.lang.String _case1;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> augmentation = Collections.emptyMap();

    public ABuilder() {
    }

    public ABuilder(A base) {
        this._case1 = base.getCase1();
        if (base instanceof AImpl) {
            AImpl impl = (AImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public java.lang.String getCase1() {
        return _case1;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public ABuilder setCase1(final java.lang.String value) {
        this._case1 = value;
        return this;
    }
    
    public ABuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public ABuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public A build() {
        return new AImpl(this);
    }

    private static final class AImpl implements A {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A.class;
        }

        private final java.lang.String _case1;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> augmentation = Collections.emptyMap();

        private AImpl(ABuilder base) {
            this._case1 = base.getCase1();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.String getCase1() {
            return _case1;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_case1);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A)obj;
            if (!Objects.equals(_case1, other.getCase1())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                AImpl otherImpl = (AImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.main.group.cont.choice.A>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("A [");
            boolean first = true;
        
            if (_case1 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_case1=");
                builder.append(_case1);
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
