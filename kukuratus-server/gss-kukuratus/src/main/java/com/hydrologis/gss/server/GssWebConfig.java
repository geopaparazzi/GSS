package com.hydrologis.gss.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

public class GssWebConfig {

    @WebServlet("/*")
    @VaadinServletConfiguration(ui = GssApplication.class, productionMode = false)
    public static class WebappVaadinServlet extends VaadinServlet {
    }

    @WebListener
    public static class JdbcExampleContextListener implements ServletContextListener {

        @Override
        public void contextInitialized( ServletContextEvent sce ) {
            /// called when the system starts up and the servlet context is initialized
            try {
                GssDbProvider.INSTANCE.getDatabaseHandler();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void contextDestroyed( ServletContextEvent sce ) {
            try {
                GssDbProvider.INSTANCE.getDatabaseHandler().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}