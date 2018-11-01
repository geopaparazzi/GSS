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

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.ExportPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.export.KmlRepresenter;
import com.hydrologis.kukuratus.libs.utils.export.KmzExport;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class KmzExportView extends VerticalLayout implements View, ExportPage {
    private static final long serialVersionUID = 1L;
    private Grid<GpapUsers> surveyorsGrid;
    private Button prepareBtn;
    private GridLayout mainLayout;
    private VerticalLayout downloadLayout;

    @Override
    public void enter( ViewChangeEvent event ) {
        mainLayout = new GridLayout(3, 1);
        mainLayout.setSpacing(true);

        List<GpapUsers> devices = new ArrayList<>();
        try {
            devices = SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get().getDao(GpapUsers.class).queryForAll();
        } catch (SQLException e1) {
            KukuratusLogger.logError(this, e1);
            Notification.show("An error occurred.", Type.ERROR_MESSAGE);
        }

        VerticalLayout gridLayout = new VerticalLayout();
        surveyorsGrid = new Grid<>(GpapUsers.class);
        surveyorsGrid.setItems(devices);
        surveyorsGrid.setSelectionMode(SelectionMode.MULTI);
        surveyorsGrid.setColumns();
        surveyorsGrid.addColumn(GpapUsers::getName).setCaption("Surveyors");
        surveyorsGrid.asMultiSelect().addValueChangeListener(e -> {
            Set<GpapUsers> selectedUsers = e.getValue();
            prepareBtn.setEnabled(!selectedUsers.isEmpty());
        });
        surveyorsGrid.setSizeFull();

        gridLayout.addComponent(new Label("Select Surveyors"));
        gridLayout.addComponentsAndExpand(surveyorsGrid);
        gridLayout.setSizeFull();
        mainLayout.addComponent(gridLayout);
        mainLayout.setComponentAlignment(gridLayout, Alignment.TOP_LEFT);

        prepareBtn = new Button("Prepare data", VaadinIcons.COGS);
        prepareBtn.addClickListener(e -> {
            try {
                prepareData();
            } catch (Exception e1) {
                KukuratusLogger.logError(this, e1);
                Notification.show("An error occurred.", Type.ERROR_MESSAGE);
            }
        });
        prepareBtn.setSizeUndefined();
        VerticalLayout btnLayout = new VerticalLayout(prepareBtn);
        btnLayout.setSizeFull();
        mainLayout.addComponent(btnLayout);
        mainLayout.setComponentAlignment(btnLayout, Alignment.TOP_LEFT);
        prepareBtn.setEnabled(false);

        mainLayout.setHeight("100%");
        downloadLayout = new VerticalLayout();
        downloadLayout.setSizeFull();
        mainLayout.addComponent(downloadLayout);

        addComponent(mainLayout);

        setSizeFull();

    }

    private void prepareData() throws Exception {
        Set<GpapUsers> devices = surveyorsGrid.asMultiSelect().getSelectedItems();

        DatabaseHandler dbHandler = SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get();
        Dao<Notes, ? > notesDAO = dbHandler.getDao(Notes.class);
        Dao<Images, ? > imagesDAO = dbHandler.getDao(Images.class);
        Dao<ImageData, ? > imageDataDAO = dbHandler.getDao(ImageData.class);
        Dao<GpsLogs, ? > logsDAO = dbHandler.getDao(GpsLogs.class);

        Notification.show("Report generation started", "You'll be notified once the report is ready.",
                Notification.Type.TRAY_NOTIFICATION);
        prepareBtn.setEnabled(false);

        new Thread(() -> {
            try {
                List<KmlRepresenter> kmlList = new ArrayList<>();
                List<Notes> notesList = notesDAO.queryBuilder().where().in(Notes.GPAPUSER_FIELD_NAME, devices).query();
                kmlList.addAll(notesList);
                List<GpsLogs> logsList = logsDAO.queryBuilder().where().in(Notes.GPAPUSER_FIELD_NAME, devices).query();
                kmlList.addAll(logsList);

                File tmpFolder = KukuratusWorkspace.getInstance().getTmpFolder();
                File outFile = new File(tmpFolder,
                        "gss_export_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact) + ".kmz");
                KmzExport exporter = new KmzExport(null, outFile){

                    @Override
                    protected String getImageNameById( long id ) {
                        try {
                            return imagesDAO.queryForSameId(new Images(id)).text;
                        } catch (SQLException e) {
                            KukuratusLogger.logError(this, e);
                            return "ERROR";
                        }
                    }

                    @Override
                    protected byte[] getImageDataById( long id ) {
                        try {
                            Images images = imagesDAO.queryForSameId(new Images(id));
                            ImageData imageData = imageDataDAO.queryForSameId(images.imageData);
                            return imageData.data;
                        } catch (SQLException e) {
                            KukuratusLogger.logError(this, e);
                            return null;
                        }
                    }
                };
                exporter.export(kmlList);

                FileInputStream fileInputStream = new FileInputStream(outFile);

                getUI().access(() -> {
                    Button downloadBtn = new Button("Download KMZ", VaadinIcons.DOWNLOAD_ALT);
                    downloadBtn.setSizeUndefined();
                    downloadBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
                    downloadLayout.addComponent(downloadBtn);
                    downloadLayout.setComponentAlignment(downloadBtn, Alignment.TOP_LEFT);

                    FileDownloader downloader = new FileDownloader(new StreamResource(() -> {
                        downloadLayout.removeComponent(downloadBtn);
                        prepareBtn.setEnabled(true);
                        return fileInputStream;
                    }, outFile.getName()));
                    downloader.extend(downloadBtn);

                    Notification.show("KMZ ready for download", Notification.Type.TRAY_NOTIFICATION);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
    
    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.GLOBE;
    }

    @Override
    public String getLabel() {
        return "KMZ";
    }

    @Override
    public int getOrder() {
        return 2;
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
