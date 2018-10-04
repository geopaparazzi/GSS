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
package com.hydrologis.gssmobile;

import com.hydrologis.gssmobile.utils.GssUtilities;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.io.Preferences;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.tree.Tree;
import com.hydrologis.cn1.libs.HyDialogs;
import com.hydrologis.cn1.libs.HyLog;
import com.hydrologis.cn1.libs.HyNativeUtils;
import com.hydrologis.cn1.libs.HyUtilities;
import com.hydrologis.gssmobile.utils.ServerUrlDialog;
import com.hydrologis.gssmobile.utils.UdidDialog;

public class GssMobile {

    public static final String MASTER_GSS_PASSWORD = "gss_Master_Survey_Forever_2018";

    public static final int MPR_TIMEOUT = 5 * 60 * 1000; // 5 minutes timeout

    private Form current;
    private Resources theme;

    private Form mainForm;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        Tree.setNodeIcon(FontImage.createMaterial(FontImage.MATERIAL_NOTE, "Node", 2));
        Tree.setFolderIcon(FontImage.createMaterial(FontImage.MATERIAL_KEYBOARD_ARROW_RIGHT, "Folder", 6));
        Tree.setFolderOpenIcon(FontImage.createMaterial(FontImage.MATERIAL_KEYBOARD_ARROW_DOWN, "FolderOpen", 6));

        // Pro only feature
        HyLog.bindCrashProtection(true);

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            final Exception error = err.getError();
            if (error != null) {
                HyLog.p(error.getMessage());
                HyLog.e(error);
            }
            HyLog.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }

        HyNativeUtils nativeUtils = NativeLookup.create(HyNativeUtils.class);
        if (nativeUtils != null && nativeUtils.isSupported()) {
            boolean checkPermissions = nativeUtils.checkPemissions(HyNativeUtils.PERMISSION_WRITE_EXTERNAL_STORAGE, "This is required modify data on disk.");
            HyLog.p("Check disk write permission: " + checkPermissions);
            if (!checkPermissions) {
                callSerially(() -> {
                    HyDialogs.showErrorDialog("It is not possible to continue without allowing write access");
                });
            }
        } else {
            HyLog.p("Native Utils check disk write permission not supported.");
        }

        mainForm = new Form("Gsss", new BorderLayout());

        Toolbar toolbar = mainForm.getToolbar();
        Image iconImage = theme.getImage("gss_launcher_icon.png");
        Container topBar = BorderLayout.east(new Label(iconImage));
        topBar.add(BorderLayout.SOUTH, new Label("Geopaparazzi Survey Server Sync", "SidemenuTagline"));
        topBar.setUIID("SideCommand");
        toolbar.addComponentToSideMenu(topBar);
        toolbar.addMaterialCommandToSideMenu("Synchronization", FontImage.MATERIAL_SYNC, e -> {
            DataSynchForm synchForm = new DataSynchForm(mainForm, theme);
            synchForm.show();
        });
        toolbar.addMaterialCommandToSideMenu("Data download", FontImage.MATERIAL_CLOUD_DOWNLOAD, e -> {
            DataDownloadForm dataDownloadForm = new DataDownloadForm(mainForm, theme);
            dataDownloadForm.show();
        });
        toolbar.addMaterialCommandToSideMenu("Forms download", FontImage.MATERIAL_EVENT_NOTE, e -> {
        });
        toolbar.addMaterialCommandToSideMenu("Device Id", FontImage.MATERIAL_SECURITY, e -> {
            String udid = HyUtilities.getUdid();
            if (udid == null) {
                UdidDialog insertUdidDialog = new UdidDialog();
                insertUdidDialog.show();

                String newUdid = insertUdidDialog.getUdid();
                Preferences.set(HyUtilities.CUSTOM_UDID, newUdid);
            } else {
                HyDialogs.showInfoDialog("Unique device id", "UDID: " + udid);
            }
        });
        toolbar.addMaterialCommandToSideMenu("Server URL", FontImage.MATERIAL_CLOUD_CIRCLE, e -> {
            ServerUrlDialog serverUrlDialog = new ServerUrlDialog();
            serverUrlDialog.show();

            String serverUrl = serverUrlDialog.getServerUrl();
            Preferences.set(GssUtilities.SERVER_URL, serverUrl);
        });
        toolbar.addMaterialCommandToSideMenu("Send debug log", FontImage.MATERIAL_SEND, e -> {
            HyLog.sendLog();
        });

        mainForm.show();
    }

    public void stop() {
        current = getCurrentForm();
        if (current instanceof Dialog) {
            ((Dialog) current).dispose();
            current = getCurrentForm();
        }
    }

    public void destroy() {
    }

}
