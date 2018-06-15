package com.hydrologis.gss.map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.geotools.geojson.geom.GeometryJSON;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.entrypoints.MapviewerEntryPoint;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.map.LeafletMapBrowser;
import eu.hydrologis.stage.libs.utils.ResourcesHandler;

/**
 * A browser widget dedicated to map interactions.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GssMapBrowser extends LeafletMapBrowser implements ProgressListener {

    private static final long serialVersionUID = 1L;

    private String gpspointsStyle;
    private String notesIconJson;
    private String imagesIconJson;

    public GssMapBrowser( Composite parent, int style ) throws Exception {
        super(parent, style);

        StringBuilder sb1 = new StringBuilder();
        sb1.append("{ ");
        sb1.append(q("color") + ": ").append(q("red")).append(",");
        sb1.append(q("opacity") + ": ").append(q("1")).append(",");
        sb1.append(q("weight") + ": ").append(q("1")).append(",");
        sb1.append(q("fillColor") + ": ").append(q("black")).append(",");
        sb1.append(q("fillOpacity") + ": ").append(q("1")).append(",");
        sb1.append(q("radius") + ": ").append(q(7));
        sb1.append(" }");
        gpspointsStyle = sb1.toString();

        String databasePath = GssContext.instance().getDbProvider().getDb().getDatabasePath();
        File dbFile = new File(databasePath);

        String notesTableName = DatabaseHandler.getTableName(Notes.class);
        File notesIconFile = new File(dbFile.getParentFile(), notesTableName + ".png");
        if (notesIconFile.exists()) {
            String resourceName = ResourcesHandler.registerFileByName(notesIconFile);
            BufferedImage iconImage = ImageIO.read(notesIconFile);
            int width = iconImage.getWidth();
            int height = iconImage.getHeight();
            String resourceUrl = ResourcesHandler.getImageUrl(resourceName);
            StringBuffer sb = new StringBuffer();
            sb.append("{ \"iconUrl\": \"").append(resourceUrl).append("\", \"iconSize\": [").append(width).append(",")
                    .append(height).append("]}");
            notesIconJson = sb.toString();
        }
        String imagesTableName = DatabaseHandler.getTableName(Images.class);
        File imagesIconFile = new File(dbFile.getParentFile(), imagesTableName + ".png");
        if (imagesIconFile.exists()) {
            String resourceName = ResourcesHandler.registerFileByName(imagesIconFile);
            BufferedImage iconImage = ImageIO.read(imagesIconFile);
            int width = iconImage.getWidth();
            int height = iconImage.getHeight();
            String resourceUrl = ResourcesHandler.getImageUrl(resourceName);
            StringBuffer sb = new StringBuffer();
            sb.append("{ \"iconUrl\": \"").append(resourceUrl).append("\", \"iconSize\": [").append(width).append(",")
                    .append(height).append("]}");
            imagesIconJson = sb.toString();
        }

        setExtraBlock(true);
    }

    public void addBrowserFunctions() {
        super.addBrowserFunctions();
        new BrowserFunction(this, "getInitialBounds"){
            @Override
            public Object function( Object[] arguments ) {
                double centerX = 12.4853;
                double centerY = 41.8685;

                double delta = 0.01;
                double west = centerX - delta;
                double south = centerY - delta;
                double east = centerX + delta;
                double north = centerY + delta;
                Double[] bounds = {west, south, east, north};
                return bounds;
            }
        };
        new BrowserFunction(this, "getDataStyle"){
            @Override
            public Object function( Object[] arguments ) {
                if (arguments != null) {
                    // called only for gpspoints
                    return gpspointsStyle;
                }
                return null;
            }

        };
        new BrowserFunction(this, "getDataIcon"){
            @Override
            public Object function( Object[] arguments ) {
                if (arguments != null) {
                    String layerName = arguments[0].toString();
                    if (layerName.startsWith(MapviewerEntryPoint.NOTES)) {
                        return notesIconJson;
                    } else if (layerName.startsWith(MapviewerEntryPoint.IMAGES)) {
                        return imagesIconJson;
                    }
                    return null;
                }
                return null;
            }

        };
    }

    public String getLegendCmd( String legendName, String[] colorsArray, String[] labelsArray, String legendPosition ) {
        for( int i = 0; i < labelsArray.length; i++ ) {
            labelsArray[i] = q(labelsArray[i]);
            colorsArray[i] = q(colorsArray[i]);
        }
        String colors = Arrays.toString(colorsArray);
        String labels = Arrays.toString(labelsArray);
        String legendCmd = "addLegend('" + legendName + "', " + colors + ", " + labels + ", '" + legendPosition + "')";
        return legendCmd;
    }

    public String getZoomToGeometryAndHighlight( Geometry geometry ) {
        try {
            Envelope env = geometry.getEnvelopeInternal();
            if (env.getWidth() == 0) {
                env.expandBy(0.001);
            }
            String script = "zoomToBounds(" + env.getMinX() + "," + env.getMaxX() + "," + env.getMinY() + "," + env.getMaxY()
                    + ");";
            GeometryJSON gjson = new GeometryJSON(7);
            StringWriter writer = new StringWriter();
            gjson.write(geometry, writer);
            String geojson = writer.toString();
            String script2 = "highlightGeometry('" + geojson + "'," + 2000 + ");";
            script += script2;
            return script;
        } catch (Exception e) {
            StageLogger.logError(this, e);
        }
        return "";
    }

}
