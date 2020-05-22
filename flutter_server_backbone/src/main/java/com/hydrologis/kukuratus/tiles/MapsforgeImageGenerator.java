/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.kukuratus.tiles;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Class to generate map images from mapsforge.
 * 
 * @author AndreaAntonello
 */
public class MapsforgeImageGenerator {

    private static final Object INTERPOLATION_MODE = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    private static BufferedImage dummyTile;
    private MapsforgeTilesGenerator generator;

    public MapsforgeImageGenerator( File[] mapsforgeFiles, Integer tileSize, Float scaleFactor ) throws Exception {
        generator = new MapsforgeTilesGenerator("dummy", mapsforgeFiles, tileSize, scaleFactor, null); //$NON-NLS-1$
        makeDummyTile();
    }

    public MapsforgeImageGenerator( MapsforgeTilesGenerator generator ) throws Exception {
        this.generator = generator;
        makeDummyTile();
    }

    public BufferedImage getMapAreaImage( Envelope requestedEnvelope, int zoomLevel, boolean drawDebug ) throws Exception {
        return getMapAreaImage(requestedEnvelope, zoomLevel, 0, 0, drawDebug);
    }

    /**
     * Draw tiles rotated onto an image.
     * 
     * @param tileCacheFolder
     * @param requestedEnvelope
     *            the world bounds requested.
     * @param zoomLevel
     *            the zoomlevel at which it should be drawn.
     * @param bufferTilesToAdd
     *            optional tiles to add around to cover wholes.
     * @param tileSize
     *            the size of the tiles (should be 256).
     * @param rotationAngle
     *            the rotation angle to apply.
     * @param mapFile
     *            the map file to use.
     * @param renderer
     *            mapsforge renderer to use.
     * @param xmlRenderTheme
     *            the renderer theme to use.
     * @param displayModel
     *            mapsforge displaymode.
     * @param drawDebug
     *            if <code>true</code>, debug frames are drawn.
     * @return the created image.
     * @throws Exception
     */
    public BufferedImage getMapAreaImage( Envelope requestedEnvelope, int zoomLevel, //
            int bufferTilesToAdd, double rotationAngle, boolean drawDebug ) throws Exception {
        int tileSize = generator.getTileSize();
        if (rotationAngle == 0)
            bufferTilesToAdd = 0;

        Coordinate envCenter = requestedEnvelope.centre();
        double requestedCenterLon = envCenter.x;
        double requestedCenterLat = envCenter.y;
        double requestedMinX = requestedEnvelope.getMinX();
        double requestedMaxX = requestedEnvelope.getMaxX();
        double requestedMinY = requestedEnvelope.getMinY();
        double requestedMaxY = requestedEnvelope.getMaxY();
        double requestedWidth = requestedMaxX - requestedMinX;
        double requestedHeight = requestedMaxY - requestedMinY;

        int[] tileLL = getTileNumber(requestedMinY, requestedMinX, zoomLevel);
        int[] tileUR = getTileNumber(requestedMaxY, requestedMaxX, zoomLevel);
        int originalStartXTile = tileLL[0];
        int originalStartYTile = tileUR[1];
        int originalEndXTile = tileUR[0];
        int originalEndYTile = tileLL[1];

        int startXTile = originalStartXTile - bufferTilesToAdd;
        int startYTile = originalStartYTile - bufferTilesToAdd;
        int endXTile = originalEndXTile + bufferTilesToAdd;
        int endYTile = originalEndYTile + bufferTilesToAdd;

        // check the drawn region
        double[] startNSWE = tile2boundingBox(startXTile, startYTile, zoomLevel);
        double[] endNSWE = tile2boundingBox(endXTile, endYTile, zoomLevel);
        double drawnMinX = min(startNSWE[2], endNSWE[2]);
        double drawnMaxX = max(startNSWE[3], endNSWE[3]);
        double drawnMinY = min(startNSWE[1], endNSWE[1]);
        double drawnMaxY = max(startNSWE[0], endNSWE[0]);
        double drawnWidth = drawnMaxX - drawnMinX;
        double drawnHeight = drawnMaxY - drawnMinY;

        double centerPercentX = (requestedCenterLon - drawnMinX) / drawnWidth;
        double centerPercentY = (drawnMaxY - requestedCenterLat) / drawnHeight;

        double scalefactorX = requestedWidth / drawnWidth;
        double scalefactorY = requestedHeight / drawnHeight;

        double tileDeltaMinX = requestedMinX - drawnMinX;
        double tileDeltaMaxY = drawnMaxY - requestedMaxY;

        int yTiles = endYTile - startYTile + 1;
        int xTiles = endXTile - startXTile + 1;
        int drawnImageWidth = xTiles * tileSize;
        int drawnImageHeight = yTiles * tileSize;

        int requestedImageWidth = (int) (drawnImageWidth * scalefactorX);
        int requestedImageHeight = (int) (drawnImageHeight * scalefactorY);
        int requestedImageShiftX = (int) (drawnImageWidth * tileDeltaMinX / drawnWidth);
        int requestedImageShiftY = (int) (drawnImageHeight * tileDeltaMaxY / drawnHeight);

        BufferedImage bufferImage = new BufferedImage(drawnImageWidth, drawnImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D bufferGraphics = (Graphics2D) bufferImage.getGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION_MODE);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (rotationAngle != 0)
            bufferGraphics.rotate(Math.toRadians(rotationAngle), drawnImageWidth * centerPercentX,
                    drawnImageHeight * centerPercentY);

        // long t1 = System.currentTimeMillis();
        int x = 0;
        int y = 0;
        int firstY = 0;
        // if (GisConstants.GIS_DEBUG) {
        // System.out.println("**********************************");
        // System.out.println("Tiles in imagemap: ");
        // for (String d : imageMap.keySet()) {
        // System.out.println("\t " + d);
        // }
        // System.out.println("looking for: " + tileString);
        // }
        for( int xTile = startXTile; xTile <= endXTile; xTile++ ) {
            for( int yTile = startYTile; yTile <= endYTile; yTile++ ) {
                BufferedImage tileImage = generator.getTileImage(xTile, yTile, zoomLevel);
                if (tileImage != null) {
                    bufferGraphics.drawImage(tileImage, null, x, y);
                } else {
                    bufferGraphics.drawImage(dummyTile, null, x, y);
                }
                if (drawDebug) {
                    bufferGraphics.setColor(Color.black);
                    bufferGraphics.drawRect(x, y, tileSize, tileSize);
                    bufferGraphics.drawString(xTile + "/" + yTile, x + 10, y + 20); //$NON-NLS-1$
                }
                y = y + tileSize;
            }

            x = x + tileSize;
            y = firstY;
        }
        // if (GisConstants.GIS_DEBUG) {
        // System.out.println("Tiles to draw: " + count);
        // System.out.println("of which new created: " + countNew);
        // System.out.println("imageMap size: " + imageMap.size());
        // }

        // long t2 = System.currentTimeMillis();
        // System.out.println("TTT: " + (t2 - t1));

        // if (GisConstants.GIS_DEBUG) {
        // System.out.println(requestedImageShiftX + "/" + requestedImageShiftY
        // + "/" + requestedImageWidth + "/" + requestedImageHeight);
        // }
        try {
            bufferImage = bufferImage.getSubimage(requestedImageShiftX, requestedImageShiftY, requestedImageWidth,
                    requestedImageHeight);
        } catch (Exception e) {
            bufferImage = dummyTile;
        }

        return bufferImage;
    }

    private static void makeDummyTile() {
        dummyTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) dummyTile.getGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, 256, 256);
        g2d.dispose();
    }

    public static int[] getTileNumber( final double lat, final double lon, final int zoom ) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor(
                (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        return new int[]{xtile, ytile};
    }

    /**
     * Convert a tile number at a given zoom to its lat/lon bounds.
     * 
     * @param x
     *            tile x.
     * @param y
     *            tile y.
     * @param zoom
     *            the zoom level.
     * @return the bounds as [n,s,w,e].
     */
    public static double[] tile2boundingBox( final int x, final int y, final int zoom ) {
        double[] nswe = new double[4];
        nswe[0] = tile2lat(y, zoom);
        nswe[1] = tile2lat(y + 1, zoom);
        nswe[2] = tile2lon(x, zoom);
        nswe[3] = tile2lon(x + 1, zoom);
        return nswe;
    }

    private static double tile2lon( int x, int z ) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    private static double tile2lat( int y, int z ) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    // public static void main( String[] args ) throws Exception {
    //     File[] mapFiles = new File[]{new File("path.map")}; //$NON-NLS-1$
    //     // 15/46.63192/11.14469
    //     double y = 46.63192;
    //     double x = 11.14469;
    //     double dx = 0.002;
    //     double dy = 0.001;

    //     Envelope env = new Envelope(x - dx, x + dx, y - dy, y + dy);
    //     MapsforgeImageGenerator gen = new MapsforgeImageGenerator(mapFiles, 256, 1f);
    //     BufferedImage img = gen.getMapAreaImage(env, 21, false);
    //     ImageIO.write(img, "png", new File("image.png")); //$NON-NLS-1$ //$NON-NLS-2$
    // }
}
