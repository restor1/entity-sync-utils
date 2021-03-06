package me.vukas.common.entity.operation;

import me.vukas.common.entity.*;
import me.vukas.common.entity.element.Element;
import me.vukas.common.entity.element.LeafElement;
import me.vukas.common.entity.element.NodeElement;
import me.vukas.common.entity.generation.array.ArrayEntityGeneration;
import me.vukas.common.entity.generation.map.MapEntryEntityGeneration;
import me.vukas.common.entity.key.CircularKey;
import me.vukas.common.entity.key.Key;
import me.vukas.common.entity.key.LeafKey;
import me.vukas.common.entity.key.NodeKey;

import java.lang.reflect.Field;
import java.util.*;

import static me.vukas.common.base.Objects.*;

public class Diff {
    private Compare compare;
    private Clone clone;
    private final Stack<Object> visitedElements = new Stack<Object>();
    private final Stack<Object> visitedKeys = new Stack<Object>();

    private final Map<Object, List<LeafKey>> visitedCircularKeys = new HashMap<Object, List<LeafKey>>();
    private final Map<Object, CircularKey> rootCircularKeys = new HashMap<Object, CircularKey>();

    private final Map<Object, Object> originalToRevisedElements = new HashMap<Object, Object>();
    private final Map<Object, Object> revisedToOriginalElements = new HashMap<Object, Object>();

    private final Map<Class, EntityDefinition> typesToEntityDefinitions;
    private final Map<Class, IgnoredFields> typesToIgnoredFields;
    private final List<EntityGeneration<?>> entityGenerations;

    @SuppressWarnings("unchecked")
    private Diff(Builder builder) {
        this.typesToEntityDefinitions = builder.typesToEntityDefinitions;
        this.entityGenerations = builder.entityGenerations;
        this.typesToIgnoredFields = builder.typesToIgnoredFields;

        this.registerInternalEntityGenerations();

        for (EntityGeneration entityGeneration : this.entityGenerations) {
            entityGeneration.setDiff(this);
        }

        this.compare = new Compare.Builder()
                .registerEntities(new ArrayList<EntityDefinition>(this.typesToEntityDefinitions.values()))
                .registerEntityGenerations((List<EntityComparison<?>>) (List<?>) this.entityGenerations).build();

        this.clone = new Clone(this);
    }

    protected Diff(Map<Class, IgnoredFields> typesToIgnoredFields, Clone clone) {
        this.typesToEntityDefinitions = new HashMap<Class, EntityDefinition>();
        this.entityGenerations = new ArrayList<EntityGeneration<?>>();
        this.typesToIgnoredFields = typesToIgnoredFields;

        this.registerInternalEntityGenerations();

        for (EntityGeneration entityGeneration : this.entityGenerations) {
            entityGeneration.setDiff(this);
        }

        this.compare = new Compare.Builder()
                .registerEntities(new ArrayList<EntityDefinition>(this.typesToEntityDefinitions.values()))
                .registerEntityGenerations((List<EntityComparison<?>>) (List<?>) this.entityGenerations).build();

        this.clone = clone;
    }

    private void registerInternalEntityGenerations() {
        this.entityGenerations.add(new MapEntryEntityGeneration());
    }

    public <T> Element<Name, T> diff(T original, T revised) {
        Class revisedClass = revised == null ? null : revised.getClass();
        Key<Name, T> rootKey = this.generateKey(Name.ROOT, revisedClass, null, original);
        Element<Name, T> result = this.diff(original, revised, Name.ROOT, revisedClass, null, rootKey);
        this.originalToRevisedElements.clear();
        this.revisedToOriginalElements.clear();
        this.visitedCircularKeys.clear();
        this.rootCircularKeys.clear();
        return result;
    }

    public <N, T> Element<N, T> diff(T original, T revised, N elementName, Class fieldType, Class containerType, Key<N, T> key) {

        if (original == revised) {

            if(fieldType!=null){
                if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) {
                    this.visitedElements.push(original);
                    EntityGeneration<T> entityGeneration = new ArrayEntityGeneration<T>(this, this.compare);
                    Element<N, T> element = entityGeneration.diff(original, revised, elementName, fieldType, containerType, key);
                    this.visitedElements.pop();
                    return element;
                }

                for (EntityGeneration<?> entityGeneration : this.entityGenerations) { //TODO: class hierarchy priority
                    if (entityGeneration.getType().isAssignableFrom(fieldType)) {
                        this.visitedElements.push(original);
                        EntityGeneration<T> entityGenerationCasted = (EntityGeneration<T>) entityGeneration;
                        Element<N, T> element = entityGenerationCasted.diff(original, revised, elementName, fieldType, containerType, key);
                        this.visitedElements.pop();
                        return element;
                    }
                }
            }

            if (this.rootCircularKeys.containsKey(revised) && (this.originalToRevisedElements.containsKey(revised) || this.revisedToOriginalElements.containsKey(revised))) {

                return this.diff(this.getRevisedIfCircularReference(revised), revised, elementName, fieldType, containerType, key);
            }

            return new LeafElement<N, T>(elementName, Element.Status.EQUAL, key, revised);
        }

        if (original == null) {


            if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) {
                EntityGeneration<T> entityGeneration = new ArrayEntityGeneration<T>(this, this.compare);
                Element<N, T> element = entityGeneration.diff(original, revised, elementName, fieldType, containerType, key);
                return element;
            }

            for (EntityGeneration<?> entityGeneration : this.entityGenerations) { //TODO: class hierarchy priority
                if (entityGeneration.getType().isAssignableFrom(fieldType)) {
                    EntityGeneration<T> entityGenerationCasted = (EntityGeneration<T>) entityGeneration;
                    Element<N, T> element = entityGenerationCasted.diff(original, revised, elementName, fieldType, containerType, key);
                    return element;
                }
            }

            if (this.rootCircularKeys.containsKey(revised) && this.originalToRevisedElements.containsKey(revised)) {
                LeafElement<N, T> element = new LeafElement<N, T>(elementName, Element.Status.MODIFIED, key, (T) Name.CIRCULAR_REFERENCE);
                this.registerCircularElement(this.originalToRevisedElements.get(revised), element);
                return element;
            }

            return new LeafElement<N, T>(elementName, Element.Status.MODIFIED, key, this.clone.clone(revised, true));
        }

        if (revised == null) {
            return new LeafElement<N, T>(elementName, Element.Status.MODIFIED, key, null);
        }

        if (!getWrappedClass(fieldType).equals(revised.getClass())) {
            throw new UnsupportedOperationException("Diff objects must have the same type");
        }

        if (isStringOrPrimitiveOrWrapped(fieldType)) {
            if (original.equals(revised)) {
                return new LeafElement<N, T>(elementName, Element.Status.EQUAL, key, revised);
            }
            return new LeafElement<N, T>(elementName, Element.Status.MODIFIED, key, revised);
        }

        if (this.rootCircularKeys.containsKey(original) && this.originalToRevisedElements.containsKey(original)) {
            LeafElement<N, T> element = new LeafElement<N, T>(elementName, Element.Status.MODIFIED, key, (T) Name.CIRCULAR_REFERENCE);
            this.registerCircularElement(original, element);
            return element;
        }

        //TODO: here was if with this.visitedElements.contains(original) - do we need this anymore?

        this.visitedElements.push(original);

        if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) {
            EntityGeneration<T> entityGeneration = new ArrayEntityGeneration<T>(this, this.compare);
            Element<N, T> element = entityGeneration.diff(original, revised, elementName, fieldType, containerType, key);
            this.visitedElements.pop();
            return element;
        }

        for (EntityGeneration<?> entityGeneration : this.entityGenerations) { //TODO: class hierarchy priority
            if (entityGeneration.getType().isAssignableFrom(fieldType)) {
                EntityGeneration<T> entityGenerationCasted = (EntityGeneration<T>) entityGeneration;
                Element<N, T> element = entityGenerationCasted.diff(original, revised, elementName, fieldType, containerType, key);
                this.visitedElements.pop();
                return element;
            }
        }

        this.originalToRevisedElements.put(original, revised);
        this.revisedToOriginalElements.put(revised, original);

        List<Element<?, ?>> elements = this.processFields(fieldType, original, revised);

        Element.Status status = determineElementStatus(elements);

        this.visitedElements.pop();
        return new NodeElement<N, T>(elementName, status, key, elements);
    }

    private <T> List<Element<?, ?>> processFields(Class fieldType, T original, T revised) {
        try {
            return this.processAllClassFields(fieldType, original, revised);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private <T> List<Element<?, ?>> processAllClassFields(Class fieldType, T original, T revised) throws IllegalAccessException {
        List<Element<?, ?>> elements = new ArrayList<Element<?, ?>>();
        List<Field> fields = getAllFields(fieldType);
        for (Field field : fields) {
            if (shouldDiffField(fieldType, field.getDeclaringClass(), field)) {
                field.setAccessible(true);
                Object originalField = field.get(original);
                Object revisedField = field.get(revised);
                Class revisedFieldType = revisedField == null ? null : revisedField.getClass();
                Key fieldKey = this.generateKey(field.getName(), field.getType(), field.getDeclaringClass(), originalField);
                Element element = this.diff(this.getRevisedIfCircularReference(originalField), revisedField, field.getName(), revisedFieldType, field.getDeclaringClass(), fieldKey);
                elements.add(element);
            }
        }
        return elements;
    }

    private boolean shouldDiffField(Class container, Class declaring, Field field) {
        return !(this.typesToIgnoredFields.containsKey(container) && this.typesToIgnoredFields.get(container).containsField(declaring, field.getName()));
    }

    public static Element.Status determineElementStatus(List<Element<?, ?>> children) {
        for (Element<?, ?> element : children) {
            if (element.getStatus() != Element.Status.EQUAL) {
                return Element.Status.MODIFIED;
            }
        }
        return Element.Status.EQUAL;
    }

    public <T> T getRevisedIfCircularReference(T original) {
        if (this.originalToRevisedElements.containsKey(original)) {
            return (T) this.originalToRevisedElements.get(original);
        }
        if (this.revisedToOriginalElements.containsKey(original)) {
            return (T) this.revisedToOriginalElements.get(original);
        }
        return original;
    }

    public <T> void registerCircularElement(T original, LeafElement element) {
        this.rootCircularKeys.get(original).registerCircularElement(element);
    }

    public <N, T> Key<N, T> generateKey(N elementName, Class elementType, Class containerType, T value) {
        if (value == null || elementType == null || isStringOrPrimitiveOrWrapped(elementType) || Enum.class.isAssignableFrom(elementType)) {
            return new LeafKey<N, T>(elementName, elementType, containerType, value);
        }

        if (this.rootCircularKeys.containsKey(value)) {
            LeafKey<N, T> key = new LeafKey<N, T>(elementName, elementType, containerType, (T) Name.CIRCULAR_REFERENCE);
            this.rootCircularKeys.get(value).registerCircularKey(key);
            return key;
        }

        if (this.visitedElements.contains(value) || this.visitedKeys.contains(value)) {
            LeafKey<N, T> key = new LeafKey<N, T>(elementName, elementType, containerType, (T) Name.CIRCULAR_REFERENCE);
            List<LeafKey> leafKeys = this.visitedCircularKeys.getOrDefault(value, new ArrayList<LeafKey>());
            this.visitedCircularKeys.putIfAbsent(value, leafKeys);
            leafKeys.add(key);
            return key;
        }

        this.visitedKeys.push(value);

        if (elementType.isArray() || Collection.class.isAssignableFrom(elementType) || Map.class.isAssignableFrom(elementType)) {
            EntityGeneration<T> entityGeneration = new ArrayEntityGeneration<T>(this, this.compare);
            Key<N, T> key = entityGeneration.generateKey(elementName, elementType, containerType, value);
            this.visitedKeys.pop();
            return key;
        }

        for (EntityGeneration<?> entityGeneration : this.entityGenerations) {
            if (entityGeneration.getType().isAssignableFrom(elementType)) {   //TODO: class hierarchy priority
                EntityGeneration<T> entityGenerationCasted = (EntityGeneration<T>) entityGeneration;
                Key<N, T> key = entityGenerationCasted.generateKey(elementName, elementType, containerType, value);
                this.visitedKeys.pop();
                return key;
            }
        }

        List<Key<?, ?>> keys = new ArrayList<Key<?, ?>>();
        List<Field> fields;

        if (this.typesToEntityDefinitions.containsKey(elementType)) {
            fields = this.typesToEntityDefinitions.get(elementType).getFields();
        } else {
            fields = getAllFields(elementType);
        }

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Key<?, ?> key = this.generateKey(field.getName(), field.getType(), field.getDeclaringClass(), field.get(value));
                keys.add(key);
            } catch (IllegalAccessException e) {
                //TODO: remove this catch to separate method
            }
        }

        this.visitedKeys.pop();
        NodeKey<N, T> key = new NodeKey<N, T>(elementName, elementType, containerType, keys);
        if (this.visitedCircularKeys.containsKey(value)) {
            for (LeafKey leafKey : this.visitedCircularKeys.get(value)) {
                key.registerCircularKey(leafKey);
            }
        }
        this.rootCircularKeys.put(value, key);
        return key;
    }

    public static class Builder {
        private final Map<Class, EntityDefinition> typesToEntityDefinitions = new HashMap<Class, EntityDefinition>();
        private final Map<Class, IgnoredFields> typesToIgnoredFields = new HashMap<Class, IgnoredFields>();
        private final List<EntityGeneration<?>> entityGenerations = new ArrayList<EntityGeneration<?>>();

        public Builder registerEntity(EntityDefinition entityDefinition) {
            this.typesToEntityDefinitions.putIfAbsent(entityDefinition.getType(), entityDefinition);
            return this;
        }

        public Builder registerEntities(List<EntityDefinition> entityDefinitions) {
            for (EntityDefinition entityDefinition : entityDefinitions) {
                this.typesToEntityDefinitions.putIfAbsent(entityDefinition.getType(), entityDefinition);
            }
            return this;
        }

        public Builder ignoreFields(IgnoredFields ignoredFields) {
            this.typesToIgnoredFields.putIfAbsent(ignoredFields.getType(), ignoredFields);
            return this;
        }

        public Builder registerEntityGeneration(EntityGeneration entityGeneration) {
            this.entityGenerations.add(entityGeneration);
            return this;
        }

        public Builder registerEntityGenerations(List<EntityGeneration<?>> entityGenerations) {
            this.entityGenerations.addAll(entityGenerations);
            return this;
        }

        public Diff build() {
            return new Diff(this);
        }
    }
}