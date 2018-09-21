/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.database;

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