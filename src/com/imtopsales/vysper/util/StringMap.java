package com.imtopsales.vysper.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class StringMap extends HashMap<String, String> {

    private static final long serialVersionUID = 311710247188569247L;

    public StringMap() {
    }

    public StringMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringMap(Map<? extends String, ? extends String> m) {
        super(m);
    }

    public StringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    
}
