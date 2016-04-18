package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered
 *
 */
public class MainGroupContListOrderedBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered> {

    private MainGroupContListOrderedKey _key;
    private java.lang.String _name1;
    private java.lang.String _type1;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> augmentation = Collections.emptyMap();

    public MainGroupContListOrderedBuilder() {
    }

    public MainGroupContListOrderedBuilder(MainGroupContListOrdered base) {
        if (base.identifier() == null) {
            this._key = new MainGroupContListOrderedKey(
                base.getName1()
            );
            this._name1 = base.getName1();
        } else {
            this._key = base.identifier();
            this._name1 = _key.getName1();
        }
        this._type1 = base.getType1();
        if (base instanceof MainGroupContListOrderedImpl) {
            MainGroupContListOrderedImpl impl = (MainGroupContListOrderedImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public MainGroupContListOrderedKey getKey() {
        return _key;
    }
    
    public java.lang.String getName1() {
        return _name1;
    }
    
    public java.lang.String getType1() {
        return _type1;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public MainGroupContListOrderedBuilder setKey(final MainGroupContListOrderedKey value) {
        this._key = value;
        return this;
    }
    
     
    public MainGroupContListOrderedBuilder setName1(final java.lang.String value) {
        this._name1 = value;
        return this;
    }
    
     
    public MainGroupContListOrderedBuilder setType1(final java.lang.String value) {
        this._type1 = value;
        return this;
    }
    
    public MainGroupContListOrderedBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public MainGroupContListOrderedBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public MainGroupContListOrdered build() {
        return new MainGroupContListOrderedImpl(this);
    }

    private static final class MainGroupContListOrderedImpl implements MainGroupContListOrdered {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered.class;
        }

        private final MainGroupContListOrderedKey _key;
        private final java.lang.String _name1;
        private final java.lang.String _type1;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> augmentation = Collections.emptyMap();

        private MainGroupContListOrderedImpl(MainGroupContListOrderedBuilder base) {
            if (base.getKey() == null) {
                this._key = new MainGroupContListOrderedKey(
                    base.getName1()
                );
                this._name1 = base.getName1();
            } else {
                this._key = base.getKey();
                this._name1 = _key.getName1();
            }
            this._type1 = base.getType1();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public MainGroupContListOrderedKey identifier() {
            return _key;
        }
        
        @Override
        public java.lang.String getName1() {
            return _name1;
        }
        
        @Override
        public java.lang.String getType1() {
            return _type1;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_key);
            result = prime * result + Objects.hashCode(_name1);
            result = prime * result + Objects.hashCode(_type1);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered)obj;
            if (!Objects.equals(_key, other.identifier())) {
                return false;
            }
            if (!Objects.equals(_name1, other.getName1())) {
                return false;
            }
            if (!Objects.equals(_type1, other.getType1())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                MainGroupContListOrderedImpl otherImpl = (MainGroupContListOrderedImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainGroupContListOrdered [");
            boolean first = true;
        
            if (_key != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_key=");
                builder.append(_key);
             }
            if (_name1 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_name1=");
                builder.append(_name1);
             }
            if (_type1 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_type1=");
                builder.append(_type1);
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
