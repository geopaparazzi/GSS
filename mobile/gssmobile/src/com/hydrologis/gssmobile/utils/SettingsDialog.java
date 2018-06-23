/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.utils;

import com.codename1.components.OnOffSwitch;
import com.codename1.components.SpanLabel;
import com.codename1.db.Database;
import com.codename1.ext.filechooser.FileChooser;
import com.codename1.io.Preferences;
import com.codename1.ui.Button;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
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

    public SettingsDialog(final Database db) {

        super("Settings");

        setLayout(BoxLayout.y());

        SpanLabel fileBrowserLabel = new SpanLabel("Use native file browser");
        OnOffSwitch fileBrowserSwitch = new OnOffSwitch();
        add(fileBrowserLabel);
        add(fileBrowserSwitch);
        if (FileChooser.isAvailable()) {
            boolean useNativeBrowser = Preferences.get(GssUtilities.NATIVE_BROWSER_USE, false);
            fileBrowserSwitch.setValue(useNativeBrowser);
            fileBrowserSwitch.setNoTextMode(true);
            fileBrowserSwitch.addActionListener(e -> {
                Preferences.set(GssUtilities.NATIVE_BROWSER_USE, fileBrowserSwitch.isValue());
            });
        } else {
            fileBrowserSwitch.setValue(false);
            fileBrowserSwitch.setEnabled(false);
        }
        Label grayLabel = new Label();
        grayLabel.setShowEvenIfBlank(true);
        grayLabel.getUnselectedStyle().setBgColor(0xcccccc);
        grayLabel.getUnselectedStyle().setPadding(1, 1, 1, 1);
        grayLabel.getUnselectedStyle().setPaddingUnit(Style.UNIT_TYPE_PIXELS);
        add(grayLabel);

        SpanLabel resetDirtyLabel = new SpanLabel("Reset the database to dirty status?\n(Warning, this will result in all the data being reloaded.)");
        Button resetDirtyButton = new Button(FontImage.createMaterial(FontImage.MATERIAL_SETTINGS_BACKUP_RESTORE, "restore_dirty", GssUtilities.ICON_SIZE));
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

        add(resetDirtyLabel);
        add(resetDirtyButton);

        SpanLabel resetCleanLabel = new SpanLabel("Update the database to clean status?\n(Warning, this will result in no data to be loaded.)");
        Button resetCleanButton = new Button(FontImage.createMaterial(FontImage.MATERIAL_CLEAR_ALL, "restore_clean", GssUtilities.ICON_SIZE));
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

        add(resetCleanLabel);
        add(resetCleanButton);

        setDisposeWhenPointerOutOfBounds(true);

    }

}
