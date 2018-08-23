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

import com.hydrologis.gss.server.views.DashboardPage;
import com.hydrologis.kukuratus.libs.spi.ThemeHandler;
import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Image;

public class GssThemeHandler implements ThemeHandler {

    @Override
    public String getApplicationTitle() {
        return "Geopaparazzi Survey Server";
    }
    
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends View> Class<T> getDefaultNavigationViewClass() {
        return (Class<T>) DashboardPage.class;
    }



}
