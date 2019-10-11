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
package com.hydrologis.kukuratus.utils.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.hydrologis.kukuratus.utils.KukuratusLogger;

public abstract class KmzExport {

    private final File outputFile;
    private String name;

    /**
     * Constructor.
     *
     * @param name       a name for the kmz.O
     * @param outputFile the path in which to create the kmz.
     */
    public KmzExport( String name, File outputFile ) {
        this.name = name;
        this.outputFile = outputFile;
    }

    /**
     * Export.
     *
     * @param kmlRepresenters the list of data representers.
     * @throws Exception if something goes wrong.
     */
    public void export( List<KmlRepresenter> kmlRepresenters ) throws Exception {
        if (name == null) {
            name = "KMZ Export"; //$NON-NLS-1$
        }

        /*
         * write the internal kml file
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
        stringBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n"); //$NON-NLS-1$
        stringBuilder.append("xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"); //$NON-NLS-1$
        stringBuilder.append("<Document>\n"); //$NON-NLS-1$
        stringBuilder.append("<name>"); //$NON-NLS-1$
        stringBuilder.append(name);
        stringBuilder.append("</name>\n"); //$NON-NLS-1$
        addMarker(stringBuilder, "red-pushpin", "http://maps.google.com/mapfiles/kml/pushpin/red-pushpin.png", 20, 2); //$NON-NLS-1$ //$NON-NLS-2$
        addMarker(stringBuilder, "yellow-pushpin", "http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png", 20, 2); //$NON-NLS-1$ //$NON-NLS-2$
        addMarker(stringBuilder, "bookmark-icon", "http://maps.google.com/mapfiles/kml/pal4/icon39.png", 16, 16); //$NON-NLS-1$ //$NON-NLS-2$
        addMarker(stringBuilder, "camera-icon", "http://maps.google.com/mapfiles/kml/pal4/icon38.png", 16, 16); //$NON-NLS-1$ //$NON-NLS-2$
        addMarker(stringBuilder, "info-icon", "http://maps.google.com/mapfiles/kml/pal3/icon35.png", 16, 16); //$NON-NLS-1$ //$NON-NLS-2$

        for( KmlRepresenter kmlRepresenter : kmlRepresenters ) {
            try {
                stringBuilder.append(kmlRepresenter.toKmlString());
            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        }
        stringBuilder.append("</Document>\n"); //$NON-NLS-1$
        stringBuilder.append("</kml>\n"); //$NON-NLS-1$

        byte[] kmlBytes = stringBuilder.toString().getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$

        /*
         * start adding the kml part
         */
        FileOutputStream fos = new FileOutputStream(outputFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        CRC32 crc = new CRC32();
        String kmlName = "kml.kml"; //$NON-NLS-1$
        crc.update(kmlBytes);
        ZipEntry entry = new ZipEntry(kmlName);
        entry.setMethod(ZipEntry.STORED);
        entry.setCompressedSize(kmlBytes.length);
        entry.setSize(kmlBytes.length);
        entry.setCrc(crc.getValue());
        zos.putNextEntry(entry);
        zos.write(kmlBytes);

        /*
         * now add all images
         */
        TreeSet<String> addedImages = new TreeSet<String>();
        for( KmlRepresenter kmlRepresenter : kmlRepresenters ) {
            if (kmlRepresenter.hasImages()) {
                List<String> imageIds = kmlRepresenter.getImageIds();
                for( String imageId : imageIds ) {
                    long id = Long.parseLong(imageId);
                    String imageName = getImageNameById(id);

                    if (!addedImages.add(imageName)) {
                        // don't add double images
                        continue;
                    }
                    byte[] imageData = getImageDataById(id);

                    crc.reset();
                    crc.update(imageData);
                    ZipEntry imageEntry = new ZipEntry(imageName);
                    imageEntry.setMethod(ZipEntry.STORED);
                    imageEntry.setCompressedSize(imageData.length);
                    imageEntry.setSize(imageData.length);
                    imageEntry.setCrc(crc.getValue());
                    zos.putNextEntry(imageEntry);
                    zos.write(imageData);
                }
            }
        }
        zos.close();
    }

    protected abstract String getImageNameById( long id );

    protected abstract byte[] getImageDataById( long id );

    private static void addMarker( StringBuilder sb, String alias, String url, int x, int y ) throws IOException {
        sb.append("<Style id=\"" + alias + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("<IconStyle>\n"); //$NON-NLS-1$
        sb.append("<scale>1.1</scale>\n"); //$NON-NLS-1$
        sb.append("<Icon>\n"); //$NON-NLS-1$
        sb.append("<href>" + url + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("</href>\n"); //$NON-NLS-1$
        sb.append("</Icon>\n"); //$NON-NLS-1$
        sb.append("<hotSpot x=\"" + x + "\" y=\"" + y + "\" xunits=\"pixels\" yunits=\"pixels\" />\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        sb.append("</IconStyle>\n"); //$NON-NLS-1$
        sb.append("<ListStyle>\n"); //$NON-NLS-1$
        sb.append("</ListStyle>\n"); //$NON-NLS-1$
        sb.append("</Style>\n"); //$NON-NLS-1$
    }
}
