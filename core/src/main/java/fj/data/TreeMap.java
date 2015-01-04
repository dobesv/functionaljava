package fj.data;

import static fj.Function.compose;
import static fj.Function.flip;
import static fj.P.p;
import static fj.data.IterableW.join;
import static fj.data.List.iterableList;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import fj.F;
import fj.F1Functions;
import fj.Ord;
import fj.P;
import fj.P2;
import fj.P3;

/**
 * An immutable, in-memory map, backed by a red-black tree.
 */
public final class TreeMap<K, V> implements Iterable<P2<K, V>> {
  private final Set<P2<K, Option<V>>> tree;

  private TreeMap(final Set<P2<K, Option<V>>> tree) {
    this.tree = tree;
  }

  private static <K, V> Ord<P2<K, V>> ord(final Ord<K> keyOrd) {
    return keyOrd.comap(P2.<K, V>__1());
  }

  /**
   * Constructs an empty tree map.
   *
   * @param keyOrd An order for the keys of the tree map.
   * @return an empty TreeMap with the given key order.
   */
  public static <K, V> TreeMap<K, V> empty(final Ord<K> keyOrd) {
    return new TreeMap<K, V>(Set.empty(TreeMap.<K, Option<V>>ord(keyOrd)));
  }

  /**
   * Returns a potential value that the given key maps to.
   *
   * @param k The key to look up in the tree map.
   * @return A potential value for the given key.
   */
  public Option<V> get(final K k) {
    final Option<P2<K, Option<V>>> x = this.tree.split(P.p(k, Option.<V>none()))._2();
    return x.bind(P2.<K, Option<V>>__2());
  }

  /**
   * Inserts the given key and value association into the tree map.
   * If the given key is already mapped to a value, the old value is replaced with the given one.
   *
   * @param k The key to insert.
   * @param v The value to insert.
   * @return A new tree map with the given value mapped to the given key.
   */
  public TreeMap<K, V> set(final K k, final V v) {
      return new TreeMap<K, V>(tree.insert(P.p(k, Option.some(v))));
  }

  /**
   * Deletes the entry in the tree map that corresponds to the given key.
   *
   * @param k The key to delete from this tree map.
   * @return A new tree map with the entry corresponding to the given key removed.
   */
  public TreeMap<K, V> delete(final K k) {
    return new TreeMap<K, V>(this.tree.delete(P.p(k, Option.<V>none())));
  }

  /**
   * Returns the number of entries in this tree map.
   *
   * @return The number of entries in this tree map.
   */
  public int size() {
    return this.tree.size();
  }

  /**
   * Determines if this tree map has any entries.
   *
   * @return <code>true</code> if this tree map has no entries, <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return this.tree.isEmpty();
  }

  /**
   * Returns all values in this tree map.
   *
   * @return All values in this tree map.
   */
  public List<V> values() {
    return iterableList(join(this.tree.toList().map(compose(IterableW.<V, Option<V>>wrap(), P2.<K, Option<V>>__2()))));
  }

  /**
   * Returns all keys in this tree map.
   *
   * @return All keys in this tree map.
   */
  public List<K> keys() {
    return this.tree.toList().map(P2.<K, Option<V>>__1());
  }

  /**
   * Determines if the given key value exists in this tree map.
   *
   * @param k The key value to look for in this tree map.
   * @return <code>true</code> if this tree map contains the given key, <code>false</code> otherwise.
   */
  public boolean contains(final K k) {
    return this.tree.member(P.p(k, Option.<V>none()));
  }

  /**
   * Returns an iterator for this map's key-value pairs.
   * This method exists to permit the use in a <code>for</code>-each loop.
   *
   * @return A iterator for this map's key-value pairs.
   */
  @Override
  public Iterator<P2<K, V>> iterator() {
    return join(this.tree.toStream().map(P2.<K, Option<V>, IterableW<V>>map2_(IterableW.<V, Option<V>>wrap())
        ).map(P2.tuple(compose(IterableW.<V, P2<K, V>>map(), P.<K, V>p2())))).iterator();
  }

  /**
   * A mutable map projection of this tree map.
   *
   * @return A new mutable map isomorphic to this tree map.
   */
  public Map<K, V> toMutableMap() {
    final Map<K, V> m = new java.util.TreeMap<K, V>();
    for (final P2<K, V> e : this) {
      m.put(e._1(), e._2());
    }
    return m;
  }

  /**
   * An immutable projection of the given mutable map.
   *
   * @param ord An order for the map's keys.
   * @param m   A mutable map to project to an immutable one.
   * @return A new immutable tree map isomorphic to the given mutable map.
   */
  public static <K, V> TreeMap<K, V> fromMutableMap(final Ord<K> ord, final Map<K, V> m) {
    TreeMap<K, V> t = empty(ord);
    for (final Map.Entry<K, V> e : m.entrySet()) {
      t = t.set(e.getKey(), e.getValue());
    }
    return t;
  }

  /**
   * Returns a first-class version of the get method for this TreeMap.
   *
   * @return a functional representation of this TreeMap.
   */
  public F<K, Option<V>> get() {
    return new F<K, Option<V>>() {
      @Override
      public Option<V> f(final K k) {
        return get(k);
      }
    };
  }

  /**
   * Modifies the value for the given key, if present, by applying the given function to it.
   *
   * @param k The key for the value to modify.
   * @param f A function with which to modify the value.
   * @return A new tree map with the value for the given key transformed by the given function,
   *         paired with True if the map was modified, otherwise False.
   */
  public P2<Boolean, TreeMap<K, V>> update(final K k, final F<V, V> f) {
    final P2<Boolean, Set<P2<K, Option<V>>>> up =
        this.tree.update(p(k, Option.<V>none()), P2.<K, Option<V>, Option<V>>map2_(Option.<V, V>map().f(f)));
    return P.p(up._1(), new TreeMap<K, V>(up._2()));
  }

  /**
   * Modifies the value for the given key, if present, by applying the given function to it, or
   * inserts the given value if the key is not present.
   *
   * @param k The key for the value to modify.
   * @param f A function with which to modify the value.
   * @param v A value to associate with the given key if the key is not already present.
   * @return A new tree map with the value for the given key transformed by the given function.
   */
  public TreeMap<K, V> update(final K k, final F<V, V> f, final V v) {
    final P2<Boolean, TreeMap<K, V>> up = update(k, f);
    return up._1() ? up._2() : set(k, v);
  }

  /**
   * Splits this TreeMap at the given key. Returns a triple of:
   * <ul>
   * <li>A set containing all the values of this map associated with keys less than the given key.</li>
   * <li>An option of a value mapped to the given key, if it exists in this map, otherwise None.
   * <li>A set containing all the values of this map associated with keys greater than the given key.</li>
   * </ul>
   *
   * @param k A key at which to split this map.
   * @return Two sets and an optional value, where all elements in the first set are mapped to keys less than the given
   *         key in this map, all the elements in the second set are mapped to keys greater than the given key,
   *         and the optional value is the value associated with the given key if present, otherwise None.
   */
  public P3<Set<V>, Option<V>, Set<V>> split(final K k) {
    final F<Set<P2<K, Option<V>>>, Set<V>> getSome = F1Functions.mapSet(F1Functions.o(Option.<V>fromSome(), P2.<K, Option<V>>__2())
        , tree.ord().comap(F1Functions.o(P.<K, Option<V>>p2().f(k), Option.<V>some_())));
    return tree.split(p(k, Option.<V>none())).map1(getSome).map3(getSome)
        .map2(F1Functions.o(Option.<V>join(), F1Functions.mapOption(P2.<K, Option<V>>__2())));
  }

  /**
   * Maps the given function across the values of this TreeMap.
   *
   * @param f A function to apply to the values of this TreeMap.
   * @return A new TreeMap with the values transformed by the given function.
   */
  @SuppressWarnings({"unchecked"})
  public <W> TreeMap<K, W> map(final F<V, W> f) {
    final F<P2<K, Option<V>>, P2<K, Option<W>>> g = P2.map2_(F1Functions.mapOption(f));
    final F<K, P2<K, Option<V>>> coord = flip(P.<K, Option<V>>p2()).f(Option.<V>none());
    final Ord<K> o = this.tree.ord().comap(coord);
    return new TreeMap<K, W>(this.tree.map(TreeMap.<K, Option<W>>ord(o), g));
  }

  /**
   * Copy all pairs from x into this map, replacing any existing keys in this map.
   *
   * @param TreeMap to copy from
   * @return A new treemap with all the pairs from x and non-conflicting pairs from this map.
   */
  public TreeMap<K, V> setAll(TreeMap<K, V> x) {
    TreeMap<K, V> result = this;
    for(P2<K,V> p : x) {
      result = result.set(p._1(), p._2());
    }
    return result;
  }

}
