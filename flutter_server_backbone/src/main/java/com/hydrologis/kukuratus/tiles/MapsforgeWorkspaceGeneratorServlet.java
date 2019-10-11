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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;

@WebServlet(urlPatterns = "/mapsforge")
public class MapsforgeWorkspaceGeneratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static ITilesGenerator tilesGenerator;

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        try {
            int xTile = Integer.parseInt(request.getParameter(ITilesObject.X));
            int yTile = Integer.parseInt(request.getParameter(ITilesObject.Y));
            int zoom = Integer.parseInt(request.getParameter(ITilesObject.Z));

            ITilesGenerator tilesGenerator = getGenerator();
            if (tilesGenerator == null) {
                synchronized (MapsforgeWorkspaceGeneratorServlet.class) {
                    if (tilesGenerator == null) {
                        tilesGenerator = makeGenerator();
                    }
                }
            }
            ServletOutputStream outputStream = response.getOutputStream();
            tilesGenerator.getTile(xTile, yTile, zoom, outputStream);

            try {
                outputStream.flush();
            } catch (Exception e) {
                if (!e.getMessage().contains("Broken pipe")) { //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected ITilesGenerator makeGenerator() {
        MapsforgeTilesGenerator generator = null;
        try {
            File dataFolder = KukuratusWorkspace.getInstance().getDataFolder();
            File[] mapfiles = dataFolder.listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return name.endsWith(".map"); //$NON-NLS-1$
                }
            });

            float factor = 1f;
            int tile = 256;
            generator = new MapsforgeTilesGenerator("mapsforge", mapfiles, tile, factor, null); //$NON-NLS-1$
        } catch (Exception e) {
            e.printStackTrace();
        }
        tilesGenerator = generator;
        return tilesGenerator;
    }

    protected ITilesGenerator getGenerator() {
        return tilesGenerator;
    }

}
