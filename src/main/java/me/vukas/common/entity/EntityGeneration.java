package me.vukas.common.entity;

import me.vukas.common.entity.element.Element;
import me.vukas.common.entity.key.Key;
import me.vukas.common.entity.operation.Diff;

public abstract class EntityGeneration<T> extends EntityComparison<T> {
    private Diff diff;

    public Diff getDiff(){
        return this.diff;
    }

    public void setDiff(Diff diff){
        assert this.diff == null : "Diff field can be set only once";
        this.diff = diff;
    }

    public abstract <N> Element diff(T original, T revised, N elementName, Class fieldType, Class containerType, Key<N, T> key);
    public abstract <N> Key generateKey(N elementName, Class elementType, Class containerType, T value);
}