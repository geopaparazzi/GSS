package com.hydrologis.gss.servlets;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.server.database.DatabaseHandler;
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
import com.j256.ormlite.dao.Dao;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.stage.libs.servlets.Status;
import eu.hydrologis.stage.libs.utils.NetworkUtilities;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * Download servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class UploadServlet extends HttpServlet {
    private static final String TAG = UploadServlet.class.getSimpleName();
    private static final String NO_PERMISSION = "No permission! Contact your system administrator.";
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        Logger logDb = StageWorkspace.getInstance().getLogDb();
        String ipAddress = "unknown";
        String deviceId = "unknown";
        try {
            String authHeader = request.getHeader("Authorization");
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            if (userPwd == null || !userPwd[1].equals("gss_Master_Survey_Forever_2018")) {
                throw new ServletException(NO_PERMISSION);
            }
            deviceId = userPwd[0];

            DatabaseHandler dbHandler = GssContext.instance().getDbProvider().getDatabaseHandler();
            Dao<GpapUsers, ? > usersDao = dbHandler.getDao(GpapUsers.class);
            GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId).queryForFirst();
            if (gpapUser == null) {
                // create one for the new device
                gpapUser = new GpapUsers(deviceId, deviceId, null, null, null);
                usersDao.create(gpapUser);
            }
            GeometryFactory gf = GeometryUtilities.gf();
            GpapUsers _gpapUser = gpapUser;

            Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);

            Dao<GpsLogs, ? > logsDao = dbHandler.getDao(GpsLogs.class);
            Dao<GpsLogsData, ? > logsDataDao = dbHandler.getDao(GpsLogsData.class);
            Dao<GpsLogsProperties, ? > logsPropsDao = dbHandler.getDao(GpsLogsProperties.class);

            Dao<ImageData, ? > imageDataDao = dbHandler.getDao(ImageData.class);
            Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);

            int[] notesLogsImagesCounts = new int[]{0, 0, 0};

            dbHandler.callInTransaction(new Callable<Void>(){
                @Override
                public Void call() throws Exception {
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
                            List<Notes> serverNotes = new ArrayList<>();
                            while( dis.available() > 0 ) {
                                GssNote note = new GssNote();
                                note.internalize(GssNote.VERSION, dis);

                                Point point = gf.createPoint(new Coordinate(note.longitude, note.latitude));
                                Notes serverNote = new Notes(point, note.altitude, note.timeStamp, note.description, note.text,
                                        note.form, note.style, _gpapUser);
                                deviceNoteId2serverNoteId.put(note.id, serverNote.id);
                                serverNotes.add(serverNote);
                                notesLogsImagesCounts[0] += 1;
                            }
                            notesDao.create(serverNotes);
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
                                GpsLogs newLog = new GpsLogs(gpsLog.name, gpsLog.startts, gpsLog.endts, logLine, _gpapUser);
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
                                    imgData, _gpapUser);
                            imagesDao.create(img);
                            notesLogsImagesCounts[2] += 1;
                        }
                    }

                    return null;
                }
            });

            logDb.insert(EMessageType.ACCESS, TAG,
                    "Upload connection from '" + deviceId + "' at ip:" + ipAddress + " completed properly.");
            StringBuilder sb = new StringBuilder();
            sb.append("Data properly inserted in the server.\n");
            sb.append("Notes: " + notesLogsImagesCounts[0] + "\n");
            sb.append("Gps Logs: " + notesLogsImagesCounts[1] + "\n");
            sb.append("Images: " + notesLogsImagesCounts[2]);

            Status okStatus = new Status(Status.CODE_200_OK, sb.toString());
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

}