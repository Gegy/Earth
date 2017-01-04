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
package com.vividsolutions.jts.geom;

import java.io.Serializable;

/**
 * Defines a rectangular region of the 2D coordinate plane.
 * It is often used to represent the bounding box of a {@link Geometry},
 * e.g. the minimum and maximum x and y values of the {@link Coordinate}s.
 * <p>
 * Note that Envelopes support infinite or half-infinite regions, by using the values of
 * <code>Double.POSITIVE_INFINITY</code> and <code>Double.NEGATIVE_INFINITY</code>.
 * <p>
 * When Envelope objects are created or initialized,
 * the supplies extent values are automatically sorted into the correct order.
 *
 * @version 1.7
 */
public class Envelope
        implements Serializable {
    private static final long serialVersionUID = 5873921885273102420L;

    public int hashCode() {
        //Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
        int result = 17;
        result = 37 * result + Coordinate.hashCode(this.minx);
        result = 37 * result + Coordinate.hashCode(this.maxx);
        result = 37 * result + Coordinate.hashCode(this.miny);
        result = 37 * result + Coordinate.hashCode(this.maxy);
        return result;
    }

    /**
     * Test the point q to see whether it intersects the Envelope defined by p1-p2
     *
     * @param p1 one extremal point of the envelope
     * @param p2 another extremal point of the envelope
     * @param q the point to test for intersection
     * @return <code>true</code> if q intersects the envelope p1-p2
     */
    public static boolean intersects(Coordinate p1, Coordinate p2, Coordinate q) {
        //OptimizeIt shows that Math#min and Math#max here are a bottleneck.
        //Replace with direct comparisons. [Jon Aquino]
        return ((q.x >= (p1.x < p2.x ? p1.x : p2.x)) && (q.x <= (p1.x > p2.x ? p1.x : p2.x))) &&
                ((q.y >= (p1.y < p2.y ? p1.y : p2.y)) && (q.y <= (p1.y > p2.y ? p1.y : p2.y)));
    }

    /**
     * Tests whether the envelope defined by p1-p2
     * and the envelope defined by q1-q2
     * intersect.
     *
     * @param p1 one extremal point of the envelope P
     * @param p2 another extremal point of the envelope P
     * @param q1 one extremal point of the envelope Q
     * @param q2 another extremal point of the envelope Q
     * @return <code>true</code> if Q intersects P
     */
    public static boolean intersects(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
        double minq = Math.min(q1.x, q2.x);
        double maxq = Math.max(q1.x, q2.x);
        double minp = Math.min(p1.x, p2.x);
        double maxp = Math.max(p1.x, p2.x);

        if (minp > maxq) {
            return false;
        }
        if (maxp < minq) {
            return false;
        }

        minq = Math.min(q1.y, q2.y);
        maxq = Math.max(q1.y, q2.y);
        minp = Math.min(p1.y, p2.y);
        maxp = Math.max(p1.y, p2.y);

        if (minp > maxq) {
            return false;
        }
        return !(maxp < minq);
    }

    /**
     * the minimum x-coordinate
     */
    private double minx;

    /**
     * the maximum x-coordinate
     */
    private double maxx;

    /**
     * the minimum y-coordinate
     */
    private double miny;

    /**
     * the maximum y-coordinate
     */
    private double maxy;

    /**
     * Creates a null <code>Envelope</code>.
     */
    public Envelope() {
        this.init();
    }

    /**
     * Creates an <code>Envelope</code> for a region defined by maximum and minimum values.
     *
     * @param x1 the first x-value
     * @param x2 the second x-value
     * @param y1 the first y-value
     * @param y2 the second y-value
     */
    public Envelope(double x1, double x2, double y1, double y2) {
        this.init(x1, x2, y1, y2);
    }

    /**
     * Creates an <code>Envelope</code> for a region defined by two Coordinates.
     *
     * @param p1 the first Coordinate
     * @param p2 the second Coordinate
     */
    public Envelope(Coordinate p1, Coordinate p2) {
        this.init(p1.x, p2.x, p1.y, p2.y);
    }

    /**
     * Creates an <code>Envelope</code> for a region defined by a single Coordinate.
     *
     * @param p the Coordinate
     */
    public Envelope(Coordinate p) {
        this.init(p.x, p.x, p.y, p.y);
    }

    /**
     * Create an <code>Envelope</code> from an existing Envelope.
     *
     * @param env the Envelope to initialize from
     */
    public Envelope(Envelope env) {
        this.init(env);
    }

    /**
     * Initialize to a null <code>Envelope</code>.
     */
    public void init() {
        this.setToNull();
    }

    /**
     * Initialize an <code>Envelope</code> for a region defined by maximum and minimum values.
     *
     * @param x1 the first x-value
     * @param x2 the second x-value
     * @param y1 the first y-value
     * @param y2 the second y-value
     */
    public void init(double x1, double x2, double y1, double y2) {
        if (x1 < x2) {
            this.minx = x1;
            this.maxx = x2;
        } else {
            this.minx = x2;
            this.maxx = x1;
        }
        if (y1 < y2) {
            this.miny = y1;
            this.maxy = y2;
        } else {
            this.miny = y2;
            this.maxy = y1;
        }
    }

    /**
     * Initialize an <code>Envelope</code> to a region defined by two Coordinates.
     *
     * @param p1 the first Coordinate
     * @param p2 the second Coordinate
     */
    public void init(Coordinate p1, Coordinate p2) {
        this.init(p1.x, p2.x, p1.y, p2.y);
    }

    /**
     * Initialize an <code>Envelope</code> to a region defined by a single Coordinate.
     *
     * @param p the coordinate
     */
    public void init(Coordinate p) {
        this.init(p.x, p.x, p.y, p.y);
    }

    /**
     * Initialize an <code>Envelope</code> from an existing Envelope.
     *
     * @param env the Envelope to initialize from
     */
    public void init(Envelope env) {
        this.minx = env.minx;
        this.maxx = env.maxx;
        this.miny = env.miny;
        this.maxy = env.maxy;
    }

    /**
     * Makes this <code>Envelope</code> a "null" envelope, that is, the envelope
     * of the empty geometry.
     */
    public void setToNull() {
        this.minx = 0;
        this.maxx = -1;
        this.miny = 0;
        this.maxy = -1;
    }

    /**
     * Returns <code>true</code> if this <code>Envelope</code> is a "null"
     * envelope.
     *
     * @return <code>true</code> if this <code>Envelope</code> is uninitialized
     * or is the envelope of the empty geometry.
     */
    public boolean isNull() {
        return this.maxx < this.minx;
    }

    /**
     * Returns the difference between the maximum and minimum x values.
     *
     * @return max x - min x, or 0 if this is a null <code>Envelope</code>
     */
    public double getWidth() {
        if (this.isNull()) {
            return 0;
        }
        return this.maxx - this.minx;
    }

    /**
     * Returns the difference between the maximum and minimum y values.
     *
     * @return max y - min y, or 0 if this is a null <code>Envelope</code>
     */
    public double getHeight() {
        if (this.isNull()) {
            return 0;
        }
        return this.maxy - this.miny;
    }

    /**
     * Returns the <code>Envelope</code>s minimum x-value. min x > max x
     * indicates that this is a null <code>Envelope</code>.
     *
     * @return the minimum x-coordinate
     */
    public double getMinX() {
        return this.minx;
    }

    /**
     * Returns the <code>Envelope</code>s maximum x-value. min x > max x
     * indicates that this is a null <code>Envelope</code>.
     *
     * @return the maximum x-coordinate
     */
    public double getMaxX() {
        return this.maxx;
    }

    /**
     * Returns the <code>Envelope</code>s minimum y-value. min y > max y
     * indicates that this is a null <code>Envelope</code>.
     *
     * @return the minimum y-coordinate
     */
    public double getMinY() {
        return this.miny;
    }

    /**
     * Returns the <code>Envelope</code>s maximum y-value. min y > max y
     * indicates that this is a null <code>Envelope</code>.
     *
     * @return the maximum y-coordinate
     */
    public double getMaxY() {
        return this.maxy;
    }

    /**
     * Gets the area of this envelope.
     *
     * @return 0.0 if the envelope is null
     */
    public double getArea() {
        return this.getWidth() * this.getHeight();
    }

    /**
     * Gets the minimum extent of this envelope across both dimensions.
     *
     * @return the minimum extent of this envelope
     */
    public double minExtent() {
        if (this.isNull()) {
            return 0.0;
        }
        double w = this.getWidth();
        double h = this.getHeight();
        if (w < h) {
            return w;
        }
        return h;
    }

    /**
     * Gets the maximum extent of this envelope across both dimensions.
     *
     * @return the maximum extent of this envelope
     */
    public double maxExtent() {
        if (this.isNull()) {
            return 0.0;
        }
        double w = this.getWidth();
        double h = this.getHeight();
        if (w > h) {
            return w;
        }
        return h;
    }

    /**
     * Enlarges this <code>Envelope</code> so that it contains
     * the given {@link Coordinate}.
     * Has no effect if the point is already on or within the envelope.
     *
     * @param p the Coordinate to expand to include
     */
    public void expandToInclude(Coordinate p) {
        this.expandToInclude(p.x, p.y);
    }

    /**
     * Expands this envelope by a given distance in all directions.
     * Both positive and negative distances are supported.
     *
     * @param distance the distance to expand the envelope
     */
    public void expandBy(double distance) {
        this.expandBy(distance, distance);
    }

    /**
     * Expands this envelope by a given distance in all directions.
     * Both positive and negative distances are supported.
     *
     * @param deltaX the distance to expand the envelope along the the X axis
     * @param deltaY the distance to expand the envelope along the the Y axis
     */
    public void expandBy(double deltaX, double deltaY) {
        if (this.isNull()) {
            return;
        }

        this.minx -= deltaX;
        this.maxx += deltaX;
        this.miny -= deltaY;
        this.maxy += deltaY;

        // check for envelope disappearing
        if (this.minx > this.maxx || this.miny > this.maxy) {
            this.setToNull();
        }
    }

    /**
     * Enlarges this <code>Envelope</code> so that it contains
     * the given point.
     * Has no effect if the point is already on or within the envelope.
     *
     * @param x the value to lower the minimum x to or to raise the maximum x to
     * @param y the value to lower the minimum y to or to raise the maximum y to
     */
    public void expandToInclude(double x, double y) {
        if (this.isNull()) {
            this.minx = x;
            this.maxx = x;
            this.miny = y;
            this.maxy = y;
        } else {
            if (x < this.minx) {
                this.minx = x;
            }
            if (x > this.maxx) {
                this.maxx = x;
            }
            if (y < this.miny) {
                this.miny = y;
            }
            if (y > this.maxy) {
                this.maxy = y;
            }
        }
    }

    /**
     * Enlarges this <code>Envelope</code> so that it contains
     * the <code>other</code> Envelope.
     * Has no effect if <code>other</code> is wholly on or
     * within the envelope.
     *
     * @param other the <code>Envelope</code> to expand to include
     */
    public void expandToInclude(Envelope other) {
        if (other.isNull()) {
            return;
        }
        if (this.isNull()) {
            this.minx = other.getMinX();
            this.maxx = other.getMaxX();
            this.miny = other.getMinY();
            this.maxy = other.getMaxY();
        } else {
            if (other.minx < this.minx) {
                this.minx = other.minx;
            }
            if (other.maxx > this.maxx) {
                this.maxx = other.maxx;
            }
            if (other.miny < this.miny) {
                this.miny = other.miny;
            }
            if (other.maxy > this.maxy) {
                this.maxy = other.maxy;
            }
        }
    }

    /**
     * Translates this envelope by given amounts in the X and Y direction.
     *
     * @param transX the amount to translate along the X axis
     * @param transY the amount to translate along the Y axis
     */
    public void translate(double transX, double transY) {
        if (this.isNull()) {
            return;
        }
        this.init(this.getMinX() + transX, this.getMaxX() + transX,
                this.getMinY() + transY, this.getMaxY() + transY);
    }

    /**
     * Computes the coordinate of the centre of this envelope (as long as it is non-null
     *
     * @return the centre coordinate of this envelope
     * <code>null</code> if the envelope is null
     */
    public Coordinate centre() {
        if (this.isNull()) {
            return null;
        }
        return new Coordinate(
                (this.getMinX() + this.getMaxX()) / 2.0,
                (this.getMinY() + this.getMaxY()) / 2.0);
    }

    /**
     * Computes the intersection of two {@link Envelope}s.
     *
     * @param env the envelope to intersect with
     * @return a new Envelope representing the intersection of the envelopes (this will be
     * the null envelope if either argument is null, or they do not intersect
     */
    public Envelope intersection(Envelope env) {
        if (this.isNull() || env.isNull() || !this.intersects(env)) {
            return new Envelope();
        }

        double intMinX = this.minx > env.minx ? this.minx : env.minx;
        double intMinY = this.miny > env.miny ? this.miny : env.miny;
        double intMaxX = this.maxx < env.maxx ? this.maxx : env.maxx;
        double intMaxY = this.maxy < env.maxy ? this.maxy : env.maxy;
        return new Envelope(intMinX, intMaxX, intMinY, intMaxY);
    }

    /**
     * Check if the region defined by <code>other</code>
     * overlaps (intersects) the region of this <code>Envelope</code>.
     *
     * @param other the <code>Envelope</code> which this <code>Envelope</code> is
     * being checked for overlapping
     * @return <code>true</code> if the <code>Envelope</code>s overlap
     */
    public boolean intersects(Envelope other) {
        if (this.isNull() || other.isNull()) {
            return false;
        }
        return !(other.minx > this.maxx ||
                other.maxx < this.minx ||
                other.miny > this.maxy ||
                other.maxy < this.miny);
    }

    /**
     * @deprecated Use #intersects instead. In the future, #overlaps may be
     * changed to be a true overlap check; that is, whether the intersection is
     * two-dimensional.
     */
    public boolean overlaps(Envelope other) {
        return this.intersects(other);
    }

    /**
     * Check if the point <code>p</code>
     * overlaps (lies inside) the region of this <code>Envelope</code>.
     *
     * @param p the <code>Coordinate</code> to be tested
     * @return <code>true</code> if the point overlaps this <code>Envelope</code>
     */
    public boolean intersects(Coordinate p) {
        return this.intersects(p.x, p.y);
    }

    /**
     * @deprecated Use #intersects instead.
     */
    public boolean overlaps(Coordinate p) {
        return this.intersects(p);
    }

    /**
     * Check if the point <code>(x, y)</code>
     * overlaps (lies inside) the region of this <code>Envelope</code>.
     *
     * @param x the x-ordinate of the point
     * @param y the y-ordinate of the point
     * @return <code>true</code> if the point overlaps this <code>Envelope</code>
     */
    public boolean intersects(double x, double y) {
        if (this.isNull()) {
            return false;
        }
        return !(x > this.maxx ||
                x < this.minx ||
                y > this.maxy ||
                y < this.miny);
    }

    /**
     * @deprecated Use #intersects instead.
     */
    public boolean overlaps(double x, double y) {
        return this.intersects(x, y);
    }

    /**
     * Tests if the <code>Envelope other</code>
     * lies wholely inside this <code>Envelope</code> (inclusive of the boundary).
     * <p>
     * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
     * which would exclude the envelope boundary.
     *
     * @param other the <code>Envelope</code> to check
     * @return true if <code>other</code> is contained in this <code>Envelope</code>
     * @see #covers(Envelope)
     */
    public boolean contains(Envelope other) {
        return this.covers(other);
    }

    /**
     * Tests if the given point lies in or on the envelope.
     * <p>
     * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
     * which would exclude the envelope boundary.
     *
     * @param p the point which this <code>Envelope</code> is
     * being checked for containing
     * @return <code>true</code> if the point lies in the interior or
     * on the boundary of this <code>Envelope</code>.
     * @see #covers(Coordinate)
     */
    public boolean contains(Coordinate p) {
        return this.covers(p);
    }

    /**
     * Tests if the given point lies in or on the envelope.
     * <p>
     * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
     * which would exclude the envelope boundary.
     *
     * @param x the x-coordinate of the point which this <code>Envelope</code> is
     * being checked for containing
     * @param y the y-coordinate of the point which this <code>Envelope</code> is
     * being checked for containing
     * @return <code>true</code> if <code>(x, y)</code> lies in the interior or
     * on the boundary of this <code>Envelope</code>.
     * @see #covers(double, double)
     */
    public boolean contains(double x, double y) {
        return this.covers(x, y);
    }

    /**
     * Tests if the given point lies in or on the envelope.
     *
     * @param x the x-coordinate of the point which this <code>Envelope</code> is
     * being checked for containing
     * @param y the y-coordinate of the point which this <code>Envelope</code> is
     * being checked for containing
     * @return <code>true</code> if <code>(x, y)</code> lies in the interior or
     * on the boundary of this <code>Envelope</code>.
     */
    public boolean covers(double x, double y) {
        if (this.isNull()) {
            return false;
        }
        return x >= this.minx &&
                x <= this.maxx &&
                y >= this.miny &&
                y <= this.maxy;
    }

    /**
     * Tests if the given point lies in or on the envelope.
     *
     * @param p the point which this <code>Envelope</code> is
     * being checked for containing
     * @return <code>true</code> if the point lies in the interior or
     * on the boundary of this <code>Envelope</code>.
     */
    public boolean covers(Coordinate p) {
        return this.covers(p.x, p.y);
    }

    /**
     * Tests if the <code>Envelope other</code>
     * lies wholely inside this <code>Envelope</code> (inclusive of the boundary).
     *
     * @param other the <code>Envelope</code> to check
     * @return true if this <code>Envelope</code> covers the <code>other</code>
     */
    public boolean covers(Envelope other) {
        if (this.isNull() || other.isNull()) {
            return false;
        }
        return other.getMinX() >= this.minx &&
                other.getMaxX() <= this.maxx &&
                other.getMinY() >= this.miny &&
                other.getMaxY() <= this.maxy;
    }

    /**
     * Computes the distance between this and another
     * <code>Envelope</code>.
     * The distance between overlapping Envelopes is 0.  Otherwise, the
     * distance is the Euclidean distance between the closest points.
     */
    public double distance(Envelope env) {
        if (this.intersects(env)) {
            return 0;
        }

        double dx = 0.0;
        if (this.maxx < env.minx) {
            dx = env.minx - this.maxx;
        } else if (this.minx > env.maxx) {
            dx = this.minx - env.maxx;
        }

        double dy = 0.0;
        if (this.maxy < env.miny) {
            dy = env.miny - this.maxy;
        } else if (this.miny > env.maxy) {
            dy = this.miny - env.maxy;
        }

        // if either is zero, the envelopes overlap either vertically or horizontally
        if (dx == 0.0) {
            return dy;
        }
        if (dy == 0.0) {
            return dx;
        }
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean equals(Object other) {
        if (!(other instanceof Envelope)) {
            return false;
        }
        Envelope otherEnvelope = (Envelope) other;
        if (this.isNull()) {
            return otherEnvelope.isNull();
        }
        return this.maxx == otherEnvelope.getMaxX() &&
                this.maxy == otherEnvelope.getMaxY() &&
                this.minx == otherEnvelope.getMinX() &&
                this.miny == otherEnvelope.getMinY();
    }

    public String toString() {
        return "Env[" + this.minx + " : " + this.maxx + ", " + this.miny + " : " + this.maxy + "]";
    }
}

