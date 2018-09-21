package com.hydrologis.gss.server;

import java.io.File;
import java.util.Optional;

import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;

public enum GssWorkspace {
    INSTANCE;

    public static final String BASEMAP = "basemaps";
    public static final String OVERLAYS = "overlays";
    public static final String PROJECTS = "projects";

    public Optional<File> getBasemapsFolder() {
        Optional<File> workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        if (workspaceFolder.isPresent()) {
            File folder = workspaceFolder.get();
            File baseMapsFolder = new File(folder, BASEMAP);
            if (!baseMapsFolder.exists()) {
                if (!baseMapsFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(baseMapsFolder);
        }
        return Optional.empty();
    }

    public Optional<File> getOverlaysFolder() {
        Optional<File> workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        if (workspaceFolder.isPresent()) {
            File folder = workspaceFolder.get();
            File overlaysFolder = new File(folder, OVERLAYS);
            if (!overlaysFolder.exists()) {
                if (!overlaysFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(overlaysFolder);
        }
        return Optional.empty();
    }

    public Optional<File> getProjectsFolder() {
        Optional<File> workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        if (workspaceFolder.isPresent()) {
            File folder = workspaceFolder.get();
            File projectsFolder = new File(folder, PROJECTS);
            if (!projectsFolder.exists()) {
                if (projectsFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(projectsFolder);
        }
        return Optional.empty();
    }

}
