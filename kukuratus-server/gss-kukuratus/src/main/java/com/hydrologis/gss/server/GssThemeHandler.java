package com.hydrologis.gss.server;

import com.hydrologis.kukuratus.libs.spi.ThemeHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Image;

public class GssThemeHandler implements ThemeHandler {

    @Override
    public Image getLoginImage() {
        ThemeResource resource = new ThemeResource("images/gss_logo.png");
        Image image = new Image("", resource);
        return image;
    }

    @Override
    public Image getMenuImage() {
        ThemeResource resource = new ThemeResource("images/logo.png");
        Image image = new Image("", resource);
        return image;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
