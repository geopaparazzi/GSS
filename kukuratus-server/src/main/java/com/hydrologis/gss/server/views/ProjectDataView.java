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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import com.hydrologis.gss.server.GssWorkspace;
import com.hydrologis.gss.server.utils.BaseMap;
import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.gss.server.utils.Projects;
import com.hydrologis.kukuratus.libs.spi.DefaultPage;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;

public class ProjectDataView extends VerticalLayout implements View, DefaultPage {
    private static final long serialVersionUID = 1L;
    private ProgressBar progressBar;
    private Grid<BaseMap> basemapsGrid;
    private Grid<Projects> projectsGrid;
    private VerticalLayout dropPane;
    private File basemapsFolderFile;
    private File projectsFolderFile;

    @Override
    public void enter( ViewChangeEvent event ) {
        Optional<File> basemapsFolder = GssWorkspace.INSTANCE.getBasemapsFolder();
        Optional<File> projectsFolder = GssWorkspace.INSTANCE.getProjectsFolder();
        if (!basemapsFolder.isPresent() || !projectsFolder.isPresent()) {
            Notification.show(Messages.getString("ProjectDataView.problem_datafolders"), //$NON-NLS-1$
                    Notification.Type.WARNING_MESSAGE);
            return;
        }
        basemapsFolderFile = basemapsFolder.get();
        projectsFolderFile = projectsFolder.get();

        HorizontalLayout mainDataLayout = new HorizontalLayout();

        // base maps
        VerticalLayout basemapsLayout = new VerticalLayout();

        Label basemapsLabel = new Label(Messages.getString("ProjectDataView.basemaps")); //$NON-NLS-1$
        basemapsLayout.addComponent(basemapsLabel);
        basemapsLayout.setComponentAlignment(basemapsLabel, Alignment.MIDDLE_CENTER);

        basemapsGrid = new Grid<>();
        basemapsGrid.setSelectionMode(SelectionMode.NONE);
        basemapsGrid.setHeaderVisible(false);
        basemapsGrid.setColumns();
        basemapsGrid.addColumn(BaseMap::getMapName);
        basemapsGrid.setSizeFull();
        basemapsLayout.addComponent(basemapsGrid);
        basemapsLayout.setSizeFull();

        basemapsLayout.setExpandRatio(basemapsLabel, 1);
        basemapsLayout.setExpandRatio(basemapsGrid, 20);
        mainDataLayout.addComponent(basemapsLayout);

        // projects
        VerticalLayout projectsLayout = new VerticalLayout();

        Label projectsLabel = new Label(Messages.getString("ProjectDataView.projects")); //$NON-NLS-1$
        projectsLayout.addComponent(projectsLabel);
        projectsLayout.setComponentAlignment(projectsLabel, Alignment.MIDDLE_CENTER);

        projectsGrid = new Grid<>();
        projectsGrid.setSelectionMode(SelectionMode.NONE);
        projectsGrid.setHeaderVisible(false);
        projectsGrid.setColumns();
        projectsGrid.addColumn(Projects::getName);
        projectsGrid.setSizeFull();
        projectsLayout.addComponent(projectsGrid);
        projectsLayout.setSizeFull();

        projectsLayout.setExpandRatio(projectsLabel, 1);
        projectsLayout.setExpandRatio(projectsGrid, 20);

        mainDataLayout.addComponent(projectsLayout);

        mainDataLayout.setSizeFull();

        final Label infoLabel = new Label(Messages.getString("ProjectDataView.drop_data"), ContentMode.HTML); //$NON-NLS-1$
        infoLabel.setSizeUndefined();

        dropPane = new VerticalLayout(infoLabel);
        dropPane.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);
        dropPane.addStyleName(ValoTheme.PANEL_WELL);// ("drop-area");

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        dropPane.addComponent(progressBar);

        dropPane.setWidth("40%"); //$NON-NLS-1$
        dropPane.setHeight("40%"); //$NON-NLS-1$

        addComponent(mainDataLayout);
        addComponent(dropPane);

        setExpandRatio(mainDataLayout, 3);
        setExpandRatio(dropPane, 1);

        setComponentAlignment(dropPane, Alignment.MIDDLE_CENTER);

        setSizeFull();

        refresh();

        addDropFunctionality();
    }

    @SuppressWarnings("serial")
    private void addDropFunctionality() {
        new FileDropTarget<>(dropPane, fileDropEvent -> {
            final int fileSizeLimit = 200 * 1024 * 1024; // 200MB

            fileDropEvent.getFiles().forEach(html5File -> {
                final String fileName = html5File.getFileName();

                File outputFile = null;
                if (GssWorkspace.isBaseMap(fileName)) {
                    outputFile = new File(basemapsFolderFile, fileName);
                } else if (GssWorkspace.isProject(fileName)) {
                    outputFile = new File(projectsFolderFile, fileName);
                } else {
                    Notification.show(MessageFormat.format(Messages.getString("ProjectDataView.format_not_supported"), fileName), //$NON-NLS-1$
                            Notification.Type.WARNING_MESSAGE);
                    return;
                }

                final File _outputFile = outputFile;
                if (html5File.getFileSize() > fileSizeLimit) {
                    Notification.show(MessageFormat.format(Messages.getString("ProjectDataView.file_rejected"), //$NON-NLS-1$
                            fileSizeLimit), Notification.Type.WARNING_MESSAGE);
                } else if (outputFile.exists()) {
                    Notification.show(Messages.getString("ProjectDataView.not_overwriting") + fileName, //$NON-NLS-1$
                            Notification.Type.WARNING_MESSAGE);
                } else {
                    final StreamVariable streamVariable = new StreamVariable(){

                        @Override
                        public OutputStream getOutputStream() {
                            try {
                                return new FileOutputStream(_outputFile);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        public boolean listenProgress() {
                            return false;
                        }

                        @Override
                        public void onProgress( final StreamingProgressEvent event ) {
                        }

                        @Override
                        public void streamingStarted( final StreamingStartEvent event ) {
                        }

                        @Override
                        public void streamingFinished( final StreamingEndEvent event ) {
                            progressBar.setVisible(false);
                            refresh();
                        }

                        @Override
                        public void streamingFailed( final StreamingErrorEvent event ) {
                            progressBar.setVisible(false);
                        }

                        @Override
                        public boolean isInterrupted() {
                            return false;
                        }
                    };
                    html5File.setStreamVariable(streamVariable);
                    progressBar.setVisible(true);
                }
            });
        });

    }

    private void refresh() {
        Optional<File> basemapsFolder = GssWorkspace.INSTANCE.getBasemapsFolder();
        basemapsFolder.ifPresent(folder -> {
            List<BaseMap> maps = GssWorkspace.INSTANCE.getBasemaps();
            basemapsGrid.setItems(maps);
        });

        Optional<File> projectsFolder = GssWorkspace.INSTANCE.getProjectsFolder();
        projectsFolder.ifPresent(folder -> {
            List<Projects> projects = GssWorkspace.INSTANCE.getProjects();
            projectsGrid.setItems(projects);
        });

    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.DATABASE;
    }

    @Override
    public String getLabel() {
        return Messages.getString("ProjectDataView.projectdata_label"); //$NON-NLS-1$
    }
    
    @Override
    public String getPagePath() {
        return "projectdata"; //$NON-NLS-1$
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
