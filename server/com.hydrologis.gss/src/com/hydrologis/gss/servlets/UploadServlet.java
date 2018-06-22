package com.hydrologis.gss.servlets;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gssmobile.database.GssNote;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

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
            if (userPwd == null || !userPwd[1].equals("testpwd")) {
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
            GpapUsers _gpapUser = gpapUser;
            Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);
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

                                Point point = GeometryUtilities.gf().createPoint(new Coordinate(note.longitude, note.latitude));
                                Notes serverNote = new Notes(point, note.altitude, note.timeStamp, note.description, note.text,
                                        note.form, note.style, _gpapUser);
                                deviceNoteId2serverNoteId.put(note.id, serverNote.id);
                                serverNotes.add(serverNote);
                            }
                            notesDao.create(serverNotes);
                        }
                    }

                    return null;
                }
            });

            logDb.insert(EMessageType.ACCESS, TAG,
                    "Upload connection from '" + deviceId + "' at ip:" + ipAddress + " completed properly.");

        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Upload connection from '" + deviceId + "' at ip:" + ipAddress + " errored with:\n", ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*
             * if there are problems, return some information.
             */
            String msg = "An error occurred while uploading data to the server.";
            String className = this.getClass().getSimpleName();
            ServletUtilities.sendErrorStatus(response, ex, msg, className);
        }
    }

}