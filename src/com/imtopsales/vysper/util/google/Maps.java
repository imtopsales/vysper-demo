
package com.imtopsales.vysper.util.google;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Static utility methods pertaining to {@link Map} instances. Also see this
 * class's counterparts {@link Lists} and {@link Sets}.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 * @author Isaac Shum
 */
public final class Maps {
  private Maps() {}

  /**
   * Creates a {@code HashMap} instance.
   *
   * <p><b>Note:</b> if you don't actually need the resulting map to be mutable,
   * use {@link Collections#emptyMap} instead.
   *
   * @return a new, empty {@code HashMap}
   */
  public static <K, V> HashMap<K, V> newHashMap() {
    return new HashMap<K, V>();
  }

  /**
   * Creates an insertion-ordered {@code LinkedHashMap} instance.
   *
   * @return a new, empty {@code LinkedHashMap}
   */
  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
    return new LinkedHashMap<K, V>();
  }

  /**
   * Creates a {@code TreeMap} instance using the natural ordering of its
   * elements.
   *
   * @return a new, empty {@code TreeMap}
   */
  @SuppressWarnings({
    "rawtypes"
})  // allow ungenerified Comparable types
  public static <K extends Comparable, V> TreeMap<K, V> newTreeMap() {
    return new TreeMap<K, V>();
  }

  /**
   * Creates an {@code IdentityHashMap} instance.
   *
   * @return a new, empty {@code IdentityHashMap}
   */
  public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
    return new IdentityHashMap<K, V>();
  }
}

