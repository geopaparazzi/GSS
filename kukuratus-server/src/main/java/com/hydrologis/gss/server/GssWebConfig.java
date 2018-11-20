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
package com.hydrologis.gss.server;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

@SuppressWarnings("serial")
public class GssWebConfig {
    public static final String KEY_AUTOMATIC_REGISTRATION = "GSS_KEY_AUTOMATIC_REGISTRATION"; //$NON-NLS-1$
    public static final int timerMinutes = 5;

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(ui = GssApplication.class, productionMode = true)
    public static class WebappVaadinServlet extends VaadinServlet {
    }

    @WebListener
    public static class JdbcExampleContextListener implements ServletContextListener {

        @Override
        public void contextInitialized( ServletContextEvent sce ) {
            /// called when the system starts up and the servlet context is initialized
            try {
                SpiHandler.getDbProviderSingleton().init();

                File dataFolder = KukuratusWorkspace.getInstance().getDataFolder();
                File notesOutFile = new File(dataFolder, "notes.png"); //$NON-NLS-1$
                File imagesOutFile = new File(dataFolder, "images.png"); //$NON-NLS-1$
                if (!notesOutFile.exists()) {
                    InputStream notesIs = GssWebConfig.class.getResourceAsStream("/images/notes.png"); //$NON-NLS-1$
                    Files.copy(notesIs, notesOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                if (!imagesOutFile.exists()) {
                    InputStream imagesIs = GssWebConfig.class.getResourceAsStream("/images/images.png"); //$NON-NLS-1$
                    Files.copy(imagesIs, imagesOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void contextDestroyed( ServletContextEvent sce ) {
            try {
                SpiHandler.getDbProviderSingleton().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
