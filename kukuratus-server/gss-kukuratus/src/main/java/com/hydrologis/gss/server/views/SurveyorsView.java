package com.hydrologis.gss.server.views;

import java.sql.SQLException;
import java.util.List;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.SettingsPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.j256.ormlite.dao.Dao;
import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class SurveyorsView extends VerticalLayout implements View, SettingsPage {
    private static final long serialVersionUID = 1L;
    private Grid<GpapUsers> usersGrid;

    @Override
    public void enter( ViewChangeEvent event ) {
        usersGrid = new Grid<>();
        usersGrid.getEditor().setEnabled(true);
        Binder<GpapUsers> binder = usersGrid.getEditor().getBinder();
        usersGrid.getEditor().addSaveListener(e -> save(e.getBean()));

        usersGrid.setSelectionMode(SelectionMode.SINGLE);

        usersGrid.setColumns();
        usersGrid.addColumn(GpapUsers::getDeviceId).setCaption("Device Id").setExpandRatio(2);
        usersGrid.addColumn(GpapUsers::getName).setCaption("Name").setExpandRatio(3)
                .setEditorBinding(binder.forField(new TextField()).bind(GpapUsers::getName, GpapUsers::setName));
//        usersGrid.addColumn(GpapUsers::getPassword).setCaption("Password").setExpandRatio(1);
        usersGrid.addColumn(GpapUsers::getContact).setCaption("Contact").setExpandRatio(3)
                .setEditorBinding(binder.forField(new TextField()).bind(GpapUsers::getContact, GpapUsers::setContact));

        usersGrid.setColumnReorderingAllowed(true);

        usersGrid.setWidth("95%");
        usersGrid.setHeight("100%");
        addComponent(usersGrid);
//        setExpandRatio(usersGrid, 1);

        setSizeFull();
        refresh();
    }

    private void save( GpapUsers user ) {
        try {
            DatabaseHandler databaseHandler = SpiHandler.INSTANCE.getDbProvider().getDatabaseHandler().get();
            Dao<GpapUsers, ? > dao = databaseHandler.getDao(GpapUsers.class);
            dao.update(user);
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refresh() {
        DatabaseHandler databaseHandler = SpiHandler.INSTANCE.getDbProvider().getDatabaseHandler().get();
        try {
            Dao<GpapUsers, ? > dao = databaseHandler.getDao(GpapUsers.class);
            List<GpapUsers> userList = dao.queryForAll();
            usersGrid.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    
    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.SPECIALIST;
    }

    @Override
    public String getLabel() {
        return "Surveyors";
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
        return true;
    }
}
