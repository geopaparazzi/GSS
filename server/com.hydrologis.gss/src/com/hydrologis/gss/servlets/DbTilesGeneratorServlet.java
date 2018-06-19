package com.hydrologis.gss.servlets;

import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.styling.Style;
import org.hortonmachine.dbs.compat.ASpatialDb;

import com.gasleaksensors.libsV2.IDbProvider;
import com.gasleaksensors.libsV2.SensitContextV2;

import eu.hydrologis.stage.libs.providers.tilegenerators.ITilesObject;
import eu.hydrologis.stage.libs.providers.tilegenerators.SpatialDbTilesGenerator;
import eu.hydrologis.stage.libs.providers.tilegenerators.SpatialDbTilesGeneratorBuilder;

public abstract class DbTilesGeneratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected static void refreshTileData( int lastAvailableYear, TreeMap<String, SpatialDbTilesGenerator> tilesGeneratorMap ) {
        SpatialDbTilesGenerator tilesGenerator = tilesGeneratorMap.get(String.valueOf(lastAvailableYear));
        if (tilesGenerator != null) {
            tilesGenerator.refreshData();
        }
    }

    protected void handleRequest( TreeMap<String, SpatialDbTilesGenerator> tilesGeneratorMap, String tableName, String whereStr,
            HttpServletRequest request, HttpServletResponse response ) {
        try {
            String yearStr = request.getParameter("year");
            SpatialDbTilesGenerator tilesGenerator = tilesGeneratorMap.get(yearStr);
            if (tilesGenerator == null) {
                synchronized (tilesGeneratorMap) {
                    tilesGenerator = tilesGeneratorMap.get(yearStr);
                    if (tilesGenerator == null) {
                        // System.out.println("Make new generator for " + tableName + " for year " +
                        // yearStr + " in map size: "
                        // + Arrays.toString(tilesGeneratorMap.keySet().toArray(new String[0])));
                        tilesGenerator = makeGenerator(tilesGeneratorMap, yearStr, tableName, whereStr);
                    }
                }
            }

            int xTile = Integer.parseInt(request.getParameter(ITilesObject.X));
            int yTile = Integer.parseInt(request.getParameter(ITilesObject.Y));
            int zoom = Integer.parseInt(request.getParameter(ITilesObject.Z));
            ServletOutputStream outputStream = response.getOutputStream();
            tilesGenerator.getTile(xTile, yTile, zoom, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            // ignore
        }
    }

    private synchronized SpatialDbTilesGenerator makeGenerator( TreeMap<String, SpatialDbTilesGenerator> tilesGeneratorMap,
            String yearStr, String tableName, String whereStr ) throws Exception {
        int year = Integer.parseInt(yearStr);
        IDbProvider dbProvider = GssContext.instance().getDbForYear(year);
        ASpatialDb db = dbProvider.getDb();

        SpatialDbTilesGeneratorBuilder builder = SpatialDbTilesGeneratorBuilder.newBuilder(db, tableName);
        SpatialDbTilesGenerator tilesGenerator;
        if (this instanceof WorkTilesGeneratorServlet) {
            tilesGenerator = builder.doOnlyGeometries().wrapGeometry("ST_LineMerge(ST_Collect(", "))").where(whereStr)
                    .memoryCacheSize(1000).build();
        } else if (this instanceof GpspointsTilesGeneratorServlet) {
            Style gpsPointsStyle = GpsPointsStyleUtil.createDefaultPointStyle();
            tilesGenerator = builder.doOnlyGeometries().style(gpsPointsStyle).memoryCacheSize(1000).build();
        } else {
            tilesGenerator = builder.where(whereStr).memoryCacheSize(1000).build();
        }
        
        tilesGeneratorMap.put(yearStr, tilesGenerator);
        return tilesGenerator;
    }

    public DbTilesGeneratorServlet() {
        super();
    }

}