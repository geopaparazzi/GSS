package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ToastBar;
import com.hydrologis.gssmobile.utils.GssUtilities;
import static com.codename1.ui.CN.*;
import com.codename1.db.Database;
import com.codename1.io.File;
import com.codename1.io.JSONParser;
import com.codename1.io.MultipartRequest;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.ComponentGroup;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Label;
import com.codename1.ui.Tabs;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.tree.Tree;
import com.codename1.util.Base64;
import com.hydrologis.cn1.libs.FileUtilities;
import com.hydrologis.cn1.libs.HyDialogs;
import com.hydrologis.cn1.libs.HyLog;
import com.hydrologis.cn1.libs.HyNativeUtils;
import com.hydrologis.cn1.libs.HyUploadProgressForm;
import com.hydrologis.cn1.libs.HyUtilities;
import com.hydrologis.cn1.libs.TimeUtilities;
import com.hydrologis.gssmobile.database.DaoGpsLogs;
import com.hydrologis.gssmobile.database.DaoImages;
import com.hydrologis.gssmobile.database.DaoNotes;
import com.hydrologis.gssmobile.database.GssGpsLog;
import com.hydrologis.gssmobile.database.GssImage;
import com.hydrologis.gssmobile.database.GssNote;
import com.hydrologis.gssmobile.utils.ServerUrlDialog;
import com.hydrologis.gssmobile.utils.SettingsDialog;
import com.hydrologis.gssmobile.utils.UdidDialog;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GssMobile {

    private static final String MASTER_GSS_PASSWORD = "gss_Master_Survey_Forever_2018";

    private static final int MPR_TIMEOUT = 5 * 60 * 1000; // 5 minutes timeout

    private static final char IMAGES_ICON = FontImage.MATERIAL_IMAGE;
    private static final char LOGS_ICON = FontImage.MATERIAL_TIMELINE;
    private static final char NOTE_ICON = FontImage.MATERIAL_NOTE;
    private static final char FORM_NOTE_ICON = FontImage.MATERIAL_NOTE_ADD;

    private Form current;
    private Resources theme;

    private Form mainForm;
    private InfiniteContainer notesContainer = null;
    private InfiniteContainer gpsLogsContainer = null;
    private InfiniteContainer imagesContainer = null;
    private Database db = null;
    private Label loadedDbLabel;
    private Label loadedDbParentLabel;
    private HyUploadProgressForm up;
    private LinkedList<MultipartRequest> requestsList;
    private int runninCount = 0;
    private int totalCount = 0;

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

        mainForm = new Form("Sync List", new BorderLayout());

        Toolbar toolbar = mainForm.getToolbar();
        Image iconImage = theme.getImage("gss_launcher_icon.png");
        Container topBar = BorderLayout.east(new Label(iconImage));
        topBar.add(BorderLayout.SOUTH, new Label("Geopaparazzi Survey Server Sync", "SidemenuTagline"));
        topBar.setUIID("SideCommand");
        toolbar.addComponentToSideMenu(topBar);
        toolbar.addMaterialCommandToSideMenu("Select Project", FontImage.MATERIAL_ASSIGNMENT, e -> {
//            boolean useNativeBrowser = Preferences.get(GssUtilities.NATIVE_BROWSER_USE, true);
//            if (FileChooser.isAvailable() && useNativeBrowser) {
//                FileChooser.showOpenDialog(".gpap", e2 -> {
//                    if (e2 == null) {
//                        return;
//                    }
//                    String file = (String) e2.getSource();
//                    if (file != null) {
//                        HyLog.p("Selected file: " + file);
//                        try {
//                            refreshData(file);
//                        } catch (IOException ex) {
//                            HyLog.e(ex);
//                        }
//                    }
//                });
//            } else {

            Form projectForm = new Form("Select Geopaparazzi Project", BoxLayout.y());
            ComponentGroup cg = new ComponentGroup();
            try {
                String sdcard = FileUtilities.INSTANCE.getSdcard();
                HyLog.p("sdcard: " + sdcard);
                List<String> gpapFiles = FileUtilities.INSTANCE.findFilesByExtension(sdcard, ".gpap");
                addGpapProjects(gpapFiles, cg);
            } catch (IOException ex) {
                HyLog.e(ex);
            }
            projectForm.add(cg);
            projectForm.show();
//
//            }

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
        toolbar.addMaterialCommandToSideMenu("Settings", FontImage.MATERIAL_SETTINGS, e -> {
            if (db == null) {

            } else {
                SettingsDialog settingsDialog = new SettingsDialog(db);
                settingsDialog.show();

                refreshContainers();
            }
        });
        toolbar.addMaterialCommandToSideMenu("Send debug log", FontImage.MATERIAL_SEND, e -> {
            HyLog.sendLog();
        });

        Container sideMenuSouthContainer = new Container(new BorderLayout(), "sideMenuFillerContainer");
        loadedDbLabel = new Label("No database connected.", "sideMenuFillerLabel");
        loadedDbLabel.setAutoSizeMode(true);
        loadedDbParentLabel = new Label("No database connected.", "sideMenuFillerLabel");
        loadedDbParentLabel.setAutoSizeMode(true);
        loadedDbParentLabel.setVisible(false);
        sideMenuSouthContainer.add(BorderLayout.NORTH, loadedDbLabel);
        sideMenuSouthContainer.add(BorderLayout.SOUTH, loadedDbParentLabel);

        toolbar.setComponentToSideMenuSouth(sideMenuSouthContainer);

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
            onlyNotesFab.addActionListener(e -> uploadWithProgress(true, false, false));
            FloatingActionButton onlyLogsFab = fab.createSubFAB(LOGS_ICON, "Only Gps Logs");
            onlyLogsFab.addActionListener(e -> uploadWithProgress(false, true, false));
            FloatingActionButton onlyImagesFab = fab.createSubFAB(IMAGES_ICON, "Only Media");
            onlyImagesFab.addActionListener(e -> uploadWithProgress(false, false, true));
            FloatingActionButton loadAllFab = fab.createSubFAB(FontImage.MATERIAL_CLEAR_ALL, "Everything");
            loadAllFab.addActionListener(e -> uploadWithProgress(true, true, true));

            mainForm.show();

            String lastDbPath = Preferences.get(GssUtilities.LAST_DB_PATH, "");
            HyLog.p("Db found in preferences: " + lastDbPath);
            final boolean dbExists = Database.exists(lastDbPath);
            if (lastDbPath.trim().length() == 0 || !dbExists) {
                callSerially(() -> {
                    HyDialogs.showWarningDialog("No db chosen yet.", " Please use the side menu to choose it.");
                });
            } else if (dbExists) {
                refreshData(lastDbPath);
            }
        } catch (Exception ex) {
            HyLog.e(ex);
        }
    }

    private void uploadWithProgress(boolean doNotes, boolean doLogs, boolean doMedia) {
        if (db == null) {
            HyDialogs.showErrorDialog("No database connected.");
            return;
        }

        String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "");
        if (serverUrl.trim().length() == 0) {
            HyDialogs.showErrorDialog("No server url has been define. Please set the proper url from the side menu.");
            return;
        }

        up = new HyUploadProgressForm(getCurrentForm(), "Upload data...", false);
        callSerially(() -> {
            up.show();

            final Label label = new Label("Gathering data...", "uploadProgressLabel");
            addProgressLabelAndRefresh(label);
        });

        /*
         * gather data
         */
        requestsList = new LinkedList<>();
        runninCount = 1;
        try {
            int count = 1;
            int index = 0; // 0 notes, 1 logs, 2 both
            if (doNotes && doLogs) {
                index = 2;
            } else if (doNotes) {
                index = 0;
            } else {
                index = 1;
            }
            String authCode = HyUtilities.getUdid() + ":" + MASTER_GSS_PASSWORD;
            String authHeader = "Basic " + Base64.encode(authCode.getBytes());
            if (doNotes || doLogs) {
                boolean oneAdded = false;
                MultipartRequest mpr = getMpr(index, db, -1);
                mpr.setUrl(serverUrl);
                mpr.addRequestHeader("Authorization", authHeader);
                if (doNotes) {
                    // simple notes
                    List<GssNote> notesList = DaoNotes.getSimpleNotesList(db);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(bout);
                    for (GssNote gssNote : notesList) {
                        gssNote.externalize(out);
                        oneAdded = true;
                    }
                    byte[] bytes = bout.toByteArray();
//                    addProgressLabelAndRefresh(new Label("Found simple notes: " + notesList.size(), "uploadProgressLabel"));
                    mpr.addData(GssNote.OBJID, bytes, HyUtilities.MIMETYPE_BYTEARRAY);
                }
                if (doLogs) {
                    List<GssGpsLog> logsList = DaoGpsLogs.getLogsList(db, true);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(bout);
                    for (GssGpsLog gssLog : logsList) {
                        gssLog.externalize(out);
                        oneAdded = true;
                    }
                    byte[] bytes = bout.toByteArray();
//                    addProgressLabelAndRefresh(new Label("Found logs: " + logsList.size(), "uploadProgressLabel"));
                    mpr.addData(GssGpsLog.OBJID, bytes, HyUtilities.MIMETYPE_BYTEARRAY);
                }
                if (oneAdded) {
                    requestsList.add(mpr);
                }
            }

            // NOW FORMS WITH IMAGES
            if (doNotes) {
                index = 3;
                List<GssNote> notesList = DaoNotes.getFormNotesList(db);
//                addProgressLabelAndRefresh(new Label("Found forms: " + notesList.size(), "uploadProgressLabel"));
                for (GssNote gssNote : notesList) {
                    MultipartRequest mpr = getMpr(index, db, gssNote.id);
                    mpr.setUrl(serverUrl);
                    mpr.addRequestHeader("Authorization", authHeader);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(bout);
                    gssNote.externalize(out);
                    byte[] bytes = bout.toByteArray();
                    mpr.addData(GssNote.OBJID, bytes, HyUtilities.MIMETYPE_BYTEARRAY);

                    List<GssImage> imagesForNote = DaoImages.getImagesListForNoteId(db, gssNote.id, true);
//                    addProgressLabelAndRefresh(new Label("Getting images for note: " + imagesForNote.size(), "uploadProgressLabel"));
                    for (GssImage gssImage : imagesForNote) {
                        if (gssImage.data == null) {
                            final Label label = new Label("Found image without data attached: " + gssImage.text, "uploadProgressErrorLabel");
                            addProgressLabelAndRefresh(label);
                            continue;
                        }
                        ByteArrayOutputStream bout1 = new ByteArrayOutputStream();
                        DataOutputStream out1 = new DataOutputStream(bout1);
                        gssImage.externalize(out1);
                        byte[] bytes1 = bout1.toByteArray();
                        mpr.addData(GssImage.OBJID + (count++), bytes1, HyUtilities.MIMETYPE_BYTEARRAY);
                    }
                    requestsList.add(mpr);
                }

            }
            // DO IMAGES
            if (doMedia) {
                index = 4;
                List<GssImage> imagesList = DaoImages.getLonelyImagesList(db, true);
//                addProgressLabelAndRefresh(new Label("Found images: " + imagesList.size(), "uploadProgressLabel"));
                for (GssImage gssImage : imagesList) {
                    if (gssImage.data == null) {
                        final Label label = new Label("Found image without data attached: " + gssImage.text, "uploadProgressErrorLabel");
                        addProgressLabelAndRefresh(label);
                        continue;
                    }
                    MultipartRequest mpr = getMpr(index, db, gssImage.id);
                    mpr.setUrl(serverUrl);
                    mpr.addRequestHeader("Authorization", authHeader);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(bout);
                    gssImage.externalize(out);
                    byte[] bytes = bout.toByteArray();
                    mpr.addData(GssImage.OBJID + (count++), bytes, HyUtilities.MIMETYPE_BYTEARRAY);
                    requestsList.add(mpr);
                }
            }

            if (requestsList.isEmpty()) {
                HyDialogs.showWarningDialog("No data to upload.");
            } else {
                totalCount = requestsList.size();
                callSerially(() -> {
                    final Label label = new Label("Starting data upload...", "uploadProgressLabel");
                    addProgressLabelAndRefresh(label);
                    runNextInList();
                });
            }

        } catch (Exception exception) {
            HyDialogs.showErrorDialog(exception.getMessage());
            HyLog.e(exception);
            mainForm.show();
        }

    }

    private void addGpapProjects(List<String> gpapFiles, ComponentGroup cg) {
        for (String file : gpapFiles) {
            HyLog.p("Found: " + file);
            File gpapFile = new File(file);
            final Button pButton = new Button(gpapFile.getName());
            pButton.setName(file);
            pButton.addActionListener(ev -> {
                String dbPath = pButton.getName();
                try {
                    refreshData(dbPath);
                    mainForm.show();
                } catch (IOException ex) {
                    HyLog.e(ex);
                }
            });
            cg.add(pButton);
        }
    }

    private void refreshData(String dbPath) throws IOException {
        if (db != null) {
            Util.cleanup(db);
        }
        final File dbFile = new File(dbPath);
        final String dbName = dbFile.getName();
        String parentPath = dbFile.getParentFile().getAbsolutePath();

        HyLog.p("Opening database: " + dbPath + " that exists: " + Database.exists(dbPath));

        try {
            db = Display.getInstance().openOrCreate(dbPath);

            if (parentPath.startsWith("file")) {
                parentPath = parentPath.substring(7);
            }

            String _pPath = parentPath;

            loadedDbParentLabel.setText("Path: " + _pPath);
            loadedDbParentLabel.setVisible(true);
            loadedDbLabel.setText("Name: " + dbName);

            ToastBar.showInfoMessage("Loading database: " + dbName);
        } catch (Exception e) {
            HyLog.e(e);
            String errMsg = e.getMessage();
            if (errMsg.toLowerCase().contains("in read/write mode")) {
                HyDialogs.showInfoDialog("An error occurred while opening: " + dbPath + " It is inside a supported folder?");
            } else {
                HyDialogs.showErrorDialog("An error occurred while opening the selected database: " + errMsg);
            }
            return;
        }

        Preferences.set(GssUtilities.LAST_DB_PATH, dbPath);
        refreshContainers();
    }

    private void refreshContainers() {
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
                        notesList = DaoNotes.getAllNotesList(db);
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
                        imagesList = DaoImages.getImagesList(db, false);
                    }
                } catch (Exception ex) {
                    HyLog.e(ex);
                }

                Container[] cmps = new Container[imagesList.size()];
                for (int iter = 0; iter < cmps.length; iter++) {
                    GssImage image = imagesList.get(iter);

                    String ts = TimeUtilities.toYYYYMMDDHHMMSS(image.timeStamp);
                    Label name = new Label(image.text);
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

    private MultipartRequest getMpr(final int index, Database db, long itemId) {
        MultipartRequest mpr = new MultipartRequest() {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                JSONParser jp = new JSONParser();
                Map<String, Object> responseMap = jp.parseJSON(new InputStreamReader(input, "UTF-8"));
                Object statusCode = responseMap.get("code");
                if (statusCode instanceof Number) {
                    int status = ((Number) statusCode).intValue();
                    HyLog.p("Response status code: " + status);
                    if (status != 200) {
                        callSerially(() -> {
                            final Label label = new Label(responseMap.get("trace").toString(), "uploadProgressErrorLabel");
                            addProgressLabelAndRefresh(label);
                        });
                        return;
                    }
                }
                callSerially(() -> {
                    try {
                        final Label label = new Label("Done", "uploadProgressLabel");
                        addProgressLabelAndRefresh(label);

                        switch (index) {
                            case 0:
                                DaoNotes.clearDirtySimple(db);
                                break;
                            case 1:
                                DaoGpsLogs.clearDirty(db);
                                break;
                            case 2:
                                DaoNotes.clearDirtySimple(db);
                                DaoGpsLogs.clearDirty(db);
                                break;
                            case 3:
                                DaoNotes.clearDirtyById(db, itemId);
                                DaoImages.clearDirtyByNoteId(db, itemId);
                                break;
                            case 4:
                                DaoImages.clearDirtyById(db, itemId);
                                break;
                            default:
                                break;
                        }

                        runNextInList();
                    } catch (IOException ex) {
                        HyLog.e(ex);
                    }
                });
            }

        };
        mpr.setTimeout(MPR_TIMEOUT);
        return mpr;
    }

    private void runNextInList() {
        if (!requestsList.isEmpty()) {
            MultipartRequest mpr = requestsList.pop();
            up.upload("Uploading chunk " + runninCount + " of " + totalCount, mpr);
            runninCount++;
        } else {
            HyDialogs.showInfoDialog("Data uploaded");
            up.dismiss();
            mainForm.show();
            refreshContainers();
        }

    }

    private synchronized void addProgressLabelAndRefresh(final Label label) {
        up.add(label);
        up.revalidate();
        up.scrollComponentToVisible(label);
    }
}
