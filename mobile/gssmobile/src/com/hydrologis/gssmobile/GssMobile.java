package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.hydrologis.gssmobile.utils.GssUtilities;
import static com.codename1.ui.CN.*;
import com.codename1.components.ToastBar;
import com.codename1.db.Database;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.io.Preferences;
import com.codename1.ui.ButtonGroup;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Label;
import com.codename1.ui.RadioButton;
import com.codename1.ui.Tabs;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.DefaultLookAndFeel;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.tree.Tree;
import com.hydrologis.cn1.libs.HyLog;
import com.hydrologis.cn1.libs.TimeUtilities;
import com.hydrologis.gssmobile.database.DaoGpsLogs;
import com.hydrologis.gssmobile.database.DaoImages;
import com.hydrologis.gssmobile.database.DaoNotes;
import com.hydrologis.gssmobile.database.GssGpsLog;
import com.hydrologis.gssmobile.database.GssImage;
import com.hydrologis.gssmobile.database.GssNote;
import com.hydrologis.gssmobile.utils.SyncData;
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
//        Log.bindCrashProtection(true);
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
            // TODO 
        });

        // TODO ask why it doesn't fill
        Label fillerLabel = new Label("", "sideMenuFillerLabel");
        toolbar.setComponentToSideMenuSouth(fillerLabel);

        String lastDbPath = Preferences.get(GssUtilities.LAST_DB_PATH, "");
        if (lastDbPath.trim().length() == 0) {
            Dialog.show("No db chosen yet.", " Please use the side menu to choose it.", Dialog.TYPE_WARNING, null, "OK", null);
        }

        String sdcardFile = "geopaparazzi2.gpap"; //FileUtilities.INSTANCE.getSdcardFile("geopaparazzi.gpap");
        try {
            Database db = Display.getInstance().openOrCreate(sdcardFile);
            Preferences.set(GssUtilities.LAST_DB_PATH, sdcardFile);

            Tabs t = new Tabs();
            t.setTabPlacement(Component.TOP);
            Container notesContainer = getNotesContainer(db);
            Container gpsLogsContainer = getGpsLogsContainer(db);
            Container imagesContainer = getImagesContainer(db);
            t.addTab("Notes", NOTE_ICON, 4, notesContainer);
            t.addTab("Gps Logs", LOGS_ICON, 4, gpsLogsContainer);
            t.addTab("Images", IMAGES_ICON, 4, imagesContainer);

            mainForm.add(BorderLayout.CENTER, t);

            FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_CLOUD_UPLOAD);
            fab.bindFabToContainer(mainForm.getContentPane());
            FloatingActionButton onlyNotesFab = fab.createSubFAB(NOTE_ICON, "Only Notes");
            onlyNotesFab.addActionListener(e -> ToastBar.showErrorMessage("Not yet here"));
            FloatingActionButton onlyLogsFab = fab.createSubFAB(LOGS_ICON, "Only Gps Logs");
            onlyLogsFab.addActionListener(e -> ToastBar.showErrorMessage("Not yet here"));
            FloatingActionButton onlyImagesFab = fab.createSubFAB(IMAGES_ICON, "Only Media");
            onlyImagesFab.addActionListener(e -> ToastBar.showErrorMessage("Not yet here"));
            FloatingActionButton loadAllFab = fab.createSubFAB(FontImage.MATERIAL_CLEAR_ALL, "Everything");
            loadAllFab.addActionListener(e -> ToastBar.showErrorMessage("Not yet here"));
            mainForm.show();

        } catch (Exception ex) {
            HyLog.e(ex);
        }
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

    private Container getNotesContainer(Database db) {
        FontImage simpleIcon = FontImage.createMaterial(NOTE_ICON, "SimpleNote", 4);
        FontImage formIcon = FontImage.createMaterial(FORM_NOTE_ICON, "ComplexNote", 4);
        InfiniteContainer ic = new InfiniteContainer() {
            @Override
            public Component[] fetchComponents(int index, int amount) {

                List<GssNote> notesList = new ArrayList<>();
                try {
                    notesList = DaoNotes.getNotesList(db);
                } catch (Exception ex) {
                    HyLog.e(ex);
                }

                Container[] cmps = new Container[notesList.size()];
                for (int iter = 0; iter < cmps.length; iter++) {
                    GssNote note = notesList.get(iter);

                    String ts = TimeUtilities.toYYYYMMDDHHMMSS(note.timeStamp.get());
                    Label name = new Label(note.text.get());
                    Label tsLabel = new Label(ts);
                    tsLabel.setUIID("ItemsListRowSmall");

                    Label iconLabel = null;
                    if (note.form.get() == null || note.form.get().length() == 0) {
                        iconLabel = new Label(simpleIcon);
                    } else {
                        iconLabel = new Label(formIcon);
                    }

                    Container c = BoxLayout.encloseX(iconLabel, BoxLayout.encloseY(name, tsLabel));
                    c.setUIID("ItemsListRow");
                    cmps[iter] = c;
                }
                return cmps;
            }
        };

        return ic;
    }

    private Container getImagesContainer(Database db) {
        FontImage imageIcon = FontImage.createMaterial(IMAGES_ICON, "Image", 4);
        InfiniteContainer ic = new InfiniteContainer() {
            @Override
            public Component[] fetchComponents(int index, int amount) {

                List<GssImage> imagesList = new ArrayList<>();
                try {
                    imagesList = DaoImages.getImagesList(db);
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
                return cmps;
            }
        };

        return ic;
    }

    private Container getGpsLogsContainer(Database db) {
        FontImage logsIcon = FontImage.createMaterial(LOGS_ICON, "GpsLogs", 4);
        InfiniteContainer ic = new InfiniteContainer() {
            @Override
            public Component[] fetchComponents(int index, int amount) {

                List<GssGpsLog> logsList = new ArrayList<>();
                try {
                    logsList = DaoGpsLogs.getLogsList(db);
                } catch (Exception ex) {
                    HyLog.e(ex);
                }

                Container[] cmps = new Container[logsList.size()];
                for (int iter = 0; iter < cmps.length; iter++) {
                    GssGpsLog log = logsList.get(iter);

                    String startts = TimeUtilities.toYYYYMMDDHHMMSS(log.startts.get());
                    String endts = TimeUtilities.toYYYYMMDDHHMMSS(log.endts.get());

                    Label name = new Label(log.name.get());
                    Label tsLabel = new Label(startts + "  ->  " + endts);
                    tsLabel.setUIID("ItemsListRowSmall");

                    Label iconLabel = new Label(logsIcon);

                    Container c = BoxLayout.encloseX(iconLabel, BoxLayout.encloseY(name, tsLabel));
                    c.setUIID("ItemsListRow");
                    cmps[iter] = c;
                }
                return cmps;
            }
        };

        return ic;
    }

}
