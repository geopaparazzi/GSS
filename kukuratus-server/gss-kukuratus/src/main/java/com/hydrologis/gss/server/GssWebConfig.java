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

public class GssWebConfig {

    @WebServlet("/*")
    @VaadinServletConfiguration(ui = GssApplication.class, productionMode = true)
    public static class WebappVaadinServlet extends VaadinServlet {
    }

    @WebListener
    public static class JdbcExampleContextListener implements ServletContextListener {

        @Override
        public void contextInitialized( ServletContextEvent sce ) {
            /// called when the system starts up and the servlet context is initialized
            try {
                SpiHandler.INSTANCE.getDbProvider().init();

                File dataFolder = KukuratusWorkspace.getInstance().getDataFolder(null).get();
                File notesOutFile = new File(dataFolder, "notes.png");
                File imagesOutFile = new File(dataFolder, "images.png");
                if (!notesOutFile.exists()) {
                    InputStream notesIs = GssWebConfig.class.getResourceAsStream("/images/notes.png");
                    Files.copy(notesIs, notesOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                if (!imagesOutFile.exists()) {
                    InputStream imagesIs = GssWebConfig.class.getResourceAsStream("/images/images.png");
                    Files.copy(imagesIs, imagesOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void contextDestroyed( ServletContextEvent sce ) {
            try {
                SpiHandler.INSTANCE.getDbProvider().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}