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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The notes class.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GssNote implements Externalizable {

    public static final String OBJID = "note";
    public static final int VERSION = 1;

    public long id;
    public double longitude;
    public double latitude;
    public double altitude;
    public long timeStamp;
    public String description = "";
    public String text = "";
    public String form = "";
    public String style = "";

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void externalize(DataOutputStream out) throws IOException {
        out.writeLong(id);
        out.writeDouble(longitude);
        out.writeDouble(latitude);
        out.writeDouble(altitude);
        out.writeLong(timeStamp);
        out.writeUTF(description == null ? "" : description);
        out.writeUTF(text == null ? "" : text);
        out.writeUTF(form == null ? "" : form);
        out.writeUTF(style == null ? "" : style);
    }

    @Override
    public void internalize(int version, DataInputStream in) throws IOException {
        if (version != VERSION) {
            throw new IllegalArgumentException("Wrong version: " + version + " vs " + VERSION);
        }
        id = in.readLong();
        longitude = in.readDouble();
        latitude = in.readDouble();
        altitude = in.readDouble();
        timeStamp = in.readLong();
        description = in.readUTF();
        text = in.readUTF();
        form = in.readUTF();
        style = in.readUTF();
    }

    @Override
    public String getObjectId() {
        return OBJID;
    }

}
