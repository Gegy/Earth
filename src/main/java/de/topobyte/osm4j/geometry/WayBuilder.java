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
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class WayBuilder extends AbstractGeometryBuilder {

    final static Logger logger = LogManager.getLogger(WayBuilder.class);

    private NodeBuilder nodeBuilder;

    private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
    private MissingWayNodeStrategy missingWayNodeStrategy = MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE;
    private boolean includePuntal = true;
    private boolean log = false;
    private LogLevel logLevel = LogLevel.WARN;

    public WayBuilder() {
        this(new GeometryFactory());
    }

    public WayBuilder(GeometryFactory factory) {
        super(factory);
        this.nodeBuilder = new NodeBuilder(factory);
    }

    public MissingEntitiesStrategy getMissingEntitiesStrategy() {
        return this.missingEntitiesStrategy;
    }

    public void setMissingEntitiesStrategy(
            MissingEntitiesStrategy missingEntitiesStrategy) {
        this.missingEntitiesStrategy = missingEntitiesStrategy;
    }

    public MissingWayNodeStrategy getMissingWayNodeStrategy() {
        return this.missingWayNodeStrategy;
    }

    public void setMissingWayNodeStrategy(
            MissingWayNodeStrategy missingWayNodeStrategy) {
        this.missingWayNodeStrategy = missingWayNodeStrategy;
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

    /**
     * Build a LinesString from the given way.
     *
     * @param way the way to use for building.
     * @return the constructed LineString.
     * @throws EntityNotFoundException if a node cannot be resolved.
     */
    public WayBuilderResult build(OsmWay way, OsmEntityProvider resolver)
            throws EntityNotFoundException {
        switch (this.missingEntitiesStrategy) {
            default:
            case THROW_EXCEPTION:
                return this.buildThrowExceptionIfNodeMissing(way, resolver);
            case BUILD_EMPTY:
                return this.buildReturnEmptyIfNodeMissing(way, resolver);
            case BUILD_PARTIAL:
                switch (this.missingWayNodeStrategy) {
                    default:
                    case OMIT_VERTEX_FROM_POLYLINE:
                        return this.buildOmitVertexIfNodeMissing(way, resolver);
                    case SPLIT_POLYLINE:
                        return this.buildSplitIfNodeMissing(way, resolver);
                }
        }
    }

    public WayBuilderResult buildThrowExceptionIfNodeMissing(OsmWay way,
                                                             OsmEntityProvider resolver) throws EntityNotFoundException {
        WayBuilderResult result = new WayBuilderResult();

        int numNodes = way.getNumberOfNodes();
        if (numNodes == 0) {
            return result;
        }
        if (numNodes == 1) {
            if (!this.includePuntal) {
                return result;
            } else {
                OsmNode node = resolver.getNode(way.getNodeId(0));
                result.getCoordinates().add(this.nodeBuilder.buildCoordinate(node));
            }
        }

        CoordinateSequence cs = this.factory.getCoordinateSequenceFactory().create(
                numNodes, 2);

        for (int i = 0; i < numNodes; i++) {
            OsmNode node = resolver.getNode(way.getNodeId(i));
            cs.setOrdinate(i, 0, node.getLongitude());
            cs.setOrdinate(i, 1, node.getLatitude());
        }
        this.createLine(result, cs, OsmModelUtil.isClosed(way));

        return result;
    }

    public WayBuilderResult buildReturnEmptyIfNodeMissing(OsmWay way,
                                                          OsmEntityProvider resolver) {
        WayBuilderResult result = new WayBuilderResult();

        int numNodes = way.getNumberOfNodes();
        if (numNodes == 0) {
            return result;
        }
        if (numNodes == 1) {
            if (!this.includePuntal) {
                return result;
            } else {
                try {
                    OsmNode node = resolver.getNode(way.getNodeId(0));
                    result.getCoordinates().add(
                            this.nodeBuilder.buildCoordinate(node));
                } catch (EntityNotFoundException e) {
                    return result;
                }
            }
        }

        CoordinateSequence cs = this.factory.getCoordinateSequenceFactory().create(
                numNodes, 2);

        for (int i = 0; i < way.getNumberOfNodes(); i++) {
            OsmNode node;
            try {
                node = resolver.getNode(way.getNodeId(i));
            } catch (EntityNotFoundException e) {
                result.clear();
                return result;
            }
            cs.setOrdinate(i, 0, node.getLongitude());
            cs.setOrdinate(i, 1, node.getLatitude());
        }
        this.createLine(result, cs, OsmModelUtil.isClosed(way));

        return result;
    }

    public WayBuilderResult buildOmitVertexIfNodeMissing(OsmWay way,
                                                         OsmEntityProvider resolver) {
        WayBuilderResult result = new WayBuilderResult();

        // Test if the way is closed, i.e. first node id == last node id
        boolean closed = OsmModelUtil.isClosed(way);
        // Remember if the first node is missing, so that we can handle closed
        // ways appropriately
        boolean firstMissing = false;

        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < way.getNumberOfNodes(); i++) {
            OsmNode node;
            try {
                node = resolver.getNode(way.getNodeId(i));
            } catch (EntityNotFoundException e) {
                if (this.log) {
                    this.logMissingNode(way.getNodeId(i));
                }
                if (i == 0) {
                    firstMissing = true;
                }
                continue;
            }
            coords.add(new Coordinate(node.getLongitude(), node.getLatitude()));
        }

        if (coords.size() == 0) {
            return result;
        }
        if (coords.size() == 1) {
            if (!this.includePuntal) {
                return result;
            } else {
                result.getCoordinates().add(coords.get(0));
                return result;
            }
        }

        // If the way is closed, but the first coordinate is missing, then close
        // the way by replicating the first found coordinate at the end.
        if (closed && firstMissing && coords.size() > 2) {
            coords.add(coords.get(0));
        }

        CoordinateSequence cs = this.factory.getCoordinateSequenceFactory().create(
                coords.toArray(new Coordinate[0]));
        this.createLine(result, cs, closed);

        return result;
    }

    public WayBuilderResult buildSplitIfNodeMissing(OsmWay way,
                                                    OsmEntityProvider resolver) {
        // Test if the way is closed, i.e. first node id == last node id
        boolean closed = OsmModelUtil.isClosed(way);
        // Remember if the first node is missing, so that we can handle closed
        // ways appropriately
        boolean firstMissing = false;

        CoordinateSequencesBuilder builder = new CoordinateSequencesBuilder();
        builder.beginNewSequence();

        for (int i = 0; i < way.getNumberOfNodes(); i++) {
            OsmNode node;
            try {
                node = resolver.getNode(way.getNodeId(i));
            } catch (EntityNotFoundException e) {
                if (this.log) {
                    this.logMissingNode(way.getNodeId(i));
                }
                if (i == 0) {
                    firstMissing = true;
                }
                builder.beginNewSequence();
                continue;
            }
            builder.add(new Coordinate(node.getLongitude(), node.getLatitude()));
        }
        builder.finishSequence();

        return builder.createWayBuilderResult(this.factory, this.includePuntal, closed,
                firstMissing);
    }

    private void logMissingNode(long nodeId) {
        String message = String.format("Node not found: %d", nodeId);
        this.log(message);
    }

    private void log(String message) {
        switch (this.logLevel) {
            default:
            case INFO:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case WARN:
                logger.warn(message);
                break;
        }
    }

    private void createLine(WayBuilderResult result, CoordinateSequence cs,
                            boolean close) {
        if (close && cs.size() > 3) {
            result.setLinearRing(this.factory.createLinearRing(cs));
        } else {
            result.getLineStrings().add(this.factory.createLineString(cs));
        }
    }
}
