package com.hydrologis.gss.server.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class KmzExportView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    @Override
    public void enter( ViewChangeEvent event ) {
        Label title = new Label();

        title.setCaption("KMZ View");
        title.setValue("KMZ view");

        addComponent(title);
    }
}
