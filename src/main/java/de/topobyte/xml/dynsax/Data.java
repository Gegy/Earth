// Copyright 2015 Sebastian Kuerten
//
// This file is part of dynsax.
//
// dynsax is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// dynsax is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with dynsax. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.xml.dynsax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {

    private Element element;

    public Data(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return this.element;
    }

    private Map<String, String> attributes = new HashMap<>();
    private Map<String, Data> singles = new HashMap<>();
    private Map<String, List<Data>> lists = new HashMap<>();
    StringBuilder buffer = new StringBuilder();

    void setSingle(String name, Data data) {
        this.singles.put(name, data);
    }

    void addToList(String name, Data data) {
        List<Data> list = this.lists.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(data);
    }

    void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Data getSingle(String name) {
        return this.singles.get(name);
    }

    public List<Data> getList(String name) {
        return this.lists.get(name);
    }

    public String getText() {
        return this.buffer.toString();
    }
}
