package com.imtopsales.vysper.util;

import java.util.HashMap;
import java.util.Map;

/**
 * map工具类，增加一个方法，方便实现链式表达式
 */
public class ParamMap extends HashMap<String, String> {

    private static final long serialVersionUID = 7714192659412413071L;

    public ParamMap() {}

    public ParamMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ParamMap(Map<? extends String, ? extends String> m) {
        super(m);
    }

    public ParamMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    public ParamMap add(String key, String value) {
        this.put(key, value);
        return this;
    }
}
