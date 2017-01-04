//   Rectangle.java
//   Java Spatial Index Library
//   Copyright (C) 2002-2005 Infomatiq Limited
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

package com.infomatiq.jsi;

/**
 * Currently hardcoded to 2 dimensions, but could be extended.
 */
public class Rectangle {

    /**
     * use primitives instead of arrays for the coordinates of the rectangle,
     * to reduce memory requirements.
     */
    public float minX, minY, maxX, maxY;

    public Rectangle() {
        this.minX = Float.MAX_VALUE;
        this.minY = Float.MAX_VALUE;
        this.maxX = -Float.MAX_VALUE;
        this.maxY = -Float.MAX_VALUE;
    }

    /**
     * Constructor.
     *
     * @param x1 coordinate of any corner of the rectangle
     * @param y1 (see x1)
     * @param x2 coordinate of the opposite corner
     * @param y2 (see x2)
     */
    public Rectangle(float x1, float y1, float x2, float y2) {
        this.set(x1, y1, x2, y2);
    }

    /**
     * Sets the size of the rectangle.
     *
     * @param x1 coordinate of any corner of the rectangle
     * @param y1 (see x1)
     * @param x2 coordinate of the opposite corner
     * @param y2 (see x2)
     */
    public void set(float x1, float y1, float x2, float y2) {
        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2);
    }

    /**
     * Sets the size of this rectangle to equal the passed rectangle.
     */
    public void set(Rectangle r) {
        this.minX = r.minX;
        this.minY = r.minY;
        this.maxX = r.maxX;
        this.maxY = r.maxY;
    }

    /**
     * Make a copy of this rectangle
     *
     * @return copy of this rectangle
     */
    public Rectangle copy() {
        return new Rectangle(this.minX, this.minY, this.maxX, this.maxY);
    }

    /**
     * Determine whether an edge of this rectangle overlies the equivalent
     * edge of the passed rectangle
     */
    public boolean edgeOverlaps(Rectangle r) {
        return this.minX == r.minX || this.maxX == r.maxX || this.minY == r.minY || this.maxY == r.maxY;
    }

    /**
     * Determine whether this rectangle intersects the passed rectangle
     *
     * @param r The rectangle that might intersect this rectangle
     * @return true if the rectangles intersect, false if they do not intersect
     */
    public boolean intersects(Rectangle r) {
        return this.maxX >= r.minX && this.minX <= r.maxX && this.maxY >= r.minY && this.minY <= r.maxY;
    }

    /**
     * Determine whether or not two rectangles intersect
     *
     * @param r1MinX minimum X coordinate of rectangle 1
     * @param r1MinY minimum Y coordinate of rectangle 1
     * @param r1MaxX maximum X coordinate of rectangle 1
     * @param r1MaxY maximum Y coordinate of rectangle 1
     * @param r2MinX minimum X coordinate of rectangle 2
     * @param r2MinY minimum Y coordinate of rectangle 2
     * @param r2MaxX maximum X coordinate of rectangle 2
     * @param r2MaxY maximum Y coordinate of rectangle 2
     * @return true if r1 intersects r2, false otherwise.
     */
    static public boolean intersects(float r1MinX, float r1MinY, float r1MaxX,
                                     float r1MaxY,
                                     float r2MinX, float r2MinY, float r2MaxX, float r2MaxY) {
        return r1MaxX >= r2MinX && r1MinX <= r2MaxX && r1MaxY >= r2MinY
                && r1MinY <= r2MaxY;
    }

    /**
     * Determine whether this rectangle contains the passed rectangle
     *
     * @param r The rectangle that might be contained by this rectangle
     * @return true if this rectangle contains the passed rectangle, false if
     * it does not
     */
    public boolean contains(Rectangle r) {
        return this.maxX >= r.maxX && this.minX <= r.minX && this.maxY >= r.maxY && this.minY <= r.minY;
    }

    /**
     * Determine whether or not one rectangle contains another.
     *
     * @param r1MinX minimum X coordinate of rectangle 1
     * @param r1MinY minimum Y coordinate of rectangle 1
     * @param r1MaxX maximum X coordinate of rectangle 1
     * @param r1MaxY maximum Y coordinate of rectangle 1
     * @param r2MinX minimum X coordinate of rectangle 2
     * @param r2MinY minimum Y coordinate of rectangle 2
     * @param r2MaxX maximum X coordinate of rectangle 2
     * @param r2MaxY maximum Y coordinate of rectangle 2
     * @return true if r1 contains r2, false otherwise.
     */
    static public boolean contains(float r1MinX, float r1MinY, float r1MaxX,
                                   float r1MaxY,
                                   float r2MinX, float r2MinY, float r2MaxX, float r2MaxY) {
        return r1MaxX >= r2MaxX && r1MinX <= r2MinX && r1MaxY >= r2MaxY
                && r1MinY <= r2MinY;
    }

    /**
     * Determine whether this rectangle is contained by the passed rectangle
     *
     * @param r The rectangle that might contain this rectangle
     * @return true if the passed rectangle contains this rectangle, false if
     * it does not
     */
    public boolean containedBy(Rectangle r) {
        return r.maxX >= this.maxX && r.minX <= this.minX && r.maxY >= this.maxY && r.minY <= this.minY;
    }

    /**
     * Return the distance between this rectangle and the passed point.
     * If the rectangle contains the point, the distance is zero.
     *
     * @param p Point to find the distance to
     * @return distance beween this rectangle and the passed point.
     */
    public float distance(Point p) {
        float distanceSquared = 0;

        float temp = this.minX - p.x;
        if (temp < 0) {
            temp = p.x - this.maxX;
        }

        if (temp > 0) {
            distanceSquared += (temp * temp);
        }

        temp = this.minY - p.y;
        if (temp < 0) {
            temp = p.y - this.maxY;
        }

        if (temp > 0) {
            distanceSquared += (temp * temp);
        }

        return (float) Math.sqrt(distanceSquared);
    }

    /**
     * Return the distance between a rectangle and a point.
     * If the rectangle contains the point, the distance is zero.
     *
     * @param minX minimum X coordinate of rectangle
     * @param minY minimum Y coordinate of rectangle
     * @param maxX maximum X coordinate of rectangle
     * @param maxY maximum Y coordinate of rectangle
     * @param pX X coordinate of point
     * @param pY Y coordinate of point
     * @return distance beween this rectangle and the passed point.
     */
    static public float distance(float minX, float minY, float maxX, float maxY,
                                 float pX, float pY) {
        return (float) Math.sqrt(distanceSq(minX, minY, maxX, maxY, pX, pY));
    }

    static public float distanceSq(float minX, float minY, float maxX,
                                   float maxY, float pX, float pY) {
        float distanceSqX = 0;
        float distanceSqY = 0;

        if (minX > pX) {
            distanceSqX = minX - pX;
            distanceSqX *= distanceSqX;
        } else if (pX > maxX) {
            distanceSqX = pX - maxX;
            distanceSqX *= distanceSqX;
        }

        if (minY > pY) {
            distanceSqY = minY - pY;
            distanceSqY *= distanceSqY;
        } else if (pY > maxY) {
            distanceSqY = pY - maxY;
            distanceSqY *= distanceSqY;
        }

        return distanceSqX + distanceSqY;
    }

    /**
     * Return the distance between this rectangle and the passed rectangle.
     * If the rectangles overlap, the distance is zero.
     *
     * @param r Rectangle to find the distance to
     * @return distance between this rectangle and the passed rectangle
     */

    public float distance(Rectangle r) {
        float distanceSquared = 0;
        float greatestMin = Math.max(this.minX, r.minX);
        float leastMax = Math.min(this.maxX, r.maxX);
        if (greatestMin > leastMax) {
            distanceSquared += ((greatestMin - leastMax) * (greatestMin - leastMax));
        }
        greatestMin = Math.max(this.minY, r.minY);
        leastMax = Math.min(this.maxY, r.maxY);
        if (greatestMin > leastMax) {
            distanceSquared += ((greatestMin - leastMax) * (greatestMin - leastMax));
        }
        return (float) Math.sqrt(distanceSquared);
    }

    /**
     * Calculate the area by which this rectangle would be enlarged if
     * added to the passed rectangle. Neither rectangle is altered.
     *
     * @param r Rectangle to union with this rectangle, in order to
     * compute the difference in area of the union and the
     * original rectangle
     * @return enlargement
     */
    public float enlargement(Rectangle r) {
        float enlargedArea = (Math.max(this.maxX, r.maxX) - Math.min(this.minX, r.minX)) *
                (Math.max(this.maxY, r.maxY) - Math.min(this.minY, r.minY));

        return enlargedArea - this.area();
    }

    /**
     * Calculate the area by which a rectangle would be enlarged if
     * added to the passed rectangle..
     *
     * @param r1MinX minimum X coordinate of rectangle 1
     * @param r1MinY minimum Y coordinate of rectangle 1
     * @param r1MaxX maximum X coordinate of rectangle 1
     * @param r1MaxY maximum Y coordinate of rectangle 1
     * @param r2MinX minimum X coordinate of rectangle 2
     * @param r2MinY minimum Y coordinate of rectangle 2
     * @param r2MaxX maximum X coordinate of rectangle 2
     * @param r2MaxY maximum Y coordinate of rectangle 2
     * @return enlargement
     */
    static public float enlargement(float r1MinX, float r1MinY, float r1MaxX,
                                    float r1MaxY,
                                    float r2MinX, float r2MinY, float r2MaxX, float r2MaxY) {
        float r1Area = (r1MaxX - r1MinX) * (r1MaxY - r1MinY);

        if (r1Area == Float.POSITIVE_INFINITY) {
            return 0; // cannot enlarge an infinite rectangle...
        }

        if (r2MinX < r1MinX) {
            r1MinX = r2MinX;
        }
        if (r2MinY < r1MinY) {
            r1MinY = r2MinY;
        }
        if (r2MaxX > r1MaxX) {
            r1MaxX = r2MaxX;
        }
        if (r2MaxY > r1MaxY) {
            r1MaxY = r2MaxY;
        }

        float r1r2UnionArea = (r1MaxX - r1MinX) * (r1MaxY - r1MinY);

        if (r1r2UnionArea == Float.POSITIVE_INFINITY) {
            // if a finite rectangle is enlarged and becomes infinite,
            // then the enlargement must be infinite.
            return Float.POSITIVE_INFINITY;
        }
        return r1r2UnionArea - r1Area;
    }

    /**
     * Compute the area of this rectangle.
     *
     * @return The area of this rectangle
     */
    public float area() {
        return (this.maxX - this.minX) * (this.maxY - this.minY);
    }

    /**
     * Compute the area of a rectangle.
     *
     * @param minX the minimum X coordinate of the rectangle
     * @param minY the minimum Y coordinate of the rectangle
     * @param maxX the maximum X coordinate of the rectangle
     * @param maxY the maximum Y coordinate of the rectangle
     * @return The area of the rectangle
     */
    static public float area(float minX, float minY, float maxX, float maxY) {
        return (maxX - minX) * (maxY - minY);
    }

    /**
     * Computes the union of this rectangle and the passed rectangle, storing
     * the result in this rectangle.
     *
     * @param r Rectangle to add to this rectangle
     */
    public void add(Rectangle r) {
        if (r.minX < this.minX) {
            this.minX = r.minX;
        }
        if (r.maxX > this.maxX) {
            this.maxX = r.maxX;
        }
        if (r.minY < this.minY) {
            this.minY = r.minY;
        }
        if (r.maxY > this.maxY) {
            this.maxY = r.maxY;
        }
    }

    /**
     * Computes the union of this rectangle and the passed point, storing
     * the result in this rectangle.
     *
     * @param p Point to add to this rectangle
     */
    public void add(Point p) {
        if (p.x < this.minX) {
            this.minX = p.x;
        }
        if (p.x > this.maxX) {
            this.maxX = p.x;
        }
        if (p.y < this.minY) {
            this.minY = p.y;
        }
        if (p.y > this.maxY) {
            this.maxY = p.y;
        }
    }

    /**
     * Find the the union of this rectangle and the passed rectangle.
     * Neither rectangle is altered
     *
     * @param r The rectangle to union with this rectangle
     */
    public Rectangle union(Rectangle r) {
        Rectangle union = this.copy();
        union.add(r);
        return union;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(this.maxX);
        result = prime * result + Float.floatToIntBits(this.maxY);
        result = prime * result + Float.floatToIntBits(this.minX);
        result = prime * result + Float.floatToIntBits(this.minY);
        return result;
    }

    /**
     * Determine whether this rectangle is equal to a given object.
     * Equality is determined by the bounds of the rectangle.
     *
     * @param o The object to compare with this rectangle
     */
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof Rectangle) {
            Rectangle r = (Rectangle) o;
            if (this.minX == r.minX && this.minY == r.minY && this.maxX == r.maxX && this.maxY == r.maxY) {
                equals = true;
            }
        }
        return equals;
    }

    /**
     * Determine whether this rectangle is the same as another object
     * <p>
     * Note that two rectangles can be equal but not the same object,
     * if they both have the same bounds.
     *
     * @param o The object to compare with this rectangle.
     */
    public boolean sameObject(Object o) {
        return super.equals(o);
    }

    /**
     * Return a string representation of this rectangle, in the form:
     * (1.2, 3.4), (5.6, 7.8)
     *
     * @return String String representation of this rectangle.
     */
    @Override
    public String toString() {
        return "(" + this.minX + ", " + this.minY + "), (" + this.maxX + ", " + this.maxY + ")";
    }

    /**
     * Utility methods (not used by JSI); added to
     * enable this to be used as a generic rectangle class
     */
    public float width() {
        return this.maxX - this.minX;
    }

    public float height() {
        return this.maxY - this.minY;
    }

    public float aspectRatio() {
        return this.width() / this.height();
    }

    public Point centre() {
        return new Point((this.minX + this.maxX) / 2, (this.minY + this.maxY) / 2);
    }
}
