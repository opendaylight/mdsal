package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont
 *
 */
public class MainContBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont> {

    private MainGroupCont _mainGroupCont;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> augmentation = Collections.emptyMap();

    public MainContBuilder() {
    }
    public MainContBuilder(org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup arg) {
        this._mainGroupCont = arg.getMainGroupCont();
    }

    public MainContBuilder(MainCont base) {
        this._mainGroupCont = base.getMainGroupCont();
        if (base instanceof MainContImpl) {
            MainContImpl impl = (MainContImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn.test.mainyangtest.rev160101.MainGroup</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(TreeNode arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup) {
            this._mainGroupCont = ((org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.MainGroup)arg).getMainGroupCont();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.urn.test.mainyangtest.rev160101.MainGroup] \n" +
              "but was: " + arg
            );
        }
    }

    public MainGroupCont getMainGroupCont() {
        return _mainGroupCont;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public MainContBuilder setMainGroupCont(final MainGroupCont value) {
        this._mainGroupCont = value;
        return this;
    }
    
    public MainContBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public MainContBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public MainCont build() {
        return new MainContImpl(this);
    }

    private static final class MainContImpl implements MainCont {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont.class;
        }

        private final MainGroupCont _mainGroupCont;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> augmentation = Collections.emptyMap();

        private MainContImpl(MainContBuilder base) {
            this._mainGroupCont = base.getMainGroupCont();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public MainGroupCont getMainGroupCont() {
            return _mainGroupCont;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_mainGroupCont);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont)obj;
            if (!Objects.equals(_mainGroupCont, other.getMainGroupCont())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                MainContImpl otherImpl = (MainContImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.data.MainCont>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainCont [");
            boolean first = true;
        
            if (_mainGroupCont != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainGroupCont=");
                builder.append(_mainGroupCont);
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
