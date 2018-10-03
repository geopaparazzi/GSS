/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.hydrologis.cn1.libs.*;
import com.codename1.db.Database;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;
import com.codename1.util.Base64;
import com.hydrologis.gssmobile.database.DaoNotes;
import com.hydrologis.gssmobile.database.GssNote;
import com.hydrologis.gssmobile.utils.GssUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hydrologis
 */
public class DataDownloadForm extends Form {

    private static final char NOTE_ICON = FontImage.MATERIAL_NOTE;
    private static final char FORM_NOTE_ICON = FontImage.MATERIAL_NOTE_ADD;

    private Container list = null;
    private Database db = null;

    public DataDownloadForm(Form previous, Resources theme) {
        setLayout(new BorderLayout());
        Toolbar tb = getToolbar();
        tb.setBackCommand("Back", (e) -> {
            previous.showBack();
        });
        setTitle("Data Download");

        init();
    }

    private void init() {

        try {
            list = new Container(BoxLayout.y());
            list.setScrollableY(true);
            add(BorderLayout.CENTER, list);

            FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_REFRESH);
            fab.bindFabToContainer(getContentPane());
            fab.addActionListener(e -> refreshDataList());

            setScrollable(false);
        } catch (Exception ex) {
            HyLog.e(ex);
        }
    }

    private void refreshDataList() {

        String authCode = HyUtilities.getUdid() + ":" + GssMobile.MASTER_GSS_PASSWORD;
        String authHeader = "Basic " + Base64.encode(authCode.getBytes());

        String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "");
        if (serverUrl.trim().length() == 0) {
            HyDialogs.showErrorDialog("No server url has been define. Please set the proper url from the side menu.");
            return;
        }
        serverUrl = serverUrl + GssUtilities.DATA_DOWNLOAD_PATH;

        ConnectionRequest req = new ConnectionRequest() {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                InputStreamReader reader = new InputStreamReader(input);
                JSONParser parser = new JSONParser();
                Map<String, Object> response = parser.parseJSON(reader);
                List baseMapsJson = (List) response.get(GssUtilities.DATA_DOWNLOAD_BASEMAP);

                List overlaysJson = (List) response.get(GssUtilities.DATA_DOWNLOAD_OVERLAYS);
                List projectsJson = (List) response.get(GssUtilities.DATA_DOWNLOAD_PROJECTS);

                list.removeAll();
                for (Object obj : baseMapsJson) {
                    if (obj instanceof HashMap) {
                        HashMap hashMap = (HashMap) obj;
                        Object nameObj = hashMap.get(GssUtilities.DATA_DOWNLOAD_NAME);
                        if (nameObj instanceof String) {
                            String name = (String) nameObj;

                            Container rowContainer = new Container(BoxLayout.x());

                            Button b = new Button(FontImage.MATERIAL_CLOUD_DOWNLOAD, name);
                            list.add(b);
                            b.addActionListener(e -> Log.p("You picked: " + b.getText()));
                        }
                    }
                }
                for (Object obj : overlaysJson) {
                    if (obj instanceof HashMap) {
                        HashMap hashMap = (HashMap) obj;
                        Object nameObj = hashMap.get(GssUtilities.DATA_DOWNLOAD_NAME);
                        if (nameObj instanceof String) {
                            String name = (String) nameObj;

                            Container rowContainer = new Container(BoxLayout.x());

                            Button b = new Button(FontImage.MATERIAL_CLOUD_DOWNLOAD, name);
                            list.add(b);
                            b.addActionListener(e -> Log.p("You picked: " + b.getText()));
                        }
                    }
                }
                for (Object obj : projectsJson) {
                    if (obj instanceof HashMap) {
                        HashMap hashMap = (HashMap) obj;
                        Object nameObj = hashMap.get(GssUtilities.DATA_DOWNLOAD_NAME);
                        if (nameObj instanceof String) {
                            String name = (String) nameObj;

                            Container rowContainer = new Container(BoxLayout.x());

                            Button b = new Button(FontImage.MATERIAL_CLOUD_DOWNLOAD, name);
                            list.add(b);
                            b.addActionListener(e -> Log.p("You picked: " + b.getText()));
                        }
                    }
                }

            }
        };

        req.setPost(false);
        req.setHttpMethod("GET");
        req.addRequestHeader("Authorization", authHeader);
        req.setUrl(serverUrl);

        NetworkManager.getInstance().addToQueue(req);
    }

}
