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

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hortonmachine.dbs.log.Logger;

import com.hydrologis.gss.server.database.objects.Forms;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.servlets.Status;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;

@WebServlet(urlPatterns = "/tagdownload")
public class FormDownloadServlet extends HttpServlet {
    private static final String TAG = FormDownloadServlet.class.getSimpleName();
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
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
            String tagName = request.getParameter("name");
            if (tagName != null) {
                Dao<Forms, ? > formsDao = dbHandler.getDao(Forms.class);
                Forms form = formsDao.queryBuilder().where().eq(Forms.NAME_FIELD_NAME, tagName).queryForFirst();

                response.setHeader("Content-Type", "application/json");
                ServletOutputStream outputStream = response.getOutputStream();
                PrintStream bou = new PrintStream(outputStream);
                bou.println(form.form);
                bou.close();
                outputStream.flush();
            }

        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Tag download connection from '" + deviceId + "' at ip:" + ipAddress + " errored with:\n",
                        ex);
                /*
                 * if there are problems, return some information.
                 */
                String msg = "An error occurred while downloading data from the server.";
                Status errStatus = new Status(Status.CODE_500_INTERNAL_SERVER_ERROR, msg, ex);
                errStatus.sendTo(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
