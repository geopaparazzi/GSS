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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.locationtech.jts.geom.GeometryFactory;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.GpsLogsData;
import com.hydrologis.gss.server.database.objects.GpsLogsProperties;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.servlets.KukuratusStatus;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;

@WebServlet(urlPatterns = "/upload")
@MultipartConfig(fileSizeThreshold = ServletUtils.MAX_UPLOAD_SIZE, maxFileSize = ServletUtils.MAX_UPLOAD_SIZE
        * 2, maxRequestSize = ServletUtils.MAX_UPLOAD_SIZE / 2) // 100 MB
public class UploadServlet extends HttpServlet {
    private static final String TAG = UploadServlet.class.getSimpleName();
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 10);

        Logger logDb = KukuratusWorkspace.getInstance().getLogDb();
        String deviceId = "unknown"; //$NON-NLS-1$
        try {
            if ((deviceId = ServletUtils.canProceed(request, response, "sync")) == null) { //$NON-NLS-1$
                return;
            }
            DatabaseHandler dbHandler = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get();
            GeometryFactory gf = GeometryUtilities.gf();
            Dao<GpapUsers, ? > usersDao = dbHandler.getDao(GpapUsers.class);
            GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId).queryForFirst();

            Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);

            Dao<GpsLogs, ? > logsDao = dbHandler.getDao(GpsLogs.class);
            Dao<GpsLogsData, ? > logsDataDao = dbHandler.getDao(GpsLogsData.class);
            Dao<GpsLogsProperties, ? > logsPropsDao = dbHandler.getDao(GpsLogsProperties.class);

            Dao<ImageData, ? > imageDataDao = dbHandler.getDao(ImageData.class);
            Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);

            String sessionId = session.getId();
            Collection<Part> parts = request.getParts();
            for( Part part : parts ) {
                String filename = getFilename(part);
                if (filename == null) {
                    String value = getValue(part);
                    String partName = part.getName();
                    System.out.println(sessionId + ": " + partName + ": " + value);
                } else {
                    System.out.println(sessionId + ": file: " + filename);
                }

//                try (InputStream is = part.getInputStream()) {
//                    // store or do something with the input stream
//                }
            }

            int[] notesLogsImagesCounts = new int[]{0, 0, 0};

//
//      
//
//            dbHandler.callInTransaction(() -> {
//                ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
//
//                HashMap<Long, Long> deviceNoteId2serverNoteId = new HashMap<>();
//                List<FileItem> items = sfu.parseRequest(request);
//                /*
//                 * first handle notes, since images need to be placed connected
//                 */
//                for( FileItem item : items ) {
//                    String name = item.getName();
//                    if (name.equals(GssNote.OBJID)) {
//                        DataInputStream dis = new DataInputStream(item.getInputStream());
//                        while( dis.available() > 0 ) {
//                            GssNote note = new GssNote();
//                            note.internalize(GssNote.VERSION, dis);
//
//                            Point point = gf.createPoint(new Coordinate(note.longitude, note.latitude));
//                            Notes serverNote = new Notes(point, note.altitude, note.timeStamp, note.description, note.text,
//                                    note.form, note.style, gpapUser);
//
//                            notesDao.create(serverNote);
//                            deviceNoteId2serverNoteId.put(note.id, serverNote.id);
//                            notesLogsImagesCounts[0] += 1;
//                            ServletUtils.debug("Uploaded note: " + serverNote.text); //$NON-NLS-1$
//                        }
//                    } else if (name.equals(GssGpsLog.OBJID)) {
//                        DataInputStream dis = new DataInputStream(item.getInputStream());
//
//                        List<GpsLogs> logs = new ArrayList<>();
//                        List<GpsLogsData> logsData = new ArrayList<>();
//                        List<GpsLogsProperties> logsProps = new ArrayList<>();
//                        while( dis.available() > 0 ) {
//                            GssGpsLog gpsLog = new GssGpsLog();
//                            gpsLog.internalize(GssGpsLog.VERSION, dis);
//
//                            List<Coordinate> logCoordinates = gpsLog.points.stream()
//                                    .map(gp -> new Coordinate(gp.longitude, gp.latitude)).collect(Collectors.toList());
//                            LineString logLine = gf
//                                    .createLineString(logCoordinates.toArray(new Coordinate[logCoordinates.size()]));
//                            GpsLogs newLog = new GpsLogs(gpsLog.name, gpsLog.startts, gpsLog.endts, logLine, gpapUser);
//                            logs.add(newLog);
//
//                            for( GssGpsLogPoint gpsPoint : gpsLog.points ) {
//                                Coordinate c = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
//                                Point point = gf.createPoint(c);
//                                GpsLogsData gpsLogsData = new GpsLogsData(point, gpsPoint.altimetry, gpsPoint.ts, newLog);
//                                logsData.add(gpsLogsData);
//                            }
//
//                            GpsLogsProperties prop = new GpsLogsProperties(gpsLog.color, gpsLog.width, newLog);
//                            logsProps.add(prop);
//                            notesLogsImagesCounts[1] += 1;
//                        }
//                        logsDao.create(logs);
//                        logsDataDao.create(logsData);
//                        logsPropsDao.create(logsProps);
//                        ServletUtils.debug("Uploaded Logs: " + logs.size()); //$NON-NLS-1$
//
//                    }
//                }
//
//                /*
//                 * now handle images
//                 */
//                for( FileItem item : items ) {
//                    String name = item.getName();
//                    if (name.startsWith(GssImage.OBJID)) {
//                        DataInputStream dis = new DataInputStream(item.getInputStream());
//                        GssImage image = new GssImage();
//                        image.internalize(GssImage.VERSION, dis);
//
//                        ImageData imgData = new ImageData(image.dataThumb, image.data);
//                        imageDataDao.create(imgData);
//
//                        Notes tmpNote = null;
//                        if (image.noteId != -1) {
//                            Long noteId = deviceNoteId2serverNoteId.get(image.noteId);
//                            tmpNote = new Notes(noteId);
//                        }
//                        Point point = gf.createPoint(new Coordinate(image.longitude, image.latitude));
//                        Images img = new Images(point, image.altitude, image.timeStamp, image.azimuth, image.text, tmpNote,
//                                imgData, gpapUser);
//                        imagesDao.create(img);
//                        notesLogsImagesCounts[2] += 1;
//
//                        String str = ""; //$NON-NLS-1$
//                        if (tmpNote != null) {
//                            str = " for note: " + tmpNote.id; //$NON-NLS-1$
//                        }
//                        ServletUtils.debug("Uploaded image: " + image.text + str); //$NON-NLS-1$
//                    }
//                }
//
//                return null;
//            });

            logDb.insert(EMessageType.ACCESS, TAG, "Upload connection from '" + deviceId + "' completed properly."); //$NON-NLS-1$//$NON-NLS-2$
            String message = MessageFormat.format(Messages.getString("UploadServlet.data_uploaded"), //$NON-NLS-1$
                    notesLogsImagesCounts[0], notesLogsImagesCounts[1], notesLogsImagesCounts[2]);

            ServletUtils.debug("SENDING RESPONSE MESSAGE: " + message); //$NON-NLS-1$
            KukuratusStatus okStatus = new KukuratusStatus(KukuratusStatus.CODE_200_OK, message);
            okStatus.sendTo(response);
        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Upload connection from '" + deviceId + "' errored with:\n", ex); //$NON-NLS-1$ //$NON-NLS-2$
                /*
                 * if there are problems, return some information.
                 */
                String msg = Messages.getString("UploadServlet.error_uploading"); //$NON-NLS-1$
                KukuratusStatus errStatus = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, msg, ex);
                errStatus.sendTo(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getFilename( Part part ) {
        for( String cd : part.getHeader("content-disposition").split(";") ) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private static String getValue( Part part ) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[1024];
        for( int length = 0; (length = reader.read(buffer)) > 0; ) {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }

}
