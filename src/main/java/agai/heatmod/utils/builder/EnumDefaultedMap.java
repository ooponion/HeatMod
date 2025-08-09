package agai.heatmod.utils.builder;

import java.util.EnumMap;

public class EnumDefaultedMap<K extends Enum<K>,T> {
    private T defaultSlot;
    private EnumMap<K,T> map;
    public EnumDefaultedMap(Class<K> enumClass) {
        map=new EnumMap<>(enumClass);
    }

    public void put(K slot,T data) {
        if(slot==null)
            defaultSlot=data;
        else
            map.put(slot, data);
    };
    public T get(K slot) {
        if(slot==null)
            return defaultSlot;
        return map.getOrDefault(slot, defaultSlot);
    }
}