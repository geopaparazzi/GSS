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
/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
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
 */
package com.hydrologis.kukuratus.tiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Tiles producer interface.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public interface ITilesProducer {
    final int DEFAULT_TILESIZE = 256;

    public default LoadingCache<String, BufferedImage> getImageCache( int maxSize ) {
        LoadingCache<String, BufferedImage> imageCache = CacheBuilder.newBuilder().initialCapacity(maxSize).maximumSize(maxSize)
                .build(new CacheLoader<String, BufferedImage>(){
                    public BufferedImage load( String tileDef ) throws Exception {
                        return createImageForTile(tileDef);
                    }
                });
        return imageCache;
    }

    public BufferedImage createImageForTile( String tileDef ) throws Exception;

    public static double tile2lon( int x, int z ) {
        return x / Math.pow(2.0, z) * 360.0 - 180.0;
    }

    public static double tile2lat( int y, int z ) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    public String getTileDef( int... params );

    public int[] fromTileDef( String tileDef );

    public int getTileSize();

    public static BufferedImage createEmptyImage(int tileSize) {
        Rectangle imageBounds = new Rectangle(0, 0, tileSize, tileSize);
        int imgType = BufferedImage.TYPE_INT_ARGB;
        Color backgroundColor = new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 0);
        BufferedImage emptyImage = new BufferedImage(imageBounds.width, imageBounds.height, imgType);
        Graphics2D gr = emptyImage.createGraphics();
        gr.setPaint(backgroundColor);
        gr.fill(imageBounds);
        gr.dispose();
        return emptyImage;
    }
}
