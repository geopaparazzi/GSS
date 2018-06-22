package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.SliderBridge;
import com.codename1.components.SpanLabel;
import com.hydrologis.gssmobile.utils.GssUtilities;
import static com.codename1.ui.CN.*;
import com.codename1.components.ToastBar;
import com.codename1.db.Database;
import com.codename1.io.Externalizable;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.ComponentGroup;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Label;
import com.codename1.ui.Slider;
import com.codename1.ui.Tabs;
import com.codename1.ui.TextComponent;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.TextModeLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.tree.Tree;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.Validator;
import com.codename1.util.Base64;
import com.hydrologis.cn1.libs.FileUtilities;
import com.hydrologis.cn1.libs.HyLog;
import com.hydrologis.cn1.libs.HyUtilities;
import com.hydrologis.cn1.libs.TimeUtilities;
import com.hydrologis.gssmobile.database.DaoGpsLogs;
import com.hydrologis.gssmobile.database.DaoImages;
import com.hydrologis.gssmobile.database.DaoNotes;
import com.hydrologis.gssmobile.database.GssGpsLog;
import com.hydrologis.gssmobile.database.GssImage;
import com.hydrologis.gssmobile.database.GssNote;
import com.hydrologis.gssmobile.utils.ServerUrlDialog;
import com.hydrologis.gssmobile.utils.UdidDialog;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GssMobile {

    private static final char IMAGES_ICON = FontImage.MATERIAL_IMAGE;
    private static final char LOGS_ICON = FontImage.MATERIAL_SHOW_CHART;
    private static final char NOTE_ICON = FontImage.MATERIAL_NOTE;
    private static final char FORM_NOTE_ICON = FontImage.MATERIAL_NOTE_ADD;

    private Form current;
    private Resources theme;

    private Form mainForm;
    private InfiniteContainer notesContainer = null;
    private InfiniteContainer gpsLogsContainer = null;
    private InfiniteContainer imagesContainer = null;
    private Database db = null;
    private Slider progressSlider;

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
            if (err.getError() != null) {
//                Log.e(err.getError());
            }
//            Log.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }

        mainForm = new Form("Sync List", new BorderLayout());

        Toolbar toolbar = mainForm.getToolbar();
        Image iconImage = theme.getImage("gss_launcher_icon.png");
        Container topBar = BorderLayout.east(new Label(iconImage));
        topBar.add(BorderLayout.SOUTH, new Label("Geopaparazzi Survey Server Sync", "SidemenuTagline"));
        topBar.setUIID("SideCommand");
        toolbar.addComponentToSideMenu(topBar);
        toolbar.addMaterialCommandToSideMenu("Select Project", FontImage.MATERIAL_SETTINGS, e -> {
            Dialog projectDialog = new Dialog("Select Geopaparazzi Project");
            projectDialog.setLayout(new BorderLayout());

            ComponentGroup cg = new ComponentGroup();
            try {
                String sdcard = FileUtilities.INSTANCE.getSdcard();
                HyLog.p("sdcard: " + sdcard);
                String[] listFiles = FileUtilities.INSTANCE.listFiles(sdcard);
                if (listFiles != null) {
                    HyLog.p("listFiles size: " + listFiles.length);
                    for (String listFile : listFiles) {
                        HyLog.p("-> " + listFile);
                    }
                }

                addGpapProjects(listFiles, projectDialog, cg);
                String dbPath = FileUtilities.INSTANCE.getSdcardFile("database");
                HyLog.p("database path from sdcard: " + dbPath);
                listFiles = FileUtilities.INSTANCE.listFiles(dbPath);
                if (listFiles != null) {
                    addGpapProjects(listFiles, projectDialog, cg);
                }

            } catch (IOException ex) {
                HyLog.e(ex);
            }

            projectDialog.add(BorderLayout.CENTER, cg);
            projectDialog.show();
        });
        toolbar.addMaterialCommandToSideMenu("Device Id", FontImage.MATERIAL_SECURITY, e -> {
            String udid = HyUtilities.getUdid();
            if (udid == null) {
                UdidDialog insertUdidDialog = new UdidDialog();
                insertUdidDialog.show();

                String newUdid = insertUdidDialog.getUdid();
                Preferences.set(HyUtilities.CUSTOM_UDID, newUdid);
            } else {
                Dialog.show("Unique device id", "UDID: " + udid, Dialog.TYPE_INFO, null, "OK", null);
            }
        });
        toolbar.addMaterialCommandToSideMenu("Server URL", FontImage.MATERIAL_CLOUD_CIRCLE, e -> {
            ServerUrlDialog serverUrlDialog = new ServerUrlDialog();
            serverUrlDialog.show();

            String serverUrl = serverUrlDialog.getServerUrl();
            Preferences.set(GssUtilities.SERVER_URL, serverUrl);
        });

        // TODO ask why it doesn't fill
        Label fillerLabel = new Label("", "sideMenuFillerLabel");
        toolbar.setComponentToSideMenuSouth(fillerLabel);

        try {
            Tabs t = new Tabs();
            t.setTabPlacement(Component.TOP);
            notesContainer = getNotesContainer();
            gpsLogsContainer = getGpsLogsContainer();
            imagesContainer = getImagesContainer();
            t.addTab("Notes", NOTE_ICON, 4, notesContainer);
            t.addTab("Gps Logs", LOGS_ICON, 4, gpsLogsContainer);
            t.addTab("Images", IMAGES_ICON, 4, imagesContainer);

            mainForm.add(BorderLayout.CENTER, t);

            FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_CLOUD_UPLOAD);
            fab.bindFabToContainer(mainForm.getContentPane());
            FloatingActionButton onlyNotesFab = fab.createSubFAB(NOTE_ICON, "Only Notes");
            onlyNotesFab.addActionListener(e -> upload(true, false, false));
            FloatingActionButton onlyLogsFab = fab.createSubFAB(LOGS_ICON, "Only Gps Logs");
            onlyLogsFab.addActionListener(e -> upload(false, true, false));
            FloatingActionButton onlyImagesFab = fab.createSubFAB(IMAGES_ICON, "Only Media");
            onlyImagesFab.addActionListener(e -> upload(false, false, true));
            FloatingActionButton loadAllFab = fab.createSubFAB(FontImage.MATERIAL_CLEAR_ALL, "Everything");
            loadAllFab.addActionListener(e -> upload(true, true, true));

            progressSlider = new Slider();
            mainForm.add(BorderLayout.SOUTH, progressSlider);
            mainForm.show();

            String lastDbPath = Preferences.get(GssUtilities.LAST_DB_PATH, "");
            if (lastDbPath.trim().length() == 0) {
                Dialog.show("No db chosen yet.", " Please use the side menu to choose it.", Dialog.TYPE_WARNING, null, "OK", null);
            }

            String sdcardFile = "geopaparazzi2.gpap"; //FileUtilities.INSTANCE.getSdcardFile("geopaparazzi.gpap");
            refreshData(sdcardFile);
        } catch (Exception ex) {
            HyLog.e(ex);
        }
    }

    private void upload(boolean doNotes, boolean doLogs, boolean doMedia) {
        try {
            if (db == null) {
                HyUtilities.showErrorDialog("No database connected.");
                return;
            }

            String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "");
            if (serverUrl.trim().length() == 0) {
                HyUtilities.showErrorDialog("No server url has been define. Please set the proper url from the side menu.");
                return;
            }

            // tODO handle pwd
            String password = "testpwd";

            String authCode = HyUtilities.getUdid() + ":" + password;
            String authHeader = "Basic " + Base64.encode(authCode.getBytes());
            MultipartRequest mpr = new MultipartRequest();
            mpr.setUrl(serverUrl);
            mpr.addRequestHeader("Authorization", authHeader);

            boolean oneAdded = false;

            if (doNotes) {
                List<GssNote> notesList = DaoNotes.getNotesList(db);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bout);
                for (GssNote gssNote : notesList) {
                    gssNote.externalize(out);
                }
                byte[] bytes = bout.toByteArray();
                mpr.addData(GssNote.OBJID, bytes, HyUtilities.MIMETYPE_BYTEARRAY);
                oneAdded = true;
            }
            if (doLogs) {
                List<GssGpsLog> logsList = DaoGpsLogs.getLogsList(db, true);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bout);
                for (GssGpsLog gssLog : logsList) {
                    gssLog.externalize(out);
                }
                byte[] bytes = bout.toByteArray();
                mpr.addData(GssGpsLog.OBJID, bytes, HyUtilities.MIMETYPE_BYTEARRAY);
                oneAdded = true;
            }

            SliderBridge.bindProgress(mpr, progressSlider);
            NetworkManager.getInstance().addToQueueAndWait(mpr);

            if (mpr.getResponseCode() == 200) {
                byte[] responseData = mpr.getResponseData();
            } else {
                String responseErrorMessage = mpr.getResponseErrorMessage();
                HyUtilities.showErrorDialog(responseErrorMessage);
            }
        } catch (Exception exception) {
            HyLog.e(exception);
        }

    }

    private void addGpapProjects(String[] listFiles, Dialog projectDialog, ComponentGroup cg) {
        for (String file : listFiles) {
            if (file.endsWith(".gpap")) {
                HyLog.p("Found: " + file);
                final Button pButton = new Button(file);
                pButton.setName(file);
                pButton.addActionListener(ev -> {
                    String dbName = pButton.getName();
                    try {
                        projectDialog.dispose();
                        refreshData(dbName);
                    } catch (IOException ex) {
                        HyLog.e(ex);
                    }
                });
                cg.add(pButton);
            }
        }
    }

    private void refreshData(String dbFile) throws IOException {
        if (db != null) {
            Util.cleanup(db);
        }
        String databasePath = Database.getDatabasePath(dbFile);
        boolean dbFileExists = Database.exists(dbFile);
        boolean databasePathExists = Database.exists(databasePath);
        HyLog.p("refreshData: " + dbFile + " exists = " + dbFileExists);
        HyLog.p("refreshData: " + databasePath + " exists = " + databasePathExists);
        HyLog.p("Open database: " + dbFile);
        //HyLog.p("Open database native: " + FileUtilities.INSTANCE.toNativePath(dbFile));
        db = Display.getInstance().openOrCreate(dbFile);
        Preferences.set(GssUtilities.LAST_DB_PATH, dbFile);
        notesContainer.refresh();
        gpsLogsContainer.refresh();
        imagesContainer.refresh();
    }

    public void stop() {
        current = getCurrentForm();
        if (current instanceof Dialog) {
            ((Dialog) current).dispose();
            current = getCurrentForm();
        }
        HyLog.sendLog();
    }

    public void destroy() {
    }

    private InfiniteContainer getNotesContainer() {
        FontImage simpleIcon = FontImage.createMaterial(NOTE_ICON, "SimpleNote", 4);
        FontImage formIcon = FontImage.createMaterial(FORM_NOTE_ICON, "ComplexNote", 4);
        InfiniteContainer ic = new InfiniteContainer() {
            private boolean stop = false;

            @Override
            public Component[] fetchComponents(int index, int amount) {
                if (stop) {
                    return null;
                }
                List<GssNote> notesList = new ArrayList<>();
                try {
                    if (db != null) {
                        notesList = DaoNotes.getNotesList(db);
                    }
                } catch (Exception ex) {
                    HyLog.e(ex);
                }

                final int size = notesList.size();
                Container[] cmps = new Container[size];
                for (int iter = 0; iter < cmps.length; iter++) {
                    GssNote note = notesList.get(iter);

                    String ts = TimeUtilities.toYYYYMMDDHHMMSS(note.timeStamp);
                    Label name = new Label(note.text);
                    Label tsLabel = new Label(ts);
                    tsLabel.setUIID("ItemsListRowSmall");

                    Label iconLabel = null;
                    if (note.form == null || note.form.length() == 0) {
                        iconLabel = new Label(simpleIcon);
                    } else {
                        iconLabel = new Label(formIcon);
                    }

                    Container c = BoxLayout.encloseX(iconLabel, BoxLayout.encloseY(name, tsLabel));
                    c.setUIID("ItemsListRow");
                    cmps[iter] = c;
                }
                stop = true;
                return cmps;
            }

            @Override
            public void refresh() {
                stop = false;
                super.refresh();
            }

        };

        return ic;
    }

    private InfiniteContainer getImagesContainer() {
        FontImage imageIcon = FontImage.createMaterial(IMAGES_ICON, "Image", 4);
        InfiniteContainer ic = new InfiniteContainer() {
            private boolean stop = false;

            @Override
            public Component[] fetchComponents(int index, int amount) {
                if (stop) {
                    return null;
                }

                List<GssImage> imagesList = new ArrayList<>();
                try {
                    if (db != null) {
                        imagesList = DaoImages.getImagesList(db);
                    }
                } catch (Exception ex) {
                    HyLog.e(ex);
                }

                Container[] cmps = new Container[imagesList.size()];
                for (int iter = 0; iter < cmps.length; iter++) {
                    GssImage image = imagesList.get(iter);

                    String ts = TimeUtilities.toYYYYMMDDHHMMSS(image.timeStamp.get());
                    Label name = new Label(image.text.get());
                    Label tsLabel = new Label(ts);
                    tsLabel.setUIID("ItemsListRowSmall");

                    Label iconLabel = new Label(imageIcon);

                    Container c = BoxLayout.encloseX(iconLabel, BoxLayout.encloseY(name, tsLabel));
                    c.setUIID("ItemsListRow");
                    cmps[iter] = c;
                }
                stop = true;
                return cmps;
            }

            @Override
            public void refresh() {
                stop = false;
                super.refresh();
            }
        };

        return ic;
    }

    private InfiniteContainer getGpsLogsContainer() {
        FontImage logsIcon = FontImage.createMaterial(LOGS_ICON, "GpsLogs", 4);
        InfiniteContainer ic = new InfiniteContainer() {
            private boolean stop = false;

            @Override
            public Component[] fetchComponents(int index, int amount) {
                if (stop) {
                    return null;
                }

                List<GssGpsLog> logsList = new ArrayList<>();
                try {
                    if (db != null) {
                        logsList = DaoGpsLogs.getLogsList(db, false);
                    }
                } catch (Exception ex) {
                    HyLog.e(ex);
                }

                Container[] cmps = new Container[logsList.size()];
                for (int iter = 0; iter < cmps.length; iter++) {
                    GssGpsLog log = logsList.get(iter);

                    String startts = TimeUtilities.toYYYYMMDDHHMMSS(log.startts);
                    String endts = TimeUtilities.toYYYYMMDDHHMMSS(log.endts);

                    Label name = new Label(log.name);
                    Label tsLabel = new Label(startts + "  ->  " + endts);
                    tsLabel.setUIID("ItemsListRowSmall");

                    Label iconLabel = new Label(logsIcon);

                    Container c = BoxLayout.encloseX(iconLabel, BoxLayout.encloseY(name, tsLabel));
                    c.setUIID("ItemsListRow");
                    cmps[iter] = c;
                }
                stop = true;
                return cmps;
            }

            @Override
            public void refresh() {
                stop = false;
                super.refresh();
            }
        };

        return ic;
    }

}
