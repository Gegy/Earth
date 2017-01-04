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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A data-structure to collect Tags. A tag consists of key + value. The
 * structure counts the number of times a pair has been added to the
 * MultiValMap. Thus to remove a tag, you have to remove it as often as it has
 * been added to the MultiValMap.
 *
 * @param <K> Type of the keys.
 * @param <L> Type of the values.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class CountingMultiValMap<K, L> {

    Map<K, Map<L, Integer>> storage = new HashMap<>();

    /**
     * Add the tag (key1, key2) to the MultiValMap.
     *
     * @param key1 the first key
     * @param key2 the seconds key
     */
    public void add(K key1, L key2) {
        Map<L, Integer> keyStore;
        if (this.storage.containsKey(key1)) {
            keyStore = this.storage.get(key1);
        } else {
            keyStore = new HashMap<>();
            this.storage.put(key1, keyStore);
        }
        int count = 1;
        if (keyStore.containsKey(key2)) {
            count = keyStore.get(key2) + 1;
        }
        keyStore.put(key2, count);
    }

    /**
     * @param key the key for which to determine all associated values.
     * @return the set of all v associated with key <code>key</code>: { v |
     * (key,v) is present in the MultiValMap }
     */
    public Set<L> getForKey(K key) {
        return this.storage.get(key).keySet();
    }

    /**
     * @param key the key for which to determine the association map.
     * @return the association map for the given key.
     */
    public Map<L, Integer> get(K key) {
        return this.storage.get(key);
    }

    /**
     * Remove the tag (key1, key2) from the MultiValMap. Decrements the count
     * for this tag by one. If count reaches zero, the tag will be removed.
     *
     * @param key1 the first key.
     * @param key2 the seconds key.
     */
    public void remove(K key1, L key2) {
        if (!this.storage.containsKey(key1)) {
            System.out.println("unable to remove 1");
            return;
        }
        Map<L, Integer> keyStore = this.storage.get(key1);
        if (!keyStore.containsKey(key2)) {
            System.out.println("unable to remove 2");
            return;
        }
        int count = keyStore.get(key2) - 1;
        if (count == 0) {
            keyStore.remove(key2);
        } else {
            keyStore.put(key2, count);
        }
    }

    /**
     * @return a collection of all values that are in this map.
     */
    public Collection<L> values() {
        HashSet<L> values = new HashSet<>();
        for (Entry<K, Map<L, Integer>> entry : this.storage.entrySet()) {
            Map<L, Integer> map = entry.getValue();
            Set<L> newValues = map.keySet();
            values.addAll(newValues);
        }
        return values;
    }
}
