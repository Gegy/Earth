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
package com.vividsolutions.jts.io;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.io.IOException;

/**
 * Reads a {@link Geometry}from a byte stream in Well-Known Binary format.
 * Supports use of an {@link InStream}, which allows easy use
 * with arbitrary byte stream sources.
 * <p>
 * This class reads the format describe in {@link WKBWriter}.
 * It also partially handles
 * the <b>Extended WKB</b> format used by PostGIS,
 * by parsing and storing SRID values.
 * The reader repairs structurally-invalid input
 * (specifically, LineStrings and LinearRings which contain
 * too few points have vertices added,
 * and non-closed rings are closed).
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * @see WKBWriter for a formal format specification
 */
public class WKBReader {
    /**
     * Converts a hexadecimal string to a byte array.
     * The hexadecimal digit symbols are case-insensitive.
     *
     * @param hex a string containing hex digits
     * @return an array of bytes with the value of the hex string
     */
    public static byte[] hexToBytes(String hex) {
        int byteLen = hex.length() / 2;
        byte[] bytes = new byte[byteLen];

        for (int i = 0; i < hex.length() / 2; i++) {
            int i2 = 2 * i;
            if (i2 + 1 > hex.length()) {
                throw new IllegalArgumentException("Hex string has odd length");
            }

            int nib1 = hexToInt(hex.charAt(i2));
            int nib0 = hexToInt(hex.charAt(i2 + 1));
            byte b = (byte) ((nib1 << 4) + (byte) nib0);
            bytes[i] = b;
        }
        return bytes;
    }

    private static int hexToInt(char hex) {
        int nib = Character.digit(hex, 16);
        if (nib < 0) {
            throw new IllegalArgumentException("Invalid hex digit: '" + hex + "'");
        }
        return nib;
    }

    private static final String INVALID_GEOM_TYPE_MSG
            = "Invalid geometry type encountered in ";

    private GeometryFactory factory;
    private CoordinateSequenceFactory csFactory;
    private PrecisionModel precisionModel;
    // default dimension - will be set on read
    private int inputDimension = 2;
    private boolean hasSRID = false;
    private int SRID = 0;
    /**
     * true if structurally invalid input should be reported rather than repaired.
     * At some point this could be made client-controllable.
     */
    private boolean isStrict = false;
    private ByteOrderDataInStream dis = new ByteOrderDataInStream();
    private double[] ordValues;

    public WKBReader() {
        this(new GeometryFactory());
    }

    public WKBReader(GeometryFactory geometryFactory) {
        this.factory = geometryFactory;
        this.precisionModel = this.factory.getPrecisionModel();
        this.csFactory = this.factory.getCoordinateSequenceFactory();
    }

    /**
     * Reads a single {@link Geometry} in WKB format from a byte array.
     *
     * @param bytes the byte array to read from
     * @return the geometry read
     * @throws ParseException if the WKB is ill-formed
     */
    public Geometry read(byte[] bytes) throws ParseException {
        // possibly reuse the ByteArrayInStream?
        // don't throw IOExceptions, since we are not doing any I/O
        try {
            return this.read(new ByteArrayInStream(bytes));
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage());
        }
    }

    /**
     * Reads a {@link Geometry} in binary WKB format from an {@link InStream}.
     *
     * @param is the stream to read from
     * @return the Geometry read
     * @throws IOException if the underlying stream creates an error
     * @throws ParseException if the WKB is ill-formed
     */
    public Geometry read(InStream is)
            throws IOException, ParseException {
        this.dis.setInStream(is);
        Geometry g = this.readGeometry();
        return g;
    }

    private Geometry readGeometry()
            throws IOException, ParseException {
        // determine byte order
        byte byteOrderWKB = this.dis.readByte();
        // always set byte order, since it may change from geometry to geometry
        int byteOrder = byteOrderWKB == WKBConstants.wkbNDR ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN;
        this.dis.setOrder(byteOrder);

        int typeInt = this.dis.readInt();
        int geometryType = typeInt & 0xff;
        // determine if Z values are present
        boolean hasZ = (typeInt & 0x80000000) != 0;
        this.inputDimension = hasZ ? 3 : 2;
        // determine if SRIDs are present
        this.hasSRID = (typeInt & 0x20000000) != 0;

        int SRID = 0;
        if (this.hasSRID) {
            SRID = this.dis.readInt();
        }

        // only allocate ordValues buffer if necessary
        if (this.ordValues == null || this.ordValues.length < this.inputDimension) {
            this.ordValues = new double[this.inputDimension];
        }

        Geometry geom = null;
        switch (geometryType) {
            case WKBConstants.wkbPoint:
                geom = this.readPoint();
                break;
            case WKBConstants.wkbLineString:
                geom = this.readLineString();
                break;
            case WKBConstants.wkbPolygon:
                geom = this.readPolygon();
                break;
            case WKBConstants.wkbMultiPoint:
                geom = this.readMultiPoint();
                break;
            case WKBConstants.wkbMultiLineString:
                geom = this.readMultiLineString();
                break;
            case WKBConstants.wkbMultiPolygon:
                geom = this.readMultiPolygon();
                break;
            case WKBConstants.wkbGeometryCollection:
                geom = this.readGeometryCollection();
                break;
            default:
                throw new ParseException("Unknown WKB type " + geometryType);
        }
        this.setSRID(geom, SRID);
        return geom;
    }

    /**
     * Sets the SRID, if it was specified in the WKB
     *
     * @param g the geometry to update
     * @return the geometry with an updated SRID value, if required
     */
    private Geometry setSRID(Geometry g, int SRID) {
        if (SRID != 0) {
            g.setSRID(SRID);
        }
        return g;
    }

    private Point readPoint() throws IOException {
        CoordinateSequence pts = this.readCoordinateSequence(1);
        return this.factory.createPoint(pts);
    }

    private LineString readLineString() throws IOException {
        int size = this.dis.readInt();
        CoordinateSequence pts = this.readCoordinateSequenceLineString(size);
        return this.factory.createLineString(pts);
    }

    private LinearRing readLinearRing() throws IOException {
        int size = this.dis.readInt();
        CoordinateSequence pts = this.readCoordinateSequenceRing(size);
        return this.factory.createLinearRing(pts);
    }

    private Polygon readPolygon() throws IOException {
        int numRings = this.dis.readInt();
        LinearRing[] holes = null;
        if (numRings > 1) {
            holes = new LinearRing[numRings - 1];
        }

        LinearRing shell = this.readLinearRing();
        for (int i = 0; i < numRings - 1; i++) {
            holes[i] = this.readLinearRing();
        }
        return this.factory.createPolygon(shell, holes);
    }

    private MultiPoint readMultiPoint() throws IOException, ParseException {
        int numGeom = this.dis.readInt();
        Point[] geoms = new Point[numGeom];
        for (int i = 0; i < numGeom; i++) {
            Geometry g = this.readGeometry();
            if (!(g instanceof Point)) {
                throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPoint");
            }
            geoms[i] = (Point) g;
        }
        return this.factory.createMultiPoint(geoms);
    }

    private MultiLineString readMultiLineString() throws IOException, ParseException {
        int numGeom = this.dis.readInt();
        LineString[] geoms = new LineString[numGeom];
        for (int i = 0; i < numGeom; i++) {
            Geometry g = this.readGeometry();
            if (!(g instanceof LineString)) {
                throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiLineString");
            }
            geoms[i] = (LineString) g;
        }
        return this.factory.createMultiLineString(geoms);
    }

    private MultiPolygon readMultiPolygon() throws IOException, ParseException {
        int numGeom = this.dis.readInt();
        Polygon[] geoms = new Polygon[numGeom];
        for (int i = 0; i < numGeom; i++) {
            Geometry g = this.readGeometry();
            if (!(g instanceof Polygon)) {
                throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPolygon");
            }
            geoms[i] = (Polygon) g;
        }
        return this.factory.createMultiPolygon(geoms);
    }

    private GeometryCollection readGeometryCollection() throws IOException, ParseException {
        int numGeom = this.dis.readInt();
        Geometry[] geoms = new Geometry[numGeom];
        for (int i = 0; i < numGeom; i++) {
            geoms[i] = this.readGeometry();
        }
        return this.factory.createGeometryCollection(geoms);
    }

    private CoordinateSequence readCoordinateSequence(int size) throws IOException {
        CoordinateSequence seq = this.csFactory.create(size, this.inputDimension);
        int targetDim = seq.getDimension();
        if (targetDim > this.inputDimension) {
            targetDim = this.inputDimension;
        }
        for (int i = 0; i < size; i++) {
            this.readCoordinate();
            for (int j = 0; j < targetDim; j++) {
                seq.setOrdinate(i, j, this.ordValues[j]);
            }
        }
        return seq;
    }

    private CoordinateSequence readCoordinateSequenceLineString(int size) throws IOException {
        CoordinateSequence seq = this.readCoordinateSequence(size);
        if (this.isStrict) {
            return seq;
        }
        if (seq.size() == 0 || seq.size() >= 2) {
            return seq;
        }
        return CoordinateSequences.extend(this.csFactory, seq, 2);
    }

    private CoordinateSequence readCoordinateSequenceRing(int size) throws IOException {
        CoordinateSequence seq = this.readCoordinateSequence(size);
        if (this.isStrict) {
            return seq;
        }
        if (CoordinateSequences.isRing(seq)) {
            return seq;
        }
        return CoordinateSequences.ensureValidRing(this.csFactory, seq);
    }

    /**
     * Reads a coordinate value with the specified dimensionality.
     * Makes the X and Y ordinates precise according to the precision model
     * in use.
     */
    private void readCoordinate() throws IOException {
        for (int i = 0; i < this.inputDimension; i++) {
            if (i <= 1) {
                this.ordValues[i] = this.precisionModel.makePrecise(this.dis.readDouble());
            } else {
                this.ordValues[i] = this.dis.readDouble();
            }
        }
    }
}