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
public class TagsDownloadForm extends Form {

    private Container list = null;
    private Database db = null;
    private FontImage tagsIcon;
    private final Resources theme;

    public TagsDownloadForm(Form previous, Resources theme) {
        setLayout(new BorderLayout());
        Toolbar tb = getToolbar();
        tb.setBackCommand("Back", (e) -> {
            previous.showBack();
        });
        setTitle("Tags Download");

        init();
        this.theme = theme;
    }

    private void init() {

        tagsIcon = FontImage.createMaterial(FontImage.MATERIAL_EVENT_NOTE, "tagsIcon", 4);

        try {
            list = new Container(BoxLayout.y());
            list.setScrollableY(true);
            add(BorderLayout.CENTER, list);

            FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_REFRESH);
            fab.bindFabToContainer(this.getContentPane());
            fab.addActionListener(e -> refreshDataList());

            list.forceRevalidate();
            refreshDataList();
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
        serverUrl = serverUrl + GssUtilities.TAGS_DOWNLOAD_PATH;

        KukuratusConnectionRequest req = new KukuratusConnectionRequest() {
            @Override
            public void readResponse(InputStream input) throws IOException {
                InputStreamReader reader = new InputStreamReader(input);
                try {
                    JSONParser parser = new JSONParser();
                    Map<String, Object> response = parser.parseJSON(reader);
                    List tagsJsonList = (List) response.get(GssUtilities.TAGS_DOWNLOAD_TAGS);
                    if (tagsJsonList != null) {
                        CN.callSerially(() -> {
                            list.removeAll();
                            for (Object obj : tagsJsonList) {
                                if (obj instanceof HashMap) {
                                    HashMap hashMap = (HashMap) obj;
                                    Object nameObj = hashMap.get(GssUtilities.TAGS_DOWNLOAD_TAG);
                                    if (nameObj instanceof String) {
                                        String name = (String) nameObj;
                                        addDownloadRow(name);
                                    }
                                }
                            }
                            list.forceRevalidate();
                        });
                    } else {
                        CN.callSerially(() -> {
                            HyDialogs.showWarningDialog("Could not retrieve any tag.");
                        });
                    }
                } catch (IOException iOException) {
                    CN.callSerially(() -> {
                        HyDialogs.showErrorDialog("An error occurred while downloading the tags list.");
                    });

                    HyLog.e(iOException);
                }
            }

        };
        req.doGetWithProgress(serverUrl, GssUtilities.getAuthHeader(), theme, "Downloading tags list...");
    }

    private void addDownloadRow(String name) {

        Label label = new Label(name, tagsIcon);

        Button downloadButton = new Button(FontImage.MATERIAL_CLOUD_DOWNLOAD, "hylistdownloadbutton");
        downloadButton.addActionListener(e -> {
            try {
                downloadFile(name);
            } catch (IOException ex) {
                HyLog.e(ex);
            }
        });

        Container rowContainer = new Container(BoxLayout.x(), "hylistrow");
        rowContainer.add(downloadButton);
        rowContainer.add(label);

        list.add(rowContainer);
    }

    private void downloadFile(String name) throws IOException {
        String sdcard = FileUtilities.INSTANCE.getSdcard();

        String tagsFolder = sdcard + "/geopaparazzi";
        File tagsFile = new File(tagsFolder);
        if (!tagsFile.exists()) {
            tagsFile.mkdirs();
        }
        HyLog.p("download folder: " + tagsFolder);
        final String fullName = name + "_tags.json";
        List<String> existingFiles = FileUtilities.INSTANCE.findFilesByExtension(tagsFolder, fullName);
        if (existingFiles.size() > 0) {
            HyDialogs.showWarningDialog("A file with the same name already exists on the device. Not overwriting it!");
        } else {
            String filePath = tagsFolder + "/" + fullName;

            String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "");
            if (serverUrl.trim().length() == 0) {
                HyDialogs.showErrorDialog("No server url has been define. Please set the proper url from the side menu.");
                return;
            }
            serverUrl = serverUrl + GssUtilities.TAGS_DOWNLOAD_PATH + "?" + GssUtilities.TAGS_DOWNLOAD_NAME + "=" + name;

            KukuratusConnectionRequest req = new KukuratusConnectionRequest() {
                @Override
                public void readResponse(InputStream input) throws IOException {
                    try {
                        FileSystemStorage fsStorage = FileSystemStorage.getInstance();
                        OutputStream out = fsStorage.openOutputStream(filePath);
                        Util.copy(input, out);
                        CN.callSerially(() -> {
                            HyDialogs.showInfoDialog("File downloaded to: " + FileUtilities.stripFileProtocol(filePath));
                        });
                    } catch (IOException iOException) {
                        CN.callSerially(() -> {
                            HyDialogs.showErrorDialog("An error occurred while downloading: " + name);
                        });

                        HyLog.e(iOException);
                    }
                }

            };

            req.doGetWithProgress(serverUrl, GssUtilities.getAuthHeader(), theme, "Downloading " + name + " (this might take a while)...");
        }

    }

}
