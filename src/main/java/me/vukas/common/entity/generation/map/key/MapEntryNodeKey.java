package me.vukas.common.entity.generation.map.key;

import me.vukas.common.entity.key.Key;

import java.util.Map;

public class MapEntryNodeKey<N> extends Key<N, Map.Entry> {
    private Key keyKey;
    private Key keyValue;

    public MapEntryNodeKey(N name, Class type, Class container, Key<?, ?> keyKey, Key<?, ?> keyValue) {
        super(name, type, container);
        this.keyKey = keyKey;
        this.keyValue = keyValue;
    }

    @Override
    public boolean match(Map.Entry value) {
        return keyKey.match(value.getKey()) && keyValue.match(value.getValue());
    }
}
