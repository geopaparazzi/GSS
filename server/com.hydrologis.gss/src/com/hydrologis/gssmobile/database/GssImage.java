/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The notes class.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GssImage implements Externalizable {

    public static final String OBJID = "image";
    public static final int VERSION = 1;

    public long id;
    public double longitude;
    public double latitude;
    public double altitude;
    public long timeStamp;
    public double azimuth;
    public String text;
    public long noteId = -1;
    public int isDirty;
    public byte[] data;
    public byte[] dataThumb;

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void externalize( DataOutputStream out ) throws IOException {
        out.writeLong(id);
        out.writeDouble(longitude);
        out.writeDouble(latitude);
        out.writeDouble(altitude);
        out.writeLong(timeStamp);
        out.writeDouble(azimuth);
        out.writeUTF(text);
        out.writeLong(noteId);
        out.writeInt(data.length);
        out.write(data);
        out.writeInt(dataThumb.length);
        out.write(dataThumb);
    }

    @Override
    public void internalize( int version, DataInputStream in ) throws IOException {
        if (version != VERSION) {
            throw new IllegalArgumentException("Wrong version: " + version + " vs " + VERSION);
        }
        id = in.readLong();
        longitude = in.readDouble();
        latitude = in.readDouble();
        altitude = in.readDouble();
        timeStamp = in.readLong();
        azimuth = in.readDouble();
        text = in.readUTF();
        noteId = in.readLong();

        int dataSize = in.readInt();
        data = new byte[dataSize];
        in.read(data, 0, dataSize);

        dataSize = in.readInt();
        dataThumb = new byte[dataSize];
        in.read(dataThumb, 0, dataSize);
    }

    @Override
    public String getObjectId() {
        return OBJID;
    }

}
