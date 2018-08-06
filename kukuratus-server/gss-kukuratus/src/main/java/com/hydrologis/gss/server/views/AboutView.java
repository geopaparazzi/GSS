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
package com.hydrologis.gss.server.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AboutView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    @Override
    public void enter( ViewChangeEvent event ) {

        VerticalLayout layout = new VerticalLayout();

        ThemeResource resource = new ThemeResource("images/gss_logo.png");
        Image image = new Image("", resource);

        layout.addComponent(image);

        String gssText = "<b>Geopaparazzi Survey Server</b> is brought to you by <a href='http://www.hydrologis.com'>HydroloGIS</a>.";
        gssText += "<br/><br/>It is built with <a href='https://vaadin.com/'>Vaadin</a>. The application is released under the "
                + "<a href='https://www.gnu.org/licenses/gpl-3.0.en.html'>GPL3</a>. <br/>The source code is shipped with the application.";
        Label label1 = new Label(gssText);
        label1.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);

        String gpapText = "Information about <b>Geopaparazzi</b> can be found <a href='http://www.geopaparazzi.eu'>here</a>.";
        Label label2 = new Label(gpapText);
        label2.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);

        layout.addComponent(label1);
        layout.addComponent(label2);
        layout.setSizeUndefined();

        addComponent(layout);

        setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

    }
}
