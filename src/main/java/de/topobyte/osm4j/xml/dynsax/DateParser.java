// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.osm4j.xml.dynsax;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class DateParser {

    private static final String[] PATTERNS = { "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ" };

    private static final DateTimeFormatter[] PARSERS;

    static {
        PARSERS = new DateTimeFormatter[PATTERNS.length];
        for (int i = 0; i < PATTERNS.length; i++) {
            PARSERS[i] = DateTimeFormatter.ofPattern(PATTERNS[i]);
        }
    }

    private DateTimeFormatter current = PARSERS[0];

    public LocalDateTime parse(String formattedDate) {
        try {
            return LocalDateTime.parse(formattedDate, this.current);
        } catch (IllegalArgumentException e) {
            // try other parsers
        }

        for (DateTimeFormatter parser : PARSERS) {
            if (parser == this.current) {
                continue;
            }
            try {
                LocalDateTime result = LocalDateTime.parse(formattedDate, parser);
                this.current = parser;
                return result;
            } catch (IllegalArgumentException e) {
                // continue with next pattern
            }
        }

        throw new RuntimeException("Unable to parse date '" + formattedDate
                + "'");
    }
}
