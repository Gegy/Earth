// Copyright 2015 Sebastian Kuerten
//
// This file is part of adt-multicollections.
//
// adt-multicollections is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// adt-multicollections is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with adt-multicollections. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.adt.multicollections;

import java.util.Collection;

/**
 * @param <K> keys type.
 * @param <V> values type.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface MultiValMap<K, V> {

    /**
     * Associate value with key.
     *
     * @param key the key to add.
     * @param value the associated value.
     */
    void put(K key, V value);

    /**
     * Associate all value with this key.
     *
     * @param key a key.
     * @param values associated values.
     */
    void put(K key, Collection<V> values);

    /**
     * Remove this value from the key.
     *
     * @param key the key to remove the value from.
     * @param value the value to remove.
     */
    void remove(K key, V value);

    /**
     * Remove all given values from the key.
     *
     * @param key the key to remove values from.
     * @param values the value to remove.
     */
    void remove(K key, Collection<V> values);

    /**
     * Remove all values associated with this key.
     *
     * @param key the key to remove all values from.
     */
    void removeAll(K key);

    /**
     * Retrieve a collection of values associated with key.
     *
     * @param key the key to look up.
     * @return a collection of associated values.
     */
    Collection<V> get(K key);

    /**
     * Find out whether the given key has any values associated.
     *
     * @param key the key to look up.
     * @return true if this key exists.
     */
    boolean containsKey(K key);

    /**
     * Get a collection of associated keys.
     *
     * @return the collection of keys registred.
     */
    Collection<K> keys();

    /**
     * Get the number of keys in this map.
     *
     * @return the number of keys.
     */
    int size();
}
