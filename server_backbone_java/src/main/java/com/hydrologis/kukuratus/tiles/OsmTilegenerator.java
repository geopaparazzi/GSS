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
package com.hydrologis.kukuratus.tiles;

import java.awt.image.BufferedImage;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.graphics.AwtTileBitmap;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;

/**
 * Image tiles generator for mapsforge maps.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 *
 */
public class OsmTilegenerator {

    private DatabaseRenderer renderer;
    private RenderThemeFuture theme;
    private DisplayModel model;
    private MapDataStore mapDatabase;
    private int tileSize;

    public OsmTilegenerator( MapDataStore mapDatabase, final DatabaseRenderer renderer, final RenderThemeFuture renderTheme,
            final DisplayModel displayModel, int tileSize ) {
        this.mapDatabase = mapDatabase;
        this.renderer = renderer;
        this.theme = renderTheme;
        this.model = displayModel;
        this.tileSize = tileSize;
    }

    public synchronized BufferedImage getImage( final int zoomLevel, final int xTile, final int yTile ) {
        try {
            Tile tile = new Tile(xTile, yTile, (byte) zoomLevel, tileSize);
            // displayModel.setFixedTileSize(tileSize);
            // Draw the tile
            float userScaleFactor = model.getUserScaleFactor();
            RendererJob mapGeneratorJob = new RendererJob(tile, mapDatabase, theme, model, userScaleFactor, false, false);
            AwtTileBitmap bmp = (AwtTileBitmap) renderer.executeJob(mapGeneratorJob);
            if (bmp != null) {
                BufferedImage bitmap = AwtGraphicFactory.getBitmap(bmp);
                return bitmap;
            }
        } catch (Exception e) {
            // will try again later

            // e.printStackTrace();
//            System.err.println(
//                    "Not rendering tile: " + zoomLevel + "/" + xTile + "/" + yTile + "  (" + e.getLocalizedMessage() + ")");
        }
        return null;
    }

}
