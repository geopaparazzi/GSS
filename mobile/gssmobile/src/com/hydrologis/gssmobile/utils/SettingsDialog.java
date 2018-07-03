/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.utils;

import com.codename1.components.SpanLabel;
import com.codename1.db.Database;
import com.codename1.ui.Button;
import com.codename1.ui.ComponentGroup;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.hydrologis.cn1.libs.HyLog;
import com.hydrologis.cn1.libs.HyUtilities;
import com.hydrologis.gssmobile.database.DaoGpsLogs;
import com.hydrologis.gssmobile.database.DaoImages;
import com.hydrologis.gssmobile.database.DaoNotes;
import java.io.IOException;

/**
 *
 * @author hydrologis
 */
public class SettingsDialog extends Dialog {

    private Container nativeBrowserContainer = null;

    public SettingsDialog(final Database db) {

        super("Settings");

        setLayout(BoxLayout.y());

//        if (FileChooser.isAvailable()) {
//            CheckBox fileBrowserSwitch = CheckBox.createToggle(FontImage.createMaterial(FontImage.MATERIAL_FOLDER, "native_browse", GssUtilities.BIG_ICON_SIZE));
//
//            boolean useNativeBrowser = Preferences.get(GssUtilities.NATIVE_BROWSER_USE, true);
//            fileBrowserSwitch.setSelected(useNativeBrowser);
//            fileBrowserSwitch.addActionListener(e -> {
//                Preferences.set(GssUtilities.NATIVE_BROWSER_USE, fileBrowserSwitch.isSelected());
//            });
//
//            SpanLabel browserLabel = new SpanLabel("Use native file browser instead of triggering a simple project lookup on the first 2 folder levels.");
//            
//            nativeBrowserContainer = new Container(new BorderLayout());
//            nativeBrowserContainer.add(BorderLayout.WEST, fileBrowserSwitch);
//            nativeBrowserContainer.add(BorderLayout.CENTER, browserLabel);
//        }
        Button resetDirtyButton = new Button(FontImage.createMaterial(FontImage.MATERIAL_SETTINGS_BACKUP_RESTORE, "restore_dirty", GssUtilities.BIG_ICON_SIZE));
        resetDirtyButton.addActionListener(e -> {
            if (Dialog.show("Are you sure?", "This can't be undone!", GssUtilities.YES, GssUtilities.NO)) {
                try {
                    DaoNotes.makeDirty(db);
                    DaoGpsLogs.makeDirty(db);
                    DaoImages.makeDirty(db);
                    dispose();
                } catch (IOException ex) {
                    HyLog.e(ex);
                    HyUtilities.showErrorDialog(ex.getMessage());
                }
            }
        });
        SpanLabel resetDirtyLabel = new SpanLabel("Reset the database to dirty status?\n(Warning, this will result in all the data being reloaded.)");
        Container resetDirtyContainer = new Container(new BorderLayout());
        resetDirtyContainer.add(BorderLayout.WEST, resetDirtyButton);
        resetDirtyContainer.add(BorderLayout.CENTER, resetDirtyLabel);

        SpanLabel resetCleanLabel = new SpanLabel("Update the database to clean status?\n(Warning, this will result in no data to be loaded.)");
        Button resetCleanButton = new Button(FontImage.createMaterial(FontImage.MATERIAL_CLEAR_ALL, "restore_clean", GssUtilities.BIG_ICON_SIZE));
        resetCleanButton.addActionListener(e -> {
            if (Dialog.show("Are you sure?", "This can't be undone!", GssUtilities.YES, GssUtilities.NO)) {
                try {
                    DaoNotes.clearDirty(db);
                    DaoGpsLogs.clearDirty(db);
                    DaoImages.clearDirty(db);
                    dispose();
                } catch (IOException ex) {
                    HyLog.e(ex);
                    HyUtilities.showErrorDialog(ex.getMessage());
                }
            }
        });

        Container resetCleanContainer = new Container(new BorderLayout());
        resetCleanContainer.add(BorderLayout.WEST, resetCleanButton);
        resetCleanContainer.add(BorderLayout.CENTER, resetCleanLabel);

        ComponentGroup group = null;
        if (nativeBrowserContainer == null) {
            group = ComponentGroup.enclose(resetDirtyContainer, resetCleanContainer);
        } else {
            group = ComponentGroup.enclose(nativeBrowserContainer, resetDirtyContainer, resetCleanContainer);
        }

        add(group);

        setDisposeWhenPointerOutOfBounds(true);

    }

}
