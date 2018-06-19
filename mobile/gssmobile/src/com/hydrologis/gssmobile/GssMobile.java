package com.hydrologis.gssmobile;

import com.codename1.components.FloatingActionButton;
import com.hydrologis.gssmobile.utils.GssUtilities;
import static com.codename1.ui.CN.*;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.io.Preferences;
import com.codename1.ui.tree.Tree;
import com.codename1.ui.tree.TreeModel;
import com.hydrologis.gssmobile.utils.SyncData;
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

        String lastDbPath = Preferences.get(GssUtilities.LAST_DB_PATH, "");
        if (lastDbPath.trim().length() == 0) {
            Dialog.show("No db chosen yet.", " Please use the side menu to choose it.", Dialog.TYPE_WARNING, null, "OK", null);
        }

        SyncData syncData = new SyncData();
        List<Object> rootsList = Arrays.asList("Notes", "Gps Logs", "Media");

        // TREE
        TreeModel tm = new TreeModel() {
            @Override
            public Vector getChildren(Object parent) {
                if (parent == null) {
                    return new Vector(rootsList);
                } else {
                    return new Vector();
                }
            }

            @Override
            public boolean isLeaf(Object node) {
                String nodeStr = (String) node;
                return !rootsList.contains(nodeStr);
            }
        };
        Tree t = new Tree(tm) {
            @Override
            protected String childToDisplayLabel(Object child) {
                String n = (String) child;
                int pos = n.lastIndexOf("/");
                if (pos < 0) {
                    return n;
                }
                return n.substring(pos);
            }
        };
        //t.setNodeIcon(FontImage.createMaterial(FontImage.MATERIAL_NAVIGATE_NEXT, "Node", 4));

        mainForm.add(BorderLayout.CENTER, t);
        
        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_CLOUD_UPLOAD);
        fab.bindFabToContainer(mainForm.getContentPane());
        FloatingActionButton onlyNotesFab = fab.createSubFAB(FontImage.MATERIAL_NOTE, "Only Notes");
        onlyNotesFab.addActionListener(e-> ToastBar.showErrorMessage("Not yet here"));
        FloatingActionButton loadAllFab = fab.createSubFAB(FontImage.MATERIAL_CLEAR_ALL, "Everything");
        loadAllFab.addActionListener(e-> ToastBar.showErrorMessage("Not yet here"));
        
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
