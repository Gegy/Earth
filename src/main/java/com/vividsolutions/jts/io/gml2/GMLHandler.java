/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.io.gml2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.gml2.GeometryStrategies.ParseStrategy;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * A SAX {@link DefaultHandler} which builds {@link Geometry}s
 * from GML2-formatted geometries.
 * An XML parser can delegate SAX events to this handler
 * to parse and building Geometrys.
 * <p>
 * This handler currently ignores both namespaces and prefixes.
 * <p>
 * Hints:
 * <ul>
 * <li>If your parent handler is a DefaultHandler register the parent handler to receive the errors and locator calls.
 * <li>Use {@link GeometryStrategies#findStrategy(String, String)} to help check for applicability
 * </ul>
 *
 * @author David Zwiers, Vivid Solutions.
 * @see DefaultHandler
 */
public class GMLHandler extends DefaultHandler {

    /**
     * This class is intended to log the SAX acitivity within a given element until its termination.
     * At this time, a new object of value is created and passed to the parent.
     * An object of value is typically either java.lang.* or a JTS Geometry
     * This class is not intended for use outside this distribution,
     * and may change in subsequent versions.
     *
     * @author David Zwiers, Vivid Solutions.
     */
    static class Handler {
        protected Attributes attrs = null;

        protected ParseStrategy strategy;

        /**
         * @param strategy
         * @param attributes Nullable
         */
        public Handler(ParseStrategy strategy, Attributes attributes) {
            if (attributes != null) {
                this.attrs = new AttributesImpl(attributes);
            }
            this.strategy = strategy;
        }

        protected StringBuffer text = null;

        /**
         * Caches text for the future
         *
         * @param str
         */
        public void addText(String str) {
            if (this.text == null) {
                this.text = new StringBuffer();
            }
            this.text.append(str);
        }

        protected List children = null;

        /**
         * Store param for the future
         *
         * @param obj
         */
        public void keep(Object obj) {
            if (this.children == null) {
                this.children = new LinkedList();
            }
            this.children.add(obj);
        }

        /**
         * @param gf GeometryFactory
         * @return Parsed Object
         * @throws SAXException
         */
        public Object create(GeometryFactory gf) throws SAXException {
            return this.strategy.parse(this, gf);
        }
    }

    private Stack stack = new Stack();

    private ErrorHandler delegate = null;

    private GeometryFactory gf = null;

    /**
     * Creates a new handler.
     * Allows the user to specify a delegate object for error / warning messages.
     * If the delegate also implements ContentHandler then the document Locator will be passed on.
     *
     * @param gf Geometry Factory
     * @param delegate Nullable
     * @see ErrorHandler
     * @see ContentHandler
     * @see ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     * @see org.xml.sax.Locator
     */
    public GMLHandler(GeometryFactory gf, ErrorHandler delegate) {
        this.delegate = delegate;
        this.gf = gf;
        this.stack.push(new Handler(null, null));
    }

    /**
     * Tests whether this handler has completed parsing
     * a geometry.
     * If this is the case, {@link #getGeometry()} can be called
     * to get the value of the parsed geometry.
     *
     * @return if the parsing of the geometry is complete
     */
    public boolean isGeometryComplete() {
        if (this.stack.size() > 1) {
            return false;
        }
        // top level node on stack needs to have at least one child
        Handler h = (Handler) this.stack.peek();
        return h.children.size() >= 1;
    }

    /**
     * Gets the geometry parsed by this handler.
     * This method should only be called AFTER the parser has completed execution
     *
     * @return the parsed Geometry, or a GeometryCollection if more than one geometry was parsed
     * @throws IllegalStateException if called before the parse is complete
     */
    public Geometry getGeometry() {
        if (this.stack.size() == 1) {
            Handler h = (Handler) this.stack.peek();
            if (h.children.size() == 1) {
                return (Geometry) h.children.get(0);
            }
            return this.gf.createGeometryCollection(
                    (Geometry[]) h.children.toArray(new Geometry[this.stack.size()]));
        }
        throw new IllegalStateException(
                "Parse did not complete as expected, there are " + this.stack.size()
                        + " elements on the Stack");
    }

    //////////////////////////////////////////////
    // Parsing Methods

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!this.stack.isEmpty()) {
            ((Handler) this.stack.peek()).addText(new String(ch, start, length));
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (!this.stack.isEmpty()) {
            ((Handler) this.stack.peek()).addText(" ");
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        Handler thisAction = (Handler) this.stack.pop();
        ((Handler) this.stack.peek()).keep(thisAction.create(this.gf));
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // create a handler
        ParseStrategy ps = GeometryStrategies.findStrategy(uri, localName);
        if (ps == null) {
            String qn = qName.substring(qName.indexOf(':') + 1, qName.length());
            ps = GeometryStrategies.findStrategy(null, qn);
        }
        Handler h = new Handler(ps, attributes);
        // and add it to the stack
        this.stack.push(h);
    }

    //////////////////////////////////////////////
    // Logging Methods

    /**
     * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        if (this.delegate != null && this.delegate instanceof ContentHandler) {
            ((ContentHandler) this.delegate).setDocumentLocator(locator);
        }
    }

    private Locator locator = null;

    protected Locator getDocumentLocator() {
        return this.locator;
    }

    //////////////////////////////////////////////
    // ERROR Methods

    /**
     * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        if (this.delegate != null) {
            this.delegate.fatalError(e);
        } else {
            super.fatalError(e);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        if (this.delegate != null) {
            this.delegate.error(e);
        } else {
            super.error(e);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {
        if (this.delegate != null) {
            this.delegate.warning(e);
        } else {
            super.warning(e);
        }
    }
}
