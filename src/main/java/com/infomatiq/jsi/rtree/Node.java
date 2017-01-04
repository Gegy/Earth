//   Node.java
//   Java Spatial Index Library
//   Copyright (C) 2002-2005 Infomatiq Limited
//   Copyright (C) 2008-2010 aled@sourceforge.net
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

package com.infomatiq.jsi.rtree;

import com.infomatiq.jsi.Rectangle;

/**
 * <p>Used by RTree. There are no public methods in this class.</p>
 */
public class Node {
    int nodeId = 0;
    float mbrMinX = Float.MAX_VALUE;
    float mbrMinY = Float.MAX_VALUE;
    float mbrMaxX = -Float.MAX_VALUE;
    float mbrMaxY = -Float.MAX_VALUE;

    float[] entriesMinX = null;
    float[] entriesMinY = null;
    float[] entriesMaxX = null;
    float[] entriesMaxY = null;

    int[] ids = null;
    int level;
    int entryCount;

    Node(int nodeId, int level, int maxNodeEntries) {
        this.nodeId = nodeId;
        this.level = level;
        this.entriesMinX = new float[maxNodeEntries];
        this.entriesMinY = new float[maxNodeEntries];
        this.entriesMaxX = new float[maxNodeEntries];
        this.entriesMaxY = new float[maxNodeEntries];
        this.ids = new int[maxNodeEntries];
    }

    void addEntry(float minX, float minY, float maxX, float maxY, int id) {
        this.ids[this.entryCount] = id;
        this.entriesMinX[this.entryCount] = minX;
        this.entriesMinY[this.entryCount] = minY;
        this.entriesMaxX[this.entryCount] = maxX;
        this.entriesMaxY[this.entryCount] = maxY;

        if (minX < this.mbrMinX) {
            this.mbrMinX = minX;
        }
        if (minY < this.mbrMinY) {
            this.mbrMinY = minY;
        }
        if (maxX > this.mbrMaxX) {
            this.mbrMaxX = maxX;
        }
        if (maxY > this.mbrMaxY) {
            this.mbrMaxY = maxY;
        }

        this.entryCount++;
    }

    // Return the index of the found entry, or -1 if not found
    int findEntry(float minX, float minY, float maxX, float maxY, int id) {
        for (int i = 0; i < this.entryCount; i++) {
            if (id == this.ids[i] &&
                    this.entriesMinX[i] == minX && this.entriesMinY[i] == minY &&
                    this.entriesMaxX[i] == maxX && this.entriesMaxY[i] == maxY) {
                return i;
            }
        }
        return -1;
    }

    // delete entry. This is done by setting it to null and copying the last entry into its space.
    void deleteEntry(int i) {
        int lastIndex = this.entryCount - 1;
        float deletedMinX = this.entriesMinX[i];
        float deletedMinY = this.entriesMinY[i];
        float deletedMaxX = this.entriesMaxX[i];
        float deletedMaxY = this.entriesMaxY[i];

        if (i != lastIndex) {
            this.entriesMinX[i] = this.entriesMinX[lastIndex];
            this.entriesMinY[i] = this.entriesMinY[lastIndex];
            this.entriesMaxX[i] = this.entriesMaxX[lastIndex];
            this.entriesMaxY[i] = this.entriesMaxY[lastIndex];
            this.ids[i] = this.ids[lastIndex];
        }
        this.entryCount--;

        // adjust the MBR
        this.recalculateMBRIfInfluencedBy(deletedMinX, deletedMinY, deletedMaxX,
                deletedMaxY);
    }

    // deletedMin/MaxX/Y is a rectangle that has just been deleted or made smaller.
    // Thus, the MBR is only recalculated if the deleted rectangle influenced the old MBR
    void recalculateMBRIfInfluencedBy(float deletedMinX, float deletedMinY,
                                      float deletedMaxX, float deletedMaxY) {
        if (this.mbrMinX == deletedMinX || this.mbrMinY == deletedMinY
                || this.mbrMaxX == deletedMaxX || this.mbrMaxY == deletedMaxY) {
            this.recalculateMBR();
        }
    }

    void recalculateMBR() {
        this.mbrMinX = this.entriesMinX[0];
        this.mbrMinY = this.entriesMinY[0];
        this.mbrMaxX = this.entriesMaxX[0];
        this.mbrMaxY = this.entriesMaxY[0];

        for (int i = 1; i < this.entryCount; i++) {
            if (this.entriesMinX[i] < this.mbrMinX) {
                this.mbrMinX = this.entriesMinX[i];
            }
            if (this.entriesMinY[i] < this.mbrMinY) {
                this.mbrMinY = this.entriesMinY[i];
            }
            if (this.entriesMaxX[i] > this.mbrMaxX) {
                this.mbrMaxX = this.entriesMaxX[i];
            }
            if (this.entriesMaxY[i] > this.mbrMaxY) {
                this.mbrMaxY = this.entriesMaxY[i];
            }
        }
    }

    /**
     * eliminate null entries, move all entries to the start of the source node
     */
    void reorganize(RTree rtree) {
        int countdownIndex = rtree.maxNodeEntries - 1;
        for (int index = 0; index < this.entryCount; index++) {
            if (this.ids[index] == -1) {
                while (this.ids[countdownIndex] == -1 && countdownIndex > index) {
                    countdownIndex--;
                }
                this.entriesMinX[index] = this.entriesMinX[countdownIndex];
                this.entriesMinY[index] = this.entriesMinY[countdownIndex];
                this.entriesMaxX[index] = this.entriesMaxX[countdownIndex];
                this.entriesMaxY[index] = this.entriesMaxY[countdownIndex];
                this.ids[index] = this.ids[countdownIndex];
                this.ids[countdownIndex] = -1;
            }
        }
    }

    public int getEntryCount() {
        return this.entryCount;
    }

    public int getId(int index) {
        if (index < this.entryCount) {
            return this.ids[index];
        }
        return -1;
    }

    public boolean isLeaf() {
        return (this.level == 1);
    }

    public int getLevel() {
        return this.level;
    }

    public float getMbrMinX() {
        return this.mbrMinX;
    }

    public float getMbrMinY() {
        return this.mbrMinY;
    }

    public float getMbrMaxX() {
        return this.mbrMaxX;
    }

    public float getMbrMaxY() {
        return this.mbrMaxY;
    }

    public Rectangle getMbb() {
        return new Rectangle(this.mbrMinX, this.mbrMinY, this.mbrMaxX, this.mbrMaxY);
    }

    public float getEntryMbrMinX(int index) {
        return this.entriesMinX[index];
    }

    public float getEntryMbrMinY(int index) {
        return this.entriesMinY[index];
    }

    public float getEntryMbrMaxX(int index) {
        return this.entriesMaxX[index];
    }

    public float getEntryMbrMaxY(int index) {
        return this.entriesMaxY[index];
    }

    public Rectangle getEntryMbb(int index) {
        return new Rectangle(this.entriesMinX[index], this.entriesMinY[index],
                this.entriesMaxX[index], this.entriesMaxY[index]);
    }
}
