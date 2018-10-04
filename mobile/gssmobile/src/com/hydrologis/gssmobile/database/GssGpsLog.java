/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.gssmobile.database;

import com.codename1.io.Externalizable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The gps log class.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GssGpsLog implements Externalizable {

    public static final String OBJID = "gpslog";
    public static final int VERSION = 1;

    public long id;
    public String name;
    public long startts;
    public long endts;
    public String color;
    public float width;
    public List<GssGpsLogPoint> points = new ArrayList<>();

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void externalize(DataOutputStream out) throws IOException {
        out.writeLong(id);
        out.writeUTF(name);
        out.writeLong(startts);
        out.writeLong(endts);
        out.writeUTF(color);
        out.writeFloat(width);
        out.writeInt(points.size());
        for (GssGpsLogPoint point : points) {
            out.writeDouble(point.longitude);
            out.writeDouble(point.latitude);
            out.writeDouble(point.altimetry);
            out.writeLong(point.ts);
        }
    }

    @Override
    public void internalize(int version, DataInputStream in) throws IOException {
        if (version != VERSION) {
            throw new IllegalArgumentException("Wrong version: " + version + " vs " + VERSION);
        }
        id = in.readLong();
        name = in.readUTF();
        startts = in.readLong();
        endts = in.readLong();
        color = in.readUTF();
        width = in.readFloat();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            GssGpsLogPoint p = new GssGpsLogPoint();
            p.longitude = in.readDouble();
            p.latitude = in.readDouble();
            p.altimetry = in.readDouble();
            p.ts = in.readLong();
            points.add(p);
        }
    }

    @Override
    public String getObjectId() {
        return OBJID;
    }

}
