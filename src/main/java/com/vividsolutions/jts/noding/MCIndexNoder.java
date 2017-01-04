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
package com.vividsolutions.jts.noding;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.chain.MonotoneChain;
import com.vividsolutions.jts.index.chain.MonotoneChainBuilder;
import com.vividsolutions.jts.index.chain.MonotoneChainOverlapAction;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Nodes a set of {@link SegmentString}s using a index based
 * on {@link MonotoneChain}s and a {@link SpatialIndex}.
 * The {@link SpatialIndex} used should be something that supports
 * envelope (range) queries efficiently (such as a <code>Quadtree</code>}
 * or {@link STRtree} (which is the default index provided).
 *
 * @version 1.7
 */
public class MCIndexNoder
        extends SinglePassNoder {
    private List monoChains = new ArrayList();
    private SpatialIndex index = new STRtree();
    private int idCounter = 0;
    private Collection nodedSegStrings;
    // statistics
    private int nOverlaps = 0;

    public MCIndexNoder() {
    }

    public MCIndexNoder(SegmentIntersector si) {
        super(si);
    }

    public List getMonotoneChains() {
        return this.monoChains;
    }

    public SpatialIndex getIndex() {
        return this.index;
    }

    @Override
    public Collection getNodedSubstrings() {
        return NodedSegmentString.getNodedSubstrings(this.nodedSegStrings);
    }

    @Override
    public void computeNodes(Collection inputSegStrings) {
        this.nodedSegStrings = inputSegStrings;
        for (Object inputSegString : inputSegStrings) {
            add((SegmentString) inputSegString);
        }
        this.intersectChains();
//System.out.println("MCIndexNoder: # chain overlaps = " + nOverlaps);
    }

    private void intersectChains() {
        MonotoneChainOverlapAction overlapAction = new SegmentOverlapAction(this.segInt);

        for (Object monoChain : monoChains) {
            MonotoneChain queryChain = (MonotoneChain) monoChain;
            List overlapChains = this.index.query(queryChain.getEnvelope());
            for (Object overlapChain : overlapChains) {
                MonotoneChain testChain = (MonotoneChain) overlapChain;
                /**
                 * following test makes sure we only compare each pair of chains once
                 * and that we don't compare a chain to itself
                 */
                if (testChain.getId() > queryChain.getId()) {
                    queryChain.computeOverlaps(testChain, overlapAction);
                    this.nOverlaps++;
                }
                // short-circuit if possible
                if (this.segInt.isDone()) {
                    return;
                }
            }
        }
    }

    private void add(SegmentString segStr) {
        List segChains = MonotoneChainBuilder.getChains(segStr.getCoordinates(), segStr);
        for (Object segChain : segChains) {
            MonotoneChain mc = (MonotoneChain) segChain;
            mc.setId(this.idCounter++);
            this.index.insert(mc.getEnvelope(), mc);
            this.monoChains.add(mc);
        }
    }

    public class SegmentOverlapAction
            extends MonotoneChainOverlapAction {
        private SegmentIntersector si = null;

        public SegmentOverlapAction(SegmentIntersector si) {
            this.si = si;
        }

        @Override
        public void overlap(MonotoneChain mc1, int start1, MonotoneChain mc2, int start2) {
            SegmentString ss1 = (SegmentString) mc1.getContext();
            SegmentString ss2 = (SegmentString) mc2.getContext();
            this.si.processIntersections(ss1, start1, ss2, start2);
        }
    }
}
