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

public class Element {

    String identifier;
    boolean hasText;

    public Element(String identifier, boolean hasText) {
        this.identifier = identifier;
        this.hasText = hasText;
    }

    List<Child> children = new ArrayList<>();

    public void addChild(Child child) {
        this.children.add(child);
    }

    Map<String, Child> lookup = null;

    public void init() {
        this.lookup = new HashMap<>();
        for (Child child : this.children) {
            this.lookup.put(child.element.identifier, child);
        }
    }

    List<String> attributes = new ArrayList<>();

    public void addAttribute(String attribute) {
        this.attributes.add(attribute);
    }
}
