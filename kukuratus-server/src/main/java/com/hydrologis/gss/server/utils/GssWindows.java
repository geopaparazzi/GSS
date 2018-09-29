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
package com.hydrologis.gss.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemBoolean;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDynamicText;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemLabel;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemText;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.views.FormsView;
import com.hydrologis.kukuratus.libs.utils.KukuratusWindows;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class GssWindows {
    public static void labelParamsWindow( FormsView parent, boolean checkUnderline, JSONArray formItems ) {
        KukuratusWindows window = new KukuratusWindows("Add a new label", true){
            private TextField labelTextField;
            private ComboBox<Integer> sizeCombo;
            private CheckBox underLineCheck;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                labelTextField = new TextField();
                labelTextField.setPlaceholder("Enter label text.");
                labelTextField.setWidth("100%");

                sizeCombo = new ComboBox<>();
                sizeCombo.setItems(10, 14, 16, 18, 20, 24, 28, 30, 34, 38, 40, 50, 60);
                sizeCombo.setSelectedItem(20);
                sizeCombo.setEmptySelectionAllowed(false);
                sizeCombo.setPlaceholder("Label size");
                sizeCombo.setWidth("100%");

                underLineCheck = new CheckBox();
                underLineCheck.setValue(checkUnderline);
                underLineCheck.setCaption("underline");

                layout.addComponent(labelTextField);
                layout.addComponent(sizeCombo);
                layout.addComponent(underLineCheck);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button submitButton = new Button("Ok", VaadinIcons.CHECK);
                submitButton.addClickListener(e -> onActionButtonPushed());
                submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, submitButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                String value = labelTextField.getValue();
                Optional<Integer> sizeOpt = sizeCombo.getSelectedItem();
                Boolean underline = underLineCheck.getValue();

                ItemLabel il = new ItemLabel(value, sizeOpt.get(), underline);
                String labelJson = il.toString();
                formItems.put(new JSONObject(labelJson));

                parent.saveCurrentTag();
                parent.reloadFormTab();
                getMainWindow().close();
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }
        };
        window.centerWithSize("400px", null);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void booleanParamsWindow( FormsView parent, JSONArray formItems ) {
        KukuratusWindows window = new KukuratusWindows("Add a new boolean", true){
            private TextField keyTextField;
            private TextField labelTextField;
            private CheckBox defaultValueCheck;
            private CheckBox isMandatoryCheck;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                keyTextField = new TextField();
                keyTextField.setPlaceholder("Enter key (has to be unique).");
                keyTextField.setWidth("100%");

                labelTextField = new TextField();
                labelTextField.setPlaceholder("Enter label text.");
                labelTextField.setWidth("100%");

                defaultValueCheck = new CheckBox();
                defaultValueCheck.setCaption("default value");

                isMandatoryCheck = new CheckBox();
                isMandatoryCheck.setCaption("is mandatory?");

                layout.addComponent(keyTextField);
                layout.addComponent(labelTextField);
                layout.addComponent(defaultValueCheck);
                layout.addComponent(isMandatoryCheck);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button submitButton = new Button("Ok", VaadinIcons.CHECK);
                submitButton.addClickListener(e -> onActionButtonPushed());
                submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, submitButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                String key = keyTextField.getValue();
                if (checkKey(key)) {
                    String label = labelTextField.getValue();
                    Boolean defaultValue = defaultValueCheck.getValue();
                    Boolean isMandatory = isMandatoryCheck.getValue();

                    ItemBoolean ib = new ItemBoolean(key, label, defaultValue.toString(), isMandatory);
                    String booleanJson = ib.toString();
                    formItems.put(new JSONObject(booleanJson));

                    parent.saveCurrentTag();
                    parent.reloadFormTab();
                    getMainWindow().close();
                }
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }
        };
        window.centerWithSize("400px", null);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void comboParamsWindow( FormsView parent, boolean isMulti, JSONArray formItems ) {
        KukuratusWindows window = new KukuratusWindows("Add a new combo", true){
            private TextField keyTextField;
            private TextField labelTextField;
            private CheckBox isMandatoryCheck;
            private TextArea itemsTextField;
            private TextField defaultTextField;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                keyTextField = new TextField();
                keyTextField.setPlaceholder("Enter key (has to be unique).");
                keyTextField.setWidth("100%");

                labelTextField = new TextField();
                labelTextField.setPlaceholder("Enter label text.");
                labelTextField.setWidth("100%");

                itemsTextField = new TextArea();
                itemsTextField.setRows(10);
                itemsTextField.setPlaceholder("Enter items (one per line).");
                itemsTextField.setSizeFull();

                final Label infoLabel = new Label("<b>or drop file here</b>", ContentMode.HTML);
                infoLabel.setSizeUndefined();
                VerticalLayout itemsDropPane = new VerticalLayout(infoLabel);
                itemsDropPane.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);
                itemsDropPane.addStyleName(ValoTheme.PANEL_WELL);// ("drop-area");
                itemsDropPane.setWidth("100%");
                itemsDropPane.setHeight("100%");
                addDropFunctionality(itemsDropPane);

                HorizontalLayout itemsLayoutLayout = new HorizontalLayout(itemsTextField, itemsDropPane);
                itemsLayoutLayout.setWidth("100%");
                layout.addComponent(itemsLayoutLayout);

                defaultTextField = new TextField();
                defaultTextField.setPlaceholder("Optional default value.");
                defaultTextField.setWidth("100%");

                isMandatoryCheck = new CheckBox();
                isMandatoryCheck.setCaption("is mandatory?");

                layout.addComponent(keyTextField);
                layout.addComponent(labelTextField);
                layout.addComponent(itemsLayoutLayout);
                layout.addComponent(defaultTextField);
                layout.addComponent(isMandatoryCheck);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button submitButton = new Button("Ok", VaadinIcons.CHECK);
                submitButton.addClickListener(e -> onActionButtonPushed());
                submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, submitButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                String key = keyTextField.getValue();
                if (checkKey(key)) {
                    String label = labelTextField.getValue();
                    String itemsValue = itemsTextField.getValue().trim();
                    String defaultValue = defaultTextField.getValue();
                    Boolean isMandatory = isMandatoryCheck.getValue();

                    ItemCombo ic = new ItemCombo(key, label, itemsValue.split("\n"), defaultValue, isMulti, isMandatory);
                    String comboJson = ic.toString();
                    formItems.put(new JSONObject(comboJson));

                    parent.saveCurrentTag();
                    parent.reloadFormTab();
                    getMainWindow().close();
                }
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }

            private void addDropFunctionality( VerticalLayout dropPane ) {
                new FileDropTarget<>(dropPane, fileDropEvent -> {
                    final int fileSizeLimit = 2 * 1024 * 1024; // 2MB
                    fileDropEvent.getFiles().forEach(html5File -> {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                        if (html5File.getFileSize() > fileSizeLimit) {
                            KukuratusWindows
                                    .openWarningNotification("File rejected. Max " + fileSizeLimit + "MB files are accepted.");
                        } else {
                            final StreamVariable streamVariable = new StreamVariable(){
                                @Override
                                public OutputStream getOutputStream() {
                                    return outStream;
                                }

                                @Override
                                public boolean listenProgress() {
                                    return false;
                                }

                                @Override
                                public void onProgress( final StreamingProgressEvent event ) {
                                }

                                @Override
                                public void streamingStarted( final StreamingStartEvent event ) {
                                }

                                @Override
                                public void streamingFinished( final StreamingEndEvent event ) {
                                    try {
                                        String string = outStream.toString(StandardCharsets.UTF_8.name());
                                        itemsTextField.setValue(string);
                                    } catch (UnsupportedEncodingException e) {
                                        KukuratusWindows.openErrorNotification(e.getMessage());
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void streamingFailed( final StreamingErrorEvent event ) {
                                }

                                @Override
                                public boolean isInterrupted() {
                                    return false;
                                }
                            };
                            html5File.setStreamVariable(streamVariable);
                        }
                    });
                });

            }
        };
        window.centerWithSize("600px", null);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void dynamicTextParamsWindow( FormsView parent, JSONArray formItems ) {
        KukuratusWindows window = new KukuratusWindows("Add a new dynamic text", true){
            private TextField keyTextField;
            private TextField labelTextField;
            private CheckBox isMandatoryCheck;
            private TextArea itemsTextField;
            private CheckBox isLabelCheck;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                keyTextField = new TextField();
                keyTextField.setPlaceholder("Enter key (has to be unique).");
                keyTextField.setWidth("100%");

                labelTextField = new TextField();
                labelTextField.setPlaceholder("Enter label text.");
                labelTextField.setWidth("100%");

                itemsTextField = new TextArea();
                itemsTextField.setRows(10);
                itemsTextField.setPlaceholder("Enter default items (one per line).");
                itemsTextField.setWidth("100%");

                isLabelCheck = new CheckBox();
                isLabelCheck.setCaption("is label?");

                isMandatoryCheck = new CheckBox();
                isMandatoryCheck.setCaption("is mandatory?");

                layout.addComponent(keyTextField);
                layout.addComponent(labelTextField);
                layout.addComponent(itemsTextField);
                layout.addComponent(isLabelCheck);
                layout.addComponent(isMandatoryCheck);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button submitButton = new Button("Ok", VaadinIcons.CHECK);
                submitButton.addClickListener(e -> onActionButtonPushed());
                submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, submitButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                String key = keyTextField.getValue();
                if (checkKey(key)) {
                    String label = labelTextField.getValue();
                    String itemsValue = itemsTextField.getValue().trim();
                    Boolean isMandatory = isMandatoryCheck.getValue();
                    Boolean isLabel = isLabelCheck.getValue();
                    itemsValue = itemsValue.replace('\n', ';');
                    ItemDynamicText ic = new ItemDynamicText(key, label, itemsValue, isMandatory, isLabel);
                    String comboJson = ic.toString();
                    formItems.put(new JSONObject(comboJson));

                    parent.saveCurrentTag();
                    parent.reloadFormTab();
                    getMainWindow().close();
                }
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }

        };
        window.centerWithSize("600px", null);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void textParamsWindow( FormsView parent, JSONArray formItems ) {
        KukuratusWindows window = new KukuratusWindows("Add a new text field", true){
            private TextField keyTextField;
            private TextField labelTextField;
            private CheckBox isMandatoryCheck;
            private TextField defaultTextField;
            private CheckBox isLabelCheck;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                keyTextField = new TextField();
                keyTextField.setPlaceholder("Enter key (has to be unique).");
                keyTextField.setWidth("100%");

                labelTextField = new TextField();
                labelTextField.setPlaceholder("Enter label text.");
                labelTextField.setWidth("100%");

                defaultTextField = new TextField();
                defaultTextField.setPlaceholder("Optional default value.");
                defaultTextField.setWidth("100%");

                isLabelCheck = new CheckBox();
                isLabelCheck.setCaption("is label?");

                isMandatoryCheck = new CheckBox();
                isMandatoryCheck.setCaption("is mandatory?");

                layout.addComponent(keyTextField);
                layout.addComponent(labelTextField);
                layout.addComponent(defaultTextField);
                layout.addComponent(isLabelCheck);
                layout.addComponent(isMandatoryCheck);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button submitButton = new Button("Ok", VaadinIcons.CHECK);
                submitButton.addClickListener(e -> onActionButtonPushed());
                submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, submitButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                String key = keyTextField.getValue();
                if (checkKey(key)) {
                    String label = labelTextField.getValue();
                    String defaultValue = defaultTextField.getValue();
                    Boolean isMandatory = isMandatoryCheck.getValue();
                    Boolean isLabel = isLabelCheck.getValue();

                    ItemText ic = new ItemText(key, label, defaultValue, isMandatory, isLabel);
                    String textJson = ic.toString();
                    formItems.put(new JSONObject(textJson));

                    parent.saveCurrentTag();
                    parent.reloadFormTab();
                    getMainWindow().close();
                }
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }

        };
        window.centerWithSize("400px", null);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void numericParamsWindow( FormsView parent, boolean isDouble, JSONArray formItems ) {
        KukuratusWindows window = new KukuratusWindows("Add a new numeric field", true){
            private TextField keyTextField;
            private TextField labelTextField;
            private CheckBox isMandatoryCheck;
            private TextField defaultTextField;
            private CheckBox isLabelCheck;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                keyTextField = new TextField();
                keyTextField.setPlaceholder("Enter key (has to be unique).");
                keyTextField.setWidth("100%");

                labelTextField = new TextField();
                labelTextField.setPlaceholder("Enter label text.");
                labelTextField.setWidth("100%");

                defaultTextField = new TextField();
                defaultTextField.setPlaceholder("Optional default value.");
                defaultTextField.setWidth("100%");

                isLabelCheck = new CheckBox();
                isLabelCheck.setCaption("is label?");

                isMandatoryCheck = new CheckBox();
                isMandatoryCheck.setCaption("is mandatory?");

                layout.addComponent(keyTextField);
                layout.addComponent(labelTextField);
                layout.addComponent(defaultTextField);
                layout.addComponent(isLabelCheck);
                layout.addComponent(isMandatoryCheck);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button submitButton = new Button("Ok", VaadinIcons.CHECK);
                submitButton.addClickListener(e -> onActionButtonPushed());
                submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, submitButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                String key = keyTextField.getValue();
                if (checkKey(key)) {
                    String defaultValue = defaultTextField.getValue();
                    if (defaultValue.trim().length() > 0) {
                        if (isDouble) {
                            try {
                                Double.parseDouble(defaultValue);
                            } catch (NumberFormatException e) {
                                KukuratusWindows.openWarningNotification("The value has to be a double number.");
                                return;
                            }
                        } else {
                            try {
                                Integer.parseInt(defaultValue);
                            } catch (NumberFormatException e) {
                                KukuratusWindows.openWarningNotification("The value has to be an integer number.");
                                return;
                            }
                        }
                    }

                    String label = labelTextField.getValue();
                    Boolean isMandatory = isMandatoryCheck.getValue();
                    Boolean isLabel = isLabelCheck.getValue();

                    ItemText ic = new ItemText(key, label, defaultValue, isMandatory, isLabel);
                    String textJson = ic.toString();
                    formItems.put(new JSONObject(textJson));

                    parent.saveCurrentTag();
                    parent.reloadFormTab();
                    getMainWindow().close();
                }
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }

        };
        window.centerWithSize("400px", null);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    private static boolean checkKey( String key ) {
        key = key.trim();
        if (key.length() == 0) {
            KukuratusWindows.openWarningNotification("The key needs to be set.");
            return false;
        } else if (key.contains(" ")) {
            KukuratusWindows.openWarningNotification("The key should not contain spaces.");
            return false;
        } else {
            return true;
        }
    }

}
