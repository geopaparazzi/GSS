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

import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.kukuratus.libs.spi.AboutPage;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AboutView extends VerticalLayout implements View, AboutPage {
    private static final long serialVersionUID = 1L;

    @Override
    public void enter( ViewChangeEvent event ) {

        VerticalLayout layout = new VerticalLayout();

        ThemeResource resource = new ThemeResource("images/gss_logo.png"); //$NON-NLS-1$
        Image image = new Image("", resource); //$NON-NLS-1$

        layout.addComponent(image);

        String gssText = Messages.getString("AboutView.gss_info_text"); //$NON-NLS-1$
        Label label1 = new Label(gssText);
        label1.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);

        String gpapText = Messages.getString("AboutView.gpap_info_text"); //$NON-NLS-1$
        Label label2 = new Label(gpapText);
        label2.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);

        layout.addComponent(label1);
        layout.addComponent(label2);
        layout.setSizeUndefined();

        addComponent(layout);

        setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.INFO_CIRCLE_O;
    }

    @Override
    public String getLabel() {
        return Messages.getString("AboutView.about_label"); //$NON-NLS-1$
    }
    
    @Override
    public String getPagePath() {
        return "about"; //$NON-NLS-1$
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends View> Class<T> getNavigationViewClass() {
        return (Class<T>) this.getClass();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public boolean onlyAdmin() {
        return false;
    }
}
