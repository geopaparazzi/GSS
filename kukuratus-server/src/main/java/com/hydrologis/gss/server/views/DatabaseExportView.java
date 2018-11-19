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

import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;

import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.DbProvider;
import com.hydrologis.kukuratus.libs.spi.ExportPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DatabaseExportView extends VerticalLayout implements View, ExportPage {
    private static final long serialVersionUID = 1L;

    @Override
    public void enter( ViewChangeEvent event ) {
        try {
            DbProvider dbProvider = SpiHandler.getDbProviderSingleton();
            DatabaseHandler dbHandler = dbProvider.getDatabaseHandler().get();

            String ext = ".mv.db"; //$NON-NLS-1$
            String databasePath = dbHandler.getDb().getDatabasePath();
            databasePath += ext;
            File dbFile = new File(databasePath);
            String downloadName = dbFile.getName().replaceFirst(ext,
                    "_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact) + ext); //$NON-NLS-1$

            Button button = new Button(Messages.getString("DatabaseExportView.download_db"), VaadinIcons.DOWNLOAD_ALT); //$NON-NLS-1$
            button.addStyleName(ValoTheme.BUTTON_PRIMARY);
            FileInputStream fileInputStream = new FileInputStream(databasePath);
            FileDownloader downloader = new FileDownloader(new StreamResource(() -> {
                getUI().access(() -> {
                    button.setEnabled(false);
                });
                return fileInputStream;
            }, downloadName));
            downloader.extend(button);

            addComponent(button);
            setComponentAlignment(button, Alignment.MIDDLE_CENTER);

            setSizeFull();

        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.DATABASE;
    }

    @Override
    public String getLabel() {
        return Messages.getString("DatabaseExportView.database_label"); //$NON-NLS-1$
    }
    
    @Override
    public String getPagePath() {
        return "dbexport"; //$NON-NLS-1$
    }

    @Override
    public int getOrder() {
        return 3;
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
