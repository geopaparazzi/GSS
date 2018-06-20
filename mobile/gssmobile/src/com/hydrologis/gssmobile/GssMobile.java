package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.hydrologis.gssmobile.utils.GssUtilities;
import static com.codename1.ui.CN.*;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.db.Database;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.io.Preferences;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.tree.Tree;
import com.codename1.ui.tree.TreeModel;
import com.hydrologis.cn1.libs.FileUtilities;
import com.hydrologis.gssmobile.database.DaoNotes;
import com.hydrologis.gssmobile.database.Notes;
import com.hydrologis.gssmobile.utils.SyncData;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class GssMobile {

    private Form current;
    private Resources theme;

    private Form mainForm;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

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

        SyncData syncData = new SyncData();
        String sdcardFile = "geopaparazzi.gpap"; //FileUtilities.INSTANCE.getSdcardFile("geopaparazzi.gpap");
        try {
            Database db = Display.getInstance().openOrCreate(sdcardFile);
            List<Notes> notesList = DaoNotes.getNotesList(db);
            syncData.type2ListMap.put(SyncData.NOTES, notesList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // TREE
        TreeModel tm = new TreeModel() {
            @Override
            public Vector getChildren(Object parent) {
                if (parent == null) {
                    return new Vector(syncData.rootsList);
                } else {
                    List<?> list = syncData.type2ListMap.get(parent);
                    if (list != null) {
                        return new Vector(list);
                    }
                    return new Vector();
                }
            }

            @Override
            public boolean isLeaf(Object node) {
                return node instanceof Notes;
            }
        };
        Tree t = new Tree(tm) {
            @Override
            protected String childToDisplayLabel(Object child) {
                if (child instanceof String) {
                    return (String) child;
                } else if (child instanceof Notes) {
                    Notes note = (Notes) child;
                    String label = note.id.get() + ")" + note.text.get();
                    return label;
                }
                return " - nv - ";
            }
        };
        //t.setNodeIcon(FontImage.createMaterial(FontImage.MATERIAL_NAVIGATE_NEXT, "Node", 4));

        mainForm.add(BorderLayout.CENTER, t);

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_CLOUD_UPLOAD);
        fab.bindFabToContainer(mainForm.getContentPane());
        FloatingActionButton onlyNotesFab = fab.createSubFAB(FontImage.MATERIAL_NOTE, "Only Notes");
        onlyNotesFab.addActionListener(e -> ToastBar.showErrorMessage("Not yet here"));
        FloatingActionButton loadAllFab = fab.createSubFAB(FontImage.MATERIAL_CLEAR_ALL, "Everything");
        loadAllFab.addActionListener(e -> ToastBar.showErrorMessage("Not yet here"));

        mainForm.show();
    }

    private void showOKForm(String name) {
        Form f = new Form("Thanks", BoxLayout.y());
        f.add(new SpanLabel("Thanks " + name + " for your submission. You can press the back arrow and try again"));
        f.getToolbar().setBackCommand("", e -> mainForm.showBack());
        f.show();
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
