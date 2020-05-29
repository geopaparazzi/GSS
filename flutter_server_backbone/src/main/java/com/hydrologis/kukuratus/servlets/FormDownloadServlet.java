// /*******************************************************************************
//  * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
//  * 
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU General Public License as published by
//  * the Free Software Foundation, either version 3 of the License, or
//  * (at your option) any later version.
//  * 
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU General Public License for more details.
//  * 
//  * You should have received a copy of the GNU General Public License
//  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
//  * 
//  * Author: Antonello Andrea (http://www.hydrologis.com)
//  ******************************************************************************/
// package com.hydrologis.kukuratus.servlets;

// import java.io.IOException;
// import java.util.List;

// import javax.servlet.ServletException;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import javax.servlet.http.HttpSession;

// import org.hortonmachine.dbs.log.Logger;
// import org.json.JSONArray;
// import org.json.JSONObject;

// import com.hydrologis.gss.server.database.objects.Forms;
// import com.hydrologis.gss.server.utils.FormStatus;
// import com.hydrologis.gss.server.utils.Messages;
// import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
// import com.hydrologis.kukuratus.libs.servlets.KukuratusStatus;
// import com.hydrologis.kukuratus.libs.spi.SpiHandler;
// import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
// import com.j256.ormlite.dao.Dao;

// @WebServlet(urlPatterns = "/tagsdownload")
// public class FormDownloadServlet extends HttpServlet {
//     private static final String TAG = FormDownloadServlet.class.getSimpleName();
//     private static final long serialVersionUID = 1L;

//     @Override
//     protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
//         HttpSession session = request.getSession();
//         session.setMaxInactiveInterval(60 * 10);

//         Logger logDb = KukuratusWorkspace.getInstance().getLogDb();
//         String deviceId = "unknown"; //$NON-NLS-1$
//         try {
//             String tagName = request.getParameter("name"); //$NON-NLS-1$
//             String tag = "tag download"; //$NON-NLS-1$
//             if (tagName == null) {
//                 tag = "tags list download"; //$NON-NLS-1$
//             }
//             if ((deviceId = ServletUtils.canProceed(request, response, tag)) == null) {
//                 return;
//             }

//             DatabaseHandler dbHandler = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get();
//             Dao<Forms, ? > formsDao = dbHandler.getDao(Forms.class);
//             if (tagName != null) {
//                 Forms form = formsDao.queryBuilder().where().eq(Forms.NAME_FIELD_NAME, tagName).queryForFirst();

//                 String jsonToSend = form.form;
//                 ServletUtils.sendJsonString(response, jsonToSend);
//             } else {
//                 List<Forms> visibleForms = formsDao.queryBuilder().where()
//                         .eq(Forms.STATUS_FIELD_NAME, FormStatus.VISIBLE.getStatusCode()).query();
//                 JSONObject root = new JSONObject();
//                 JSONArray formsArray = new JSONArray();
//                 root.put("tags", formsArray); //$NON-NLS-1$
//                 for( Forms form : visibleForms ) {
//                     JSONObject formObj = new JSONObject();
//                     formObj.put("tag", form.name); //$NON-NLS-1$
//                     formsArray.put(formObj);
//                 }

//                 String jsonToSend = root.toString();
//                 ServletUtils.sendJsonString(response, jsonToSend);
//             }

//         } catch (Exception ex) {
//             try {
//                 logDb.insertError(TAG, "Tag download connection from '" + deviceId + "' errored with:\n", //$NON-NLS-1$ //$NON-NLS-2$
//                         ex);
//                 /*
//                  * if there are problems, return some information.
//                  */
//                 String msg = Messages.getString("FormDownloadServlet.error_downloading"); //$NON-NLS-1$
//                 KukuratusStatus errStatus = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, msg, ex);
//                 errStatus.sendTo(response);
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         }
//     }

// }