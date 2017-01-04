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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A straightforward implementation of MultiSet using a HashMap.
 *
 * @param <T> the type of elements
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class HashMultiSet<T> implements MultiSet<T> {

    Map<T, Integer> map = new HashMap<>();

    public HashMultiSet() {
        // empty constructor
    }

    public HashMultiSet(MultiSet<T> prototype) {
        for (T object : prototype.keySet()) {
            this.map.put(object, prototype.occurences(object));
        }
    }

    @Override
    public boolean contains(T key) {
        return this.occurences(key) > 0;
    }

    @Override
    public int occurences(T key) {
        if (!this.map.containsKey(key)) {
            return 0;
        }
        return this.map.get(key);
    }

    @Override
    public void add(T key) {
        if (!this.map.containsKey(key)) {
            this.map.put(key, 1);
        } else {
            this.map.put(key, this.map.get(key) + 1);
        }
    }

    @Override
    public void add(T key, int howOften) {
        if (!this.map.containsKey(key)) {
            this.map.put(key, howOften);
        } else {
            this.map.put(key, this.map.get(key) + howOften);
        }
    }

    @Override
    public void addAll(Collection<T> keys) {
        for (T key : keys) {
            this.add(key);
        }
    }

    @Override
    public void addAll(Collection<T> keys, int howOften) {
        for (T key : keys) {
            this.add(key, howOften);
        }
    }

    @Override
    public void remove(T key) {
        if (!this.map.containsKey(key)) {
            return;
        }
        int count = this.map.get(key) - 1;
        if (count == 0) {
            this.map.remove(key);
        } else {
            this.map.put(key, count);
        }
    }

    @Override
    public void removeAll(Collection<? extends T> c) {
        for (T key : c) {
            this.remove(key);
        }
    }

    @Override
    public void removeOccurences(T key) {
        if (!this.map.containsKey(key)) {
            return;
        }
        this.map.remove(key);
    }

    @Override
    public void removeAllOccurences(Collection<? extends T> c) {
        for (T key : c) {
            this.removeOccurences(key);
        }
    }

    @Override
    public void removeN(T key, int n) {
        if (!this.map.containsKey(key)) {
            return;
        }
        int count = this.map.get(key) - n;
        if (count <= 0) {
            this.map.remove(key);
        } else {
            this.map.put(key, count);
        }
    }

    @Override
    public void removeAllN(Collection<? extends T> c, int n) {
        for (T key : c) {
            this.removeN(key, n);
        }
    }

    @Override
    public Set<T> keySet() {
        return this.map.keySet();
    }

    @Override
    public Iterator<T> iterator() {
        return new HashMultiSetIterator<>(this);
    }

    private class HashMultiSetIterator<K> implements Iterator<K> {

        private HashMultiSet<K> hms;
        private int leftForThis = 0;
        private Set<K> keys;
        private K current;

        public HashMultiSetIterator(HashMultiSet<K> hms) {
            this.hms = hms;
            this.keys = new HashSet<>();
            for (K key : hms.keySet()) {
                this.keys.add(key);
            }
        }

        @Override
        public boolean hasNext() {
            if (this.leftForThis == 0) {
                return !this.keys.isEmpty();
            }
            return true;
        }

        @Override
        public K next() {
            if (this.leftForThis == 0) {
                this.current = this.keys.iterator().next();
                this.leftForThis = this.hms.occurences(this.current);
                this.keys.remove(this.current);
            }
            this.leftForThis -= 1;
            return this.current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "HashMultiSet iterators don't provide removal method");
        }
    }
}
