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

    public static final String MAPS = "maps"; //$NON-NLS-1$
    public static final String PROJECTS = "projects"; //$NON-NLS-1$
    public static final String NAME = "name"; //$NON-NLS-1$
    private Optional<File> basemapsFolder;
    private Optional<File> projectsFolder;

    public Optional<File> getBasemapsFolder() {
        File workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        File baseMapsFolder = new File(workspaceFolder, MAPS);
        if (!baseMapsFolder.exists()) {
            if (!baseMapsFolder.mkdirs()) {
                return Optional.empty();
            }
        }
        return Optional.of(baseMapsFolder);
    }

    public Optional<File> getProjectsFolder() {
        File workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        File projectsFolder = new File(workspaceFolder, PROJECTS);
        if (!projectsFolder.exists()) {
            if (!projectsFolder.mkdirs()) {
                return Optional.empty();
            }
        }
        return Optional.of(projectsFolder);
    }

    private void checkFolders() {
        basemapsFolder = GssWorkspace.INSTANCE.getBasemapsFolder();
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
        List<Projects> projects = getProjects();

        JSONObject root = new JSONObject();

        JSONArray bmArray = new JSONArray();
        root.put(MAPS, bmArray);
        for( BaseMap bm : basemaps ) {
            JSONObject bmObj = new JSONObject();
            bmObj.put(NAME, bm.getMapName());
            bmArray.put(bmObj);
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
        return name.toLowerCase().endsWith(".map") || name.toLowerCase().endsWith(".mbtiles")
                || name.toLowerCase().endsWith(".sqlite") || name.toLowerCase().endsWith(".gpkg"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean isProject( String name ) {
        return name.toLowerCase().endsWith(".gpap"); //$NON-NLS-1$
    }

    public Optional<File> getMapFile( String fileName ) {
        if (isBaseMap(fileName) && basemapsFolder.isPresent()) {
            return Optional.of(new File(basemapsFolder.get(), fileName));
        } else if (isProject(fileName) && projectsFolder.isPresent()) {
            return Optional.of(new File(projectsFolder.get(), fileName));
        }
        return Optional.empty();
    }

}
