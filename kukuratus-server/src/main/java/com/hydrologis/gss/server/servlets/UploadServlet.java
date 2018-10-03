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
package com.hydrologis.gss.server.servlets;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.GpsLogsData;
import com.hydrologis.gss.server.database.objects.GpsLogsProperties;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gssmobile.database.GssGpsLog;
import com.hydrologis.gssmobile.database.GssGpsLogPoint;
import com.hydrologis.gssmobile.database.GssImage;
import com.hydrologis.gssmobile.database.GssNote;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.servlets.Status;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

@WebServlet(urlPatterns = "/upload")
//@MultipartConfig(fileSizeThreshold = 2 * 1024 * 1024 * 100, // 20 MB
//        maxFileSize = 1024 * 1024 * 150, // 50 MB
//        maxRequestSize = 1024 * 1024 * 200) // 100 MB
public class UploadServlet extends HttpServlet {
    private static final String TAG = UploadServlet.class.getSimpleName();
    private static final String NO_PERMISSION = "No permission! Contact your system administrator.";
    private static final long serialVersionUID = 1L;
    private static boolean DEBUG = true;

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 10);

        Logger logDb = KukuratusWorkspace.getInstance().getLogDb();
        String ipAddress = "unknown";
        String deviceId = "unknown";
        try {
            if ((deviceId = ServletUtils.canProceed(request, response)) == null) {
                return;
            }

            DatabaseHandler dbHandler = SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get();
            GeometryFactory gf = GeometryUtilities.gf();
            Dao<GpapUsers, ? > usersDao = dbHandler.getDao(GpapUsers.class);
            GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId).queryForFirst();

            Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);

            Dao<GpsLogs, ? > logsDao = dbHandler.getDao(GpsLogs.class);
            Dao<GpsLogsData, ? > logsDataDao = dbHandler.getDao(GpsLogsData.class);
            Dao<GpsLogsProperties, ? > logsPropsDao = dbHandler.getDao(GpsLogsProperties.class);

            Dao<ImageData, ? > imageDataDao = dbHandler.getDao(ImageData.class);
            Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);

            int[] notesLogsImagesCounts = new int[]{0, 0, 0};

            dbHandler.callInTransaction(() -> {
//                    Collection<Part> parts = request.getParts();
//                    Part data = parts.iterator().next();
//                    String partName = data.getName();
//                    try (InputStream is = data.getInputStream()) {
//                        // store or do something with the input stream
//                    }

                ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());

                HashMap<Long, Long> deviceNoteId2serverNoteId = new HashMap<>();
                List<FileItem> items = sfu.parseRequest(request);
                /*
                 * first handle notes, since images need to be placed connected
                 */
                for( FileItem item : items ) {
                    String name = item.getName();
                    if (name.equals(GssNote.OBJID)) {
                        DataInputStream dis = new DataInputStream(item.getInputStream());
                        while( dis.available() > 0 ) {
                            GssNote note = new GssNote();
                            note.internalize(GssNote.VERSION, dis);

                            Point point = gf.createPoint(new Coordinate(note.longitude, note.latitude));
                            Notes serverNote = new Notes(point, note.altitude, note.timeStamp, note.description, note.text,
                                    note.form, note.style, gpapUser);

                            notesDao.create(serverNote);
                            deviceNoteId2serverNoteId.put(note.id, serverNote.id);
                            notesLogsImagesCounts[0] += 1;
                            debug("Uploaded note: " + serverNote.text);
                        }
                    } else if (name.equals(GssGpsLog.OBJID)) {
                        DataInputStream dis = new DataInputStream(item.getInputStream());

                        List<GpsLogs> logs = new ArrayList<>();
                        List<GpsLogsData> logsData = new ArrayList<>();
                        List<GpsLogsProperties> logsProps = new ArrayList<>();
                        while( dis.available() > 0 ) {
                            GssGpsLog gpsLog = new GssGpsLog();
                            gpsLog.internalize(GssGpsLog.VERSION, dis);

                            List<Coordinate> logCoordinates = gpsLog.points.stream()
                                    .map(gp -> new Coordinate(gp.longitude, gp.latitude)).collect(Collectors.toList());
                            LineString logLine = gf
                                    .createLineString(logCoordinates.toArray(new Coordinate[logCoordinates.size()]));
                            GpsLogs newLog = new GpsLogs(gpsLog.name, gpsLog.startts, gpsLog.endts, logLine, gpapUser);
                            logs.add(newLog);

                            for( GssGpsLogPoint gpsPoint : gpsLog.points ) {
                                Coordinate c = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
                                Point point = gf.createPoint(c);
                                GpsLogsData gpsLogsData = new GpsLogsData(point, gpsPoint.altimetry, gpsPoint.ts, newLog);
                                logsData.add(gpsLogsData);
                            }

                            GpsLogsProperties prop = new GpsLogsProperties(gpsLog.color, gpsLog.width, newLog);
                            logsProps.add(prop);
                            notesLogsImagesCounts[1] += 1;
                        }
                        logsDao.create(logs);
                        logsDataDao.create(logsData);
                        logsPropsDao.create(logsProps);
                        debug("Uploaded Logs: " + logs.size());

                    }
                }

                /*
                 * now handle images
                 */
                for( FileItem item : items ) {
                    String name = item.getName();
                    if (name.startsWith(GssImage.OBJID)) {
                        DataInputStream dis = new DataInputStream(item.getInputStream());
                        GssImage image = new GssImage();
                        image.internalize(GssImage.VERSION, dis);

                        ImageData imgData = new ImageData(image.dataThumb, image.data);
                        imageDataDao.create(imgData);

                        Notes tmpNote = null;
                        if (image.noteId != -1) {
                            Long noteId = deviceNoteId2serverNoteId.get(image.noteId);
                            tmpNote = new Notes(noteId);
                        }
                        Point point = gf.createPoint(new Coordinate(image.longitude, image.latitude));
                        Images img = new Images(point, image.altitude, image.timeStamp, image.azimuth, image.text, tmpNote,
                                imgData, gpapUser);
                        imagesDao.create(img);
                        notesLogsImagesCounts[2] += 1;

                        String str = "";
                        if (tmpNote != null) {
                            str = " for note: " + tmpNote.id;
                        }
                        debug("Uploaded image: " + image.text + str);
                    }
                }

                return null;
            });

            logDb.insert(EMessageType.ACCESS, TAG,
                    "Upload connection from '" + deviceId + "' at ip:" + ipAddress + " completed properly.");
            StringBuilder sb = new StringBuilder();
            sb.append("Data properly inserted in the server.");
            sb.append("\nNotes: " + notesLogsImagesCounts[0] + "\n");
            sb.append("Gps Logs: " + notesLogsImagesCounts[1] + "\n");
            sb.append("Images: " + notesLogsImagesCounts[2]);

            String message = sb.toString();
            debug("SENDING RESPONSE MESSAGE: " + message);
            Status okStatus = new Status(Status.CODE_200_OK, message);
            okStatus.sendTo(response);
        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Upload connection from '" + deviceId + "' at ip:" + ipAddress + " errored with:\n", ex);
                /*
                 * if there are problems, return some information.
                 */
                String msg = "An error occurred while uploading data to the server.";
                Status errStatus = new Status(Status.CODE_500_INTERNAL_SERVER_ERROR, msg, ex);
                errStatus.sendTo(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void debug( String msg ) {
        if (DEBUG) {
            KukuratusLogger.logDebug(this, msg);
        }
    }

}
