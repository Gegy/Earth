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

package de.topobyte.osm4j.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.adt.multicollections.CountingMultiValMap;
import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.jts.utils.PolygonHelper;
import de.topobyte.jts.utils.SelfIntersectionUtil;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.relation.ChainOfNodes;
import de.topobyte.osm4j.geometry.relation.ChainOfWays;
import de.topobyte.osm4j.geometry.relation.RelationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class RegionBuilder extends AbstractGeometryBuilder {

    final static Logger logger = LogManager.getLogger(RegionBuilder.class);

    private NodeBuilder nodeBuilder;
    private WayBuilder wayBuilder;

    private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
    private MissingWayNodeStrategy missingWayNodeStrategy = MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE;
    private boolean includePuntal = true;
    private boolean includeLineal = true;
    private boolean log = false;
    private LogLevel logLevel = LogLevel.WARN;

    public RegionBuilder() {
        this(new GeometryFactory());
    }

    public RegionBuilder(GeometryFactory factory) {
        super(factory);
        this.nodeBuilder = new NodeBuilder(factory);
        this.wayBuilder = new WayBuilder(factory);
        this.wayBuilder.setMissingEntitiesStrategy(this.missingEntitiesStrategy);
        this.wayBuilder.setMissingWayNodeStrategy(this.missingWayNodeStrategy);
    }

    public boolean isLog() {
        return this.log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public MissingEntitiesStrategy getMissingEntitiesStrategy() {
        return this.missingEntitiesStrategy;
    }

    public void setMissingEntitiesStrategy(
            MissingEntitiesStrategy missingEntitiesStrategy) {
        this.missingEntitiesStrategy = missingEntitiesStrategy;
        this.wayBuilder.setMissingEntitiesStrategy(missingEntitiesStrategy);
    }

    public MissingWayNodeStrategy getMissingWayNodeStrategy() {
        return this.missingWayNodeStrategy;
    }

    public void setMissingWayNodeStrategy(
            MissingWayNodeStrategy missingWayNodeStrategy) {
        this.missingWayNodeStrategy = missingWayNodeStrategy;
        this.wayBuilder.setMissingWayNodeStrategy(missingWayNodeStrategy);
    }

    public boolean isIncludePuntal() {
        return this.includePuntal;
    }

    public void setIncludePuntal(boolean includePuntal) {
        this.includePuntal = includePuntal;
    }

    public boolean isIncludeLineal() {
        return this.includeLineal;
    }

    public void setIncludeLineal(boolean includeLineal) {
        this.includeLineal = includeLineal;
    }

    /**
     * Build a MultiPolygon from a Relation.
     *
     * @param relation a relation to construct the region for.
     * @return the constructed MultiPolygon.
     * @throws EntityNotFoundException when a required entity cannot be obtained.
     */
    public RegionBuilderResult build(OsmRelation relation,
                                     OsmEntityProvider resolver) throws EntityNotFoundException {
        EntityNotFoundStrategy enfs = Util.strategy(this.missingEntitiesStrategy,
                this.log, this.logLevel);

        logger.debug("building region id:" + relation.getId());
        Set<OsmRelation> relations = new HashSet<>();
        MultiSet<OsmWay> ways = new HashMultiSet<>();
        EntityFinder finder = EntityFinders.create(resolver, enfs);
        relations.add(relation);
        finder.findMemberRelations(relation, relations);
        finder.findMemberWays(relations, ways);

        Set<OsmNode> nodes = new HashSet<>();
        if (this.includePuntal) {
            finder.findMemberNodes(relations, nodes);
        }
        return this.build(ways, resolver, nodes);
    }

    public RegionBuilderResult build(OsmWay way, OsmEntityProvider resolver)
            throws EntityNotFoundException {
        MultiSet<OsmWay> ways = new HashMultiSet<>();
        ways.add(way);
        return this.build(ways, resolver, new HashSet<>());
    }

    /**
     * Build a MultiPolygon from a Set of Ways.
     *
     * @param ways the ways to use for geometry construction.
     * @return the constructed MultiPolygon.
     * @throws EntityNotFoundException when a required entity cannot be obtained.
     */
    public RegionBuilderResult build(MultiSet<OsmWay> ways,
                                     OsmEntityProvider resolver, Set<OsmNode> nodes)
            throws EntityNotFoundException {
        RegionBuilderResult result = new RegionBuilderResult();
        logger.debug("Have " + ways.keySet().size() + " ways");

        // Only keep ways with 2 or more nodes. Also don't keep ways with just 2
        // times the same node.
        List<OsmWay> invalidWays = new ArrayList<>();
        for (OsmWay way : ways.keySet()) {
            int numNodes = way.getNumberOfNodes();
            if (numNodes == 0 || numNodes == 1) {
                invalidWays.add(way);
            } else if (numNodes == 2 && OsmModelUtil.isClosed(way)) {
                invalidWays.add(way);
            }
        }
        ways.removeAllOccurences(invalidWays);
        logger.debug("Removed " + invalidWays.size() + " invalid ways");

        CountingMultiValMap<Long, OsmWay> wayTailMap = RelationUtil
                .buildWayTailMap(ways);

        List<ChainOfWays> chains = RelationUtil.buildRings(ways, wayTailMap);

        List<ChainOfNodes> rings = new ArrayList<>();
        List<ChainOfNodes> nonRings = new ArrayList<>();

        RelationUtil.convertToSegmentChainsAndResolveNodeIntersections(chains,
                rings, nonRings);

        try {
            RelationUtil.checkRings(chains, resolver, this.missingEntitiesStrategy);
        } catch (EntityNotFoundException e) {
            switch (this.missingEntitiesStrategy) {
                case BUILD_PARTIAL:
                    // Can't happen in this case because checkRings won't throw this
                    // exception with BUILD_PARTIAL
                    break;
                case BUILD_EMPTY:
                    return new RegionBuilderResult();
                default:
                case THROW_EXCEPTION:
                    throw (e);
            }
        }

        // This could be used to close non-closed chains
        // RelationUtil.closeUnclosedRingWithStraightLine(rings);

        List<LinearRing> linearRings = new ArrayList<>();

        this.convert(rings, nonRings, resolver, result.getCoordinates(),
                result.getLineStrings(), linearRings);

        Set<LinearRing> validRings = new HashSet<>();
        for (LinearRing r : linearRings) {
            Set<LinearRing> repaired = SelfIntersectionUtil.repair(r);
            for (LinearRing ring : repaired) {
                validRings.add(ring);
            }
        }

        MultiPolygon mp = PolygonHelper
                .multipolygonFromRings(validRings, false);
        result.setMultiPolygon(mp);

        if (this.includePuntal) {
            GeometryUtil
                    .buildNodes(this.nodeBuilder, nodes, result.getCoordinates());
        }

        return result;
    }

    private void convert(Collection<ChainOfNodes> rings,
                         Collection<ChainOfNodes> nonRings, OsmEntityProvider resolver,
                         Collection<Coordinate> coordinates,
                         Collection<LineString> lineStrings,
                         Collection<LinearRing> linearRings) throws EntityNotFoundException {
        for (ChainOfNodes ring : rings) {
            Way way = new Way(-1, ring.getNodes());
            WayBuilderResult result = this.wayBuilder.build(way, resolver);
            this.add(result, coordinates, lineStrings, linearRings);
        }
        for (ChainOfNodes ring : nonRings) {
            Way way = new Way(-1, ring.getNodes());
            WayBuilderResult result = this.wayBuilder.build(way, resolver);
            this.add(result, coordinates, lineStrings, linearRings);
        }
    }

    private void add(WayBuilderResult result,
                     Collection<Coordinate> coordinates,
                     Collection<LineString> lineStrings,
                     Collection<LinearRing> linearRings) {
        if (this.includePuntal) {
            coordinates.addAll(result.getCoordinates());
        }
        if (this.includeLineal) {
            lineStrings.addAll(result.getLineStrings());
        }
        if (result.getLinearRing() != null) {
            linearRings.add(result.getLinearRing());
        }
    }
}
