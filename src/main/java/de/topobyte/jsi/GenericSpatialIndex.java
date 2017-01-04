//  This file is part of JSI.
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
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.topobyte.jsi;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import gnu.trove.procedure.TObjectProcedure;

import java.util.List;
import java.util.Set;

/**
 * An interface to a spatial index that stores generic values that occupy a
 * rectangular area.
 *
 * @param <T> the type of the elements to be saved in this index.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface GenericSpatialIndex<T> {

    /**
     * Add <code>thing</code> to this index with place-occupation specified by
     * <code>r</code>.
     *
     * @param r the element's bounding box
     * @param thing the element to add.
     */
    void add(Rectangle r, T thing);

    /**
     * Calls <code>procedure</code> on each element that is contained in the
     * given rectangle <code>r</code>.
     *
     * @param r the rectangle to query for contained elements.
     * @param procedure the procedure to call on each element.
     */
    void contains(Rectangle r, TObjectProcedure<T> procedure);

    /**
     * Acquire a set of all elements contained in the given rectangle
     * <code>r</code>.
     *
     * @param r the rectangle to query for contained elements.
     * @return the set of all elements contained in <code>r</code>.
     */
    Set<T> contains(Rectangle r);

    /**
     * Remove element <code>thing</code> from spatial index. The associated
     * rectangle <code>r</code> has to be provided.
     *
     * @param r the element's bounding box.
     * @param thing the element to remove.
     * @return whether deletion has been successful.
     */
    boolean delete(Rectangle r, T thing);

    /**
     * Calls <code>procedure</code> on each element that intersects the given
     * rectangle <code>r</code>.
     *
     * @param r the rectangle to query for intersecting elements.
     * @param procedure the procedure to call on each element.
     */
    void intersects(Rectangle r, TObjectProcedure<T> procedure);

    /**
     * Acquire a set of all elements intersecting the given rectangle
     * <code>r</code>.
     *
     * @param r the rectangle to query for intersecting elements.
     * @return the set of all elements intersecting <code>r</code>.
     */
    Set<T> intersects(Rectangle r);

    /**
     * Acquire a list of all elements intersecting the given rectangle
     * <code>r</code>.
     *
     * @param r the rectangle to query for intersecting elements.
     * @return the list of all element intersectin <code>r</code>
     */
    List<T> intersectionsAsList(Rectangle r);

    /**
     * Execute <code>procedure</code> on all elements that are in less or equal
     * distance of <code>p</code> than <code>distance</code>.
     *
     * @param p the point from where to measure distance.
     * @param procedure the procedure to call on each element.
     * @param distance the maximum distance from <code>p</code> of elements.
     */
    void nearest(Point p, TObjectProcedure<T> procedure, float distance);

    /**
     * Acquire a set of all elements that are in less or equal distance of
     * <code>p</code> than <code>distance</code>.
     *
     * @param p the point from where to measure distance.
     * @param distance the maximum distance from <code>p</code> of elements.
     * @return the set all elements found in less distance than
     * <code>distance</code>.
     */
    Set<T> nearest(Point p, float distance);

    /**
     * Returns the number of entries in this index.
     *
     * @return the number of elements.
     */
    int size();
}
