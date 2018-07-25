package com.hydrologis.gss.server;

import com.hydrologis.kukuratus.libs.KukuratusLibs;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.auth.LoginPage;
import com.hydrologis.kukuratus.libs.maps.MapComponent;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

//@Widgetset("org.vaadin.addon.leaflet.Widgetset")
public class GssApplication extends UI {
    @Override
    protected void init( VaadinRequest request ) {
        KukuratusLibs.init();
        
        if (AuthService.isAuthenticated()) {
            String contextPath = request.getContextPath();
            System.out.println("CONTEXT PATH: " + contextPath);
            setContent(new MapComponent());
        } else {
            setContent(new LoginPage());
        }

    }

}
