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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.utils.BaseMap;
import com.hydrologis.gss.server.utils.Overlays;
import com.hydrologis.gss.server.utils.Projects;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;

public enum GssWorkspace {
    INSTANCE;

    public static final String BASEMAP = "basemaps";
    public static final String OVERLAYS = "overlays";
    public static final String PROJECTS = "projects";
    public static final String NAME = "name";
    private Optional<File> basemapsFolder;
    private Optional<File> overlaysFolder;
    private Optional<File> projectsFolder;

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
                if (!projectsFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(projectsFolder);
        }
        return Optional.empty();
    }

    private void checkFolders() {
        basemapsFolder = GssWorkspace.INSTANCE.getBasemapsFolder();
        overlaysFolder = GssWorkspace.INSTANCE.getOverlaysFolder();
        projectsFolder = GssWorkspace.INSTANCE.getProjectsFolder();
    }

    public List<BaseMap> getBasemaps() {
        checkFolders();
        List<BaseMap> maps = Collections.emptyList();
        if (basemapsFolder.isPresent()) {
            File[] baseMaps = basemapsFolder.get().listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return isBaseMap(name);
                }
            });

            maps = Arrays.asList(baseMaps).stream().map(file -> {
                BaseMap m = new BaseMap();
                m.setMapName(file.getName());
                return m;
            }).collect(Collectors.toList());
        }
        return maps;
    }

    public List<Overlays> getOverlays() {
        checkFolders();
        List<Overlays> maps = Collections.emptyList();
        if (overlaysFolder.isPresent()) {
            File[] overlayMaps = overlaysFolder.get().listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return isOverlay(name);
                }
            });

            maps = Arrays.asList(overlayMaps).stream().map(file -> {
                Overlays m = new Overlays();
                m.setName(file.getName());
                return m;
            }).collect(Collectors.toList());
        }
        return maps;
    }

    public List<Projects> getProjects() {
        checkFolders();
        List<Projects> maps = Collections.emptyList();
        if (projectsFolder.isPresent()) {
            File[] overlayMaps = projectsFolder.get().listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return isProject(name);
                }
            });

            maps = Arrays.asList(overlayMaps).stream().map(file -> {
                Projects m = new Projects();
                m.setName(file.getName());
                return m;
            }).collect(Collectors.toList());
        }
        return maps;
    }

    public String getMapsListJson() {
        List<BaseMap> basemaps = getBasemaps();
        List<Overlays> overlays = getOverlays();
        List<Projects> projects = getProjects();

        JSONObject root = new JSONObject();

        JSONArray bmArray = new JSONArray();
        root.put(BASEMAP, bmArray);
        for( BaseMap bm : basemaps ) {
            JSONObject bmObj = new JSONObject();
            bmObj.put(NAME, bm.getMapName());
            bmArray.put(bmObj);
        }

        JSONArray ovArray = new JSONArray();
        root.put(OVERLAYS, ovArray);
        for( Overlays ov : overlays ) {
            JSONObject ovObj = new JSONObject();
            ovObj.put(NAME, ov.getName());
            ovArray.put(ovObj);
        }

        JSONArray pArray = new JSONArray();
        root.put(PROJECTS, pArray);
        for( Projects p : projects ) {
            JSONObject pObj = new JSONObject();
            pObj.put(NAME, p.getName());
            pArray.put(pObj);
        }

        return root.toString();
    }

    public static boolean isBaseMap( String name ) {
        return name.toLowerCase().endsWith(".map") || name.toLowerCase().endsWith(".mbtiles");
    }

    public static boolean isOverlay( String name ) {
        return name.toLowerCase().endsWith(".sqlite");
    }

    public static boolean isProject( String name ) {
        return name.toLowerCase().endsWith(".gpap");
    }

    public Optional<File> getMapFile( String fileName ) {
        if (isBaseMap(fileName) && basemapsFolder.isPresent()) {
            return Optional.of(new File(basemapsFolder.get(), fileName));
        } else if (isOverlay(fileName) && overlaysFolder.isPresent()) {
            return Optional.of(new File(overlaysFolder.get(), fileName));
        } else if (isProject(fileName) && projectsFolder.isPresent()) {
            return Optional.of(new File(projectsFolder.get(), fileName));
        }
        return Optional.empty();
    }

}
