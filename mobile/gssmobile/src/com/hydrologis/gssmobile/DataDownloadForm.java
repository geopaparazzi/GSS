/** *****************************************************************************
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
 * ****************************************************************************
 */
package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.hydrologis.cn1.libs.*;
import com.codename1.db.Database;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.JSONParser;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import com.codename1.ui.CN;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;
import com.hydrologis.cn1.libs.kukuratus.KukuratusConnectionRequest;
import com.hydrologis.gssmobile.utils.GssUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hydrologis
 */
public class DataDownloadForm extends Form {

    private Container list = null;
    private Database db = null;
    private FontImage basemapIcon;
    private FontImage overlaysIcon;
    private FontImage projectsIcon;
    private final Resources theme;

    public DataDownloadForm(Form previous, Resources theme) {
        setLayout(new BorderLayout());
        Toolbar tb = getToolbar();
        tb.setBackCommand("Back", (e) -> {
            previous.showBack();
        });
        setTitle("Data Download");

        init();
        this.theme = theme;
    }

    private void init() {

        basemapIcon = FontImage.createMaterial(FontImage.MATERIAL_GRID_ON, "basemapsIcon", 4);
        overlaysIcon = FontImage.createMaterial(FontImage.MATERIAL_TIMELINE, "overlaysIcon", 4);
        projectsIcon = FontImage.createMaterial(FontImage.MATERIAL_STORAGE, "storageIcon", 4);

        try {
            list = new Container(BoxLayout.y());
            list.setScrollableY(true);
            add(BorderLayout.CENTER, list);

            FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_REFRESH);
            fab.bindFabToContainer(this.getContentPane());
            fab.addActionListener(e -> refreshDataList());

            list.forceRevalidate();
        } catch (Exception ex) {
            HyLog.e(ex);
        }
    }

    private void refreshDataList() {

        String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "");
        if (serverUrl.trim().length() == 0) {
            HyDialogs.showErrorDialog("No server url has been define. Please set the proper url from the side menu.");
            return;
        }
        serverUrl = serverUrl + GssUtilities.DATA_DOWNLOAD_PATH;

        KukuratusConnectionRequest req = new KukuratusConnectionRequest() {
            @Override
            public void readOkResponse(InputStream input) throws IOException {
                InputStreamReader reader = new InputStreamReader(input);
                JSONParser parser = new JSONParser();
                Map<String, Object> response = parser.parseJSON(reader);
                if (response != null) {
                    List baseMapsJson = (List) response.get(GssUtilities.DATA_DOWNLOAD_BASEMAP);
                    List overlaysJson = (List) response.get(GssUtilities.DATA_DOWNLOAD_OVERLAYS);
                    List projectsJson = (List) response.get(GssUtilities.DATA_DOWNLOAD_PROJECTS);

                    CN.callSerially(() -> {
                        list.removeAll();
                        for (Object obj : baseMapsJson) {
                            if (obj instanceof HashMap) {
                                HashMap hashMap = (HashMap) obj;
                                Object nameObj = hashMap.get(GssUtilities.DATA_DOWNLOAD_NAME);
                                if (nameObj instanceof String) {
                                    String name = (String) nameObj;
                                    addDownloadRow(name, basemapIcon, 0);
                                }
                            }
                        }
                        for (Object obj : overlaysJson) {
                            if (obj instanceof HashMap) {
                                HashMap hashMap = (HashMap) obj;
                                Object nameObj = hashMap.get(GssUtilities.DATA_DOWNLOAD_NAME);
                                if (nameObj instanceof String) {
                                    String name = (String) nameObj;
                                    addDownloadRow(name, overlaysIcon, 1);
                                }
                            }
                        }
                        for (Object obj : projectsJson) {
                            if (obj instanceof HashMap) {
                                HashMap hashMap = (HashMap) obj;
                                Object nameObj = hashMap.get(GssUtilities.DATA_DOWNLOAD_NAME);
                                if (nameObj instanceof String) {
                                    String name = (String) nameObj;
                                    addDownloadRow(name, projectsIcon, 2);
                                }
                            }
                        }

                        list.forceRevalidate();
                    });
                } else {
                    CN.callSerially(() -> {
                        HyDialogs.showWarningDialog("Could not retrieve data list.");
                    });
                }
            }
        };

        req.doGetWithProgress(serverUrl, GssUtilities.getAuthHeader(), theme, "Downloading data list...");
    }

    private void addDownloadRow(String name, Image typeIcon, int type) {

        Label label = new Label(name, typeIcon);

        Button downloadButton = new Button(FontImage.MATERIAL_CLOUD_DOWNLOAD, "hylistdownloadbutton");
        downloadButton.addActionListener(e -> {
            try {
                downloadFile(name, type);
            } catch (IOException ex) {
                HyLog.e(ex);
            }
        });

        Container rowContainer = new Container(BoxLayout.x(), "hylistrow");
        rowContainer.add(downloadButton);
        rowContainer.add(label);

        list.add(rowContainer);
    }

    private void downloadFile(String name, int type) throws IOException {
        String sdcard = FileUtilities.INSTANCE.getSdcard();

        String gssMapsFolder;
        if (type == 0 || type == 1) {
            gssMapsFolder = sdcard + "/gssworkspace/maps";
        } else {
            gssMapsFolder = sdcard + "/gssworkspace/projects";
        }
        File mapsFile = new File(gssMapsFolder);
        if (!mapsFile.exists()) {
            mapsFile.mkdirs();
        }
        HyLog.p("download folder: " + gssMapsFolder);
        List<String> existingFiles = FileUtilities.INSTANCE.findFilesByExtension(gssMapsFolder, name);
        if (existingFiles.size() > 0) {
            HyDialogs.showWarningDialog("A file with the same name already exists on the device. Not overwriting it!");
        } else {
            String filePath = gssMapsFolder + "/" + name;

            String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "");
            if (serverUrl.trim().length() == 0) {
                HyDialogs.showErrorDialog("No server url has been define. Please set the proper url from the side menu.");
                return;
            }
            serverUrl = serverUrl + GssUtilities.DATA_DOWNLOAD_PATH + "?" + GssUtilities.DATA_DOWNLOAD_NAME + "=" + name;

            HyLog.d("Downloading from: " + serverUrl);
            KukuratusConnectionRequest req = new KukuratusConnectionRequest() {
                @Override
                public void readOkResponse(InputStream input) throws IOException {
                    try {
                        if (isKilled()) {
                            return;
                        }
                        if (filePath != null) {
                            FileSystemStorage fsStorage = FileSystemStorage.getInstance();
                            OutputStream out = fsStorage.openOutputStream(filePath);
                            Util.copy(input, out);
                            if (!isKilled()) {
                                CN.callSerially(() -> {
                                    HyDialogs.showInfoDialog("File downloaded to: " + FileUtilities.stripFileProtocol(filePath));
                                });
                            }

                            if (isKilled()) {
                                FileSystemStorage.getInstance().delete(filePath);
                            }
                        }

                    } catch (IOException iOException) {
                        FileSystemStorage.getInstance().delete(filePath);
                        CN.callSerially(() -> {
                            HyDialogs.showErrorDialog("An error occurred while downloading: " + name);
                        });

                        throw iOException;
                    }
                }

            };

            req.doGetWithProgress(serverUrl, GssUtilities.getAuthHeader(), theme, "Downloading " + name + " (this might take a while)...");
        }

    }

}
