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
package com.hydrologis.gss.servlets;

import java.io.IOException;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gasleaksensors.databases.v2.core.handlers.DatabaseHandler;
import com.gasleaksensors.databases.v2.core.objects.Events;

import eu.hydrologis.stage.libs.providers.tilegenerators.SpatialDbTilesGenerator;

/**
 * Tiles provider servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class EventsTilesGeneratorServlet extends DbTilesGeneratorServlet {

    private static final long serialVersionUID = 1L;

    private static TreeMap<String, SpatialDbTilesGenerator> tilesGeneratorMap = new TreeMap<>();
    static final String whereStr = Events.EVENTCODE_FIELD_NAME + "=21";
    static final String tableName = DatabaseHandler.getTableName(Events.class);

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        handleRequest(tilesGeneratorMap, tableName, whereStr, request, response);
    }

    public static void refreshData( int lastAvailableYear ) {
        refreshTileData(lastAvailableYear, tilesGeneratorMap);
    }

}