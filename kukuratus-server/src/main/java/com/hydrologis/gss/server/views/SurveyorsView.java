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

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.SettingsPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusWindows;
import com.j256.ormlite.dao.Dao;
import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class SurveyorsView extends VerticalLayout implements View, SettingsPage {
    private static final long serialVersionUID = 1L;
    private Grid<GpapUsers> usersGrid;

    @Override
    public void enter( ViewChangeEvent event ) {
        Button addButton = new Button(VaadinIcons.PLUS);
        addButton.setDescription(Messages.getString("SurveyorsView.add_surveyor")); //$NON-NLS-1$
        addButton.addClickListener(e -> {
            SurveyorFormWindow window = new SurveyorFormWindow(Messages.getString("SurveyorsView.add_surveyor"), new GpapUsers()); //$NON-NLS-1$
            getUI().addWindow(window);
        });
        addComponent(addButton);

        usersGrid = new Grid<>();
        usersGrid.getEditor().setEnabled(true);
        Binder<GpapUsers> binder = usersGrid.getEditor().getBinder();
        usersGrid.getEditor().addSaveListener(e -> save(e.getBean()));

        usersGrid.setSelectionMode(SelectionMode.SINGLE);

        usersGrid.setColumns();
        usersGrid.addColumn(GpapUsers::getDeviceId).setCaption(Messages.getString("SurveyorsView.device_id")).setExpandRatio(2); //$NON-NLS-1$
        usersGrid.addColumn(GpapUsers::getName).setCaption(Messages.getString("SurveyorsView.name")).setExpandRatio(3) //$NON-NLS-1$
                .setEditorBinding(binder.forField(new TextField()).bind(GpapUsers::getName, GpapUsers::setName));
        usersGrid.addColumn(GpapUsers::getContact).setCaption(Messages.getString("SurveyorsView.contact")).setExpandRatio(3) //$NON-NLS-1$
                .setEditorBinding(binder.forField(new TextField()).bind(GpapUsers::getContact, GpapUsers::setContact));
        usersGrid.addComponentColumn(gpapUser -> new Button(VaadinIcons.TRASH, e -> deleteSurveyorClicked(gpapUser)))
                .setExpandRatio(1);

        usersGrid.setColumnReorderingAllowed(true);

        usersGrid.setWidth("95%"); //$NON-NLS-1$
        usersGrid.setHeight("100%"); //$NON-NLS-1$
        addComponent(usersGrid);
        setExpandRatio(usersGrid, 1);

        setSizeFull();
        refresh();
    }

    private void deleteSurveyorClicked( GpapUsers user ) {
        KukuratusWindows.openCancelDeleteWindow(usersGrid,
                MessageFormat.format(
                        Messages.getString("SurveyorsView.sure_delete_surveyor"), //$NON-NLS-1$
                        user.name),
                null, null, new Runnable(){
                    public void run() {

                        DatabaseHandler databaseHandler = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get();
                        try {
                            Dao<GpapUsers, ? > dao = databaseHandler.getDao(GpapUsers.class);
                            dao.delete(user);

                            getUI().access(() -> {
                                refresh();
                            });

                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void save( GpapUsers user ) {
        try {
            DatabaseHandler databaseHandler = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get();
            Dao<GpapUsers, ? > dao = databaseHandler.getDao(GpapUsers.class);
            dao.update(user);
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refresh() {
        DatabaseHandler databaseHandler = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get();
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
        return Messages.getString("SurveyorsView.sruveyors_label"); //$NON-NLS-1$
    }
    
    @Override
    public String getPagePath() {
        return "surveyors"; //$NON-NLS-1$
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

    @SuppressWarnings("serial")
    private class SurveyorFormWindow extends Window {

        private TextField deviceId = new TextField(Messages.getString("SurveyorsView.device_id")); //$NON-NLS-1$
        private TextField name = new TextField(Messages.getString("SurveyorsView.name")); //$NON-NLS-1$
        private TextField contact = new TextField(Messages.getString("SurveyorsView.contact")); //$NON-NLS-1$

        private Button cancel = new Button(Messages.getString("SurveyorsView.cancel")); //$NON-NLS-1$
        private Button save = new Button(Messages.getString("SurveyorsView.save"), VaadinIcons.CHECK); //$NON-NLS-1$

        public SurveyorFormWindow( String caption, GpapUsers user ) {
            initLayout(caption);
            initBehavior(user);
        }

        private void initLayout( String caption ) {
            setCaption(caption);
            save.addStyleName(ValoTheme.BUTTON_PRIMARY);

            HorizontalLayout buttons = new HorizontalLayout(cancel, save);
            buttons.setSpacing(true);

            GridLayout formLayout = new GridLayout(3, 2, deviceId, name, contact);
            formLayout.setMargin(true);
            formLayout.setSpacing(true);

            VerticalLayout layout = new VerticalLayout(formLayout, buttons);
            layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);
            setContent(layout);
            setModal(true);
            center();
        }

        private void initBehavior( GpapUsers user ) {
            Binder<GpapUsers> binder = new Binder<>(GpapUsers.class);
            binder.bindInstanceFields(this);
            binder.readBean(user);

            cancel.addClickListener(e -> close());
            save.addClickListener(e -> {
                try {
                    binder.writeBean(user);

                    if (user.getName() != null && user.getName().trim().length() == 0) {
                        Notification.show(Messages.getString("SurveyorsView.name_mandatory"), Type.ERROR_MESSAGE); //$NON-NLS-1$
                        return;
                    }
                    if (user.getDeviceId() != null && user.getDeviceId().trim().length() == 0) {
                        Notification.show(Messages.getString("SurveyorsView.device_mandatory"), Type.ERROR_MESSAGE); //$NON-NLS-1$
                        return;
                    }

                    DatabaseHandler databaseHandler = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get();
                    Dao<GpapUsers, ? > dao = databaseHandler.getDao(GpapUsers.class);

                    GpapUsers sameNameUser = dao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, user.getDeviceId())
                            .queryForFirst();
                    if (sameNameUser != null) {
                        Notification.show(Messages.getString("SurveyorsView.device_same_id_exists"), Type.ERROR_MESSAGE); //$NON-NLS-1$
                    } else {
                        dao.create(user);
                        close();
                        refresh();
                        Notification.show(Messages.getString("SurveyorsView.surveyor_saved")); //$NON-NLS-1$
                    }
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
                }
            });
        }
    }
}
