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
package com.hydrologis.gss.server.views;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hortonmachine.gears.io.geopaparazzi.forms.Form;
import org.hortonmachine.gears.io.geopaparazzi.forms.Section;
import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemBoolean;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemConnectedCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDate;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDouble;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDynamicText;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemInteger;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemLabel;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemMap;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemOneToManyConnectedCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemPicture;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemSketch;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemText;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.database.objects.Forms;
import com.hydrologis.gss.server.utils.GssWindows;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.DbProvider;
import com.hydrologis.kukuratus.libs.spi.DefaultPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KLabel;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.KukuratusWindows;
import com.hydrologis.kukuratus.libs.utils.TextRunnable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class FormsView extends VerticalLayout implements View, DefaultPage {

    private String authenticatedUsername;

    private Dao<Forms, ? > formsDAO;

    private Forms currentSelectedTags = null;

    private VerticalLayout formAndMenubarAreaLayout;
    private VerticalLayout formAreaLayout;

    private TabSheet currentSelectedFormsTabSheet;
    private Tab currentSelectedFormTab;

    private ComboBox<Forms> tagsCombo;

    private JSONObject curentSelectedSectionObject;

    private String curentSelectedSectionName;

    private MenuItem sectionsMenuItem;

    private MenuItem sectionsSeparatorMenuItem;

    private MenuItem formsMenuItem;

    private MenuItem widgetsMenuItem;

    @Override
    public void enter( ViewChangeEvent event ) {
        try {
            setMargin(true);

            authenticatedUsername = AuthService.INSTANCE.getAuthenticatedUsername();
            DbProvider dbProvider = SpiHandler.INSTANCE.getDbProviderSingleton();
            DatabaseHandler dbHandler = dbProvider.getDatabaseHandler().get();

            formsDAO = dbHandler.getDao(Forms.class);
            QueryBuilder<Forms, ? > formsQB = formsDAO.queryBuilder();
            List<Forms> formsList = formsQB.where().eq(Forms.WEBUSER_FIELD_NAME, authenticatedUsername).query();

            Button addTag = new Button("Add", VaadinIcons.PLUS_CIRCLE);
            addTag.setStyleName(ValoTheme.BUTTON_PRIMARY);
            addTag.addClickListener(e -> addTag());
            Button deleteTag = new Button("Remove", VaadinIcons.TRASH);
            deleteTag.setStyleName(ValoTheme.BUTTON_DANGER);
            deleteTag.setVisible(false);
            deleteTag.addClickListener(e -> deleteTag());

            tagsCombo = new ComboBox<>();
            tagsCombo.setPlaceholder("No tags selected");
            tagsCombo.setItemCaptionGenerator(Forms::getName);
            tagsCombo.setEmptySelectionAllowed(false);
            tagsCombo.addSelectionListener(new SingleSelectionListener<Forms>(){
                @Override
                public void selectionChange( SingleSelectionEvent<Forms> event ) {
                    Optional<Forms> selectedForm = event.getFirstSelectedItem();
                    selectedForm.ifPresent(form -> {
                        currentSelectedTags = form;
                        selectForm();
                        deleteTag.setVisible(true);
                    });
                }
            });

            HorizontalLayout tagsLayout = new HorizontalLayout(tagsCombo, addTag, deleteTag);
            addComponent(tagsLayout);

            formAndMenubarAreaLayout = new VerticalLayout();
            resetTagsCombo(formsList);

            setSizeFull();

        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private void resetTagsCombo( List<Forms> formsList ) {
        tagsCombo.setItems(formsList);
        formAndMenubarAreaLayout.removeAllComponents();
        Panel placeHolder = new Panel();
        placeHolder.addStyleName(ValoTheme.PANEL_WELL);
        placeHolder.setSizeFull();
        formAndMenubarAreaLayout.addComponent(placeHolder);
        addComponentsAndExpand(formAndMenubarAreaLayout);
    }

    private void deleteTag() {
        String message = new KLabel.Builder().text("Are you sure you want to delete: <b>" + currentSelectedTags.name + "</b>")
                .fontSizeInPixels(18).color("#AF0000").buildLabel();
        KukuratusWindows.openCancelDeleteWindow(this, message, GssWindows.DEFAULT_WIDTH, null, new Runnable(){
            public void run() {
                try {
                    QueryBuilder<Forms, ? > formsQB = formsDAO.queryBuilder();
                    Forms formToDelete = formsQB.where().eq(Forms.NAME_FIELD_NAME, currentSelectedTags.name).queryForFirst();
                    if (formToDelete == null) {
                        getUI().access(() -> {
                            KukuratusWindows.openWarningNotification("The selected form doesn't exist anymore.");
                        });
                        return;
                    }

                    formsDAO.delete(formToDelete);
                    formsQB = formsDAO.queryBuilder();
                    List<Forms> formsList = formsQB.where().eq(Forms.WEBUSER_FIELD_NAME, authenticatedUsername).query();
                    getUI().access(() -> {
                        resetTagsCombo(formsList);
                    });

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void addTag() {
        String message = new KLabel.Builder().text("Add a new tag definition. Enter a name for it!").fontSizeInPixels(18)
                .buildLabel();
        KukuratusWindows.inputWindow(this, message, GssWindows.DEFAULT_WIDTH, null, new TextRunnable(){
            @Override
            public void runOnText( String newTagName ) {
                newTagName = newTagName.trim();

                try {
                    QueryBuilder<Forms, ? > formsQB = formsDAO.queryBuilder();
                    Forms forms = formsQB.where().eq(Forms.NAME_FIELD_NAME, newTagName).queryForFirst();
                    if (forms != null) {
                        getUI().access(() -> {
                            KukuratusWindows.openWarningNotification("A form with that name already exists.");
                        });
                        return;
                    }

                    Forms newTag = new Forms(newTagName, "[]", authenticatedUsername);
                    newTag = formsDAO.createIfNotExists(newTag);
                    formsQB = formsDAO.queryBuilder();
                    List<Forms> formsList = formsQB.where().eq(Forms.WEBUSER_FIELD_NAME, authenticatedUsername).query();
                    Forms _newTag = newTag;
                    getUI().access(() -> {
                        resetTagsCombo(formsList);
                        tagsCombo.setSelectedItem(_newTag);
                    });

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

    protected void selectForm() {
        curentSelectedSectionName = null;
        curentSelectedSectionObject = null;

        LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);

        formAndMenubarAreaLayout.removeAllComponents();

        MenuBar menuBar = new MenuBar();
        menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

        sectionsMenuItem = menuBar.addItem("Sections", VaadinIcons.TASKS, null);
        sectionsMap.keySet().stream().forEach(sectionName -> {
            sectionsMenuItem.addItem(sectionName, VaadinIcons.FOLDER_OPEN, i -> {
                LinkedHashMap<String, JSONObject> tmpSectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);
                curentSelectedSectionName = sectionName;
                curentSelectedSectionObject = tmpSectionsMap.get(sectionName);
                loadSection();
            });
        });
        sectionsSeparatorMenuItem = sectionsMenuItem.addSeparator();
        sectionsMenuItem.addItem("add new", VaadinIcons.PLUS, i -> {
            addNewSection();
        });
        sectionsMenuItem.addItem("remove selected", VaadinIcons.MINUS, i -> {
            removeSection();
        });

        formsMenuItem = menuBar.addItem("Forms", VaadinIcons.TABS, null);
        formsMenuItem.addItem("add new", VaadinIcons.PLUS, i -> {
            addNewForm();
        });
        formsMenuItem.addItem("remove selected", VaadinIcons.MINUS, i -> {
            removeForm();
        });
        formsMenuItem.setVisible(false);

        widgetsMenuItem = menuBar.addItem("Widgets", VaadinIcons.INPUT, null);
        List<String> widgetNames = Arrays.asList(Utilities.ITEM_NAMES).stream().filter(name -> {
            boolean isUnsupported = name.equals(ItemConnectedCombo.TYPE) || name.equals(ItemOneToManyConnectedCombo.TYPE);
            return !isUnsupported;
        }).sorted().collect(Collectors.toList());

        widgetNames.forEach(name -> {
            widgetsMenuItem.addItem("add " + name, VaadinIcons.PLUS, i -> {
                addNewWidget(name);
            });
        });
        widgetsMenuItem.addItem("remove", VaadinIcons.MINUS, i -> {
            removeWidget();
        });
        widgetsMenuItem.setVisible(false);

        formAndMenubarAreaLayout.addComponent(menuBar);
        formAreaLayout = new VerticalLayout();
        formAndMenubarAreaLayout.addComponentsAndExpand(formAreaLayout);

        Panel placeHolder = new Panel();
        placeHolder.addStyleName(ValoTheme.PANEL_WELL);
        placeHolder.setSizeFull();
        formAreaLayout.addComponentsAndExpand(placeHolder);
    }

    private void loadSection() {
        if (formAreaLayout == null) {
            throw new RuntimeException();
        }
        formAreaLayout.removeAllComponents();
        formsMenuItem.setVisible(true);
        widgetsMenuItem.setVisible(true);

        List<String> formNames4Section = Utilities.getFormNames4Section(curentSelectedSectionObject);

        currentSelectedFormsTabSheet = new TabSheet();
        currentSelectedFormsTabSheet.setHeight(100.0f, Unit.PERCENTAGE);
        currentSelectedFormsTabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        currentSelectedFormsTabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        currentSelectedFormsTabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener(){
            public void selectedTabChange( SelectedTabChangeEvent event ) {
                currentSelectedFormsTabSheet = event.getTabSheet();
                reloadFormTab();
            }
        });

        for( String name : formNames4Section ) {
            final VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            currentSelectedFormsTabSheet.addTab(layout, name);
        }

        formAreaLayout.addComponentsAndExpand(currentSelectedFormsTabSheet);
    }

    private void addNewWidget( String widgetName ) {
        if (curentSelectedSectionName == null) {
            KukuratusWindows.openInfoNotification("No section selected.");
            return;
        }

        if (currentSelectedFormTab == null) {
            KukuratusWindows.openInfoNotification("No form selected.");
            return;
        }
        String formName = currentSelectedFormTab.getCaption();
        JSONObject form4Name = Utilities.getForm4Name(formName, curentSelectedSectionObject);
        JSONArray formItems = Utilities.getFormItems(form4Name);

        switch( widgetName ) {
        case ItemLabel.TYPE:
            GssWindows.labelParamsWindow(this, false, formItems);
            break;
        case ItemLabel.TYPE_WITHLINE:
            GssWindows.labelParamsWindow(this, true, formItems);
            break;
        case ItemBoolean.TYPE:
            GssWindows.booleanParamsWindow(this, formItems);
            break;
        case ItemCombo.TYPE:
            GssWindows.comboParamsWindow(this, false, formItems);
            break;
        case ItemCombo.MULTI_TYPE:
            GssWindows.comboParamsWindow(this, true, formItems);
            break;
        case ItemText.TYPE:
            GssWindows.textParamsWindow(this, formItems);
            break;
        case ItemInteger.TYPE:
            GssWindows.numericParamsWindow(this, false, formItems);
            break;
        case ItemDouble.TYPE:
            GssWindows.numericParamsWindow(this, true, formItems);
            break;
        case ItemDynamicText.TYPE:
            GssWindows.dynamicTextParamsWindow(this, formItems);
            break;
        case ItemDate.TYPE:
            GssWindows.dateParamsWindow(this, formItems);
            break;
        case ItemTime.TYPE:
            GssWindows.timeParamsWindow(this, formItems);
            break;
        case ItemPicture.TYPE:
            GssWindows.imageParamsWindow(this, formItems, GssWindows.IMAGEWIDGET.PICTURE);
            break;
        case ItemSketch.TYPE:
            GssWindows.imageParamsWindow(this, formItems, GssWindows.IMAGEWIDGET.SKETCH);
            break;
        case ItemMap.TYPE:
            GssWindows.imageParamsWindow(this, formItems, GssWindows.IMAGEWIDGET.MAP);
            break;
        case ItemConnectedCombo.TYPE:
            KukuratusWindows.openWarningNotification("Not implemented yet");
            break;
        case ItemOneToManyConnectedCombo.TYPE:
            KukuratusWindows.openWarningNotification("Not implemented yet");
            break;
        default:
            KukuratusWindows.openWarningNotification("Widget " + widgetName + " does not exist.");
            break;
        }

    }
    private void removeWidget() {
        if (curentSelectedSectionName == null) {
            KukuratusWindows.openInfoNotification("No section selected.");
            return;
        }

        if (currentSelectedFormTab == null) {
            KukuratusWindows.openInfoNotification("No form selected.");
            return;
        }
        String formName = currentSelectedFormTab.getCaption();
        JSONObject form4Name = Utilities.getForm4Name(formName, curentSelectedSectionObject);
        JSONArray formItems = Utilities.getFormItems(form4Name);
        GssWindows.deleteWidgetWindow(this, formItems);
    }

    private void addNewForm() {
        if (curentSelectedSectionName == null) {
            KukuratusWindows.openInfoNotification("No section selected.");
            return;
        }

        LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);
        JSONObject sectionObject = sectionsMap.get(curentSelectedSectionName);
        List<String> formNames = Utilities.getFormNames4Section(sectionObject);

        String message = "<h3>Add a new Form to the Section: <b>" + curentSelectedSectionName + "</b>. Enter a name for it!</h3>";
        KukuratusWindows.inputWindow(this, message, "500px", null, new TextRunnable(){
            @Override
            public void runOnText( String newFormName ) {
                newFormName = newFormName.trim();
                if (formNames.contains(newFormName)) {
                    getUI().access(() -> {
                        KukuratusWindows.openWarningNotification("A form with that name already exists.");
                    });
                    return;
                }

                Form newForm = new Form(newFormName);

                JSONObject formJson = new JSONObject(newForm.toString());
                JSONArray formsArray = sectionObject.getJSONArray(Utilities.ATTR_FORMS);
                formsArray.put(formJson);

                sectionsMap.put(curentSelectedSectionName, sectionObject);
                curentSelectedSectionObject = sectionObject;

                JSONArray rootArray = Utilities.formsRootFromSectionsMap(sectionsMap);
                String rootString = rootArray.toString(2);
                currentSelectedTags.form = rootString;
                try {
                    formsDAO.update(currentSelectedTags);
                    String _newFormName = newFormName;
                    getUI().access(() -> {
                        final VerticalLayout layout = new VerticalLayout();
                        layout.setMargin(true);
                        Tab newTab = currentSelectedFormsTabSheet.addTab(layout, _newFormName);
                        currentSelectedFormsTabSheet.setSelectedTab(newTab);
                    });
                } catch (SQLException e) {
                    KukuratusWindows.openErrorNotification(e.getMessage());
                    e.printStackTrace();
                }

            }

        });

    }
    private void removeForm() {
        if (curentSelectedSectionName == null) {
            KukuratusWindows.openInfoNotification("No section selected.");
            return;
        }

        if (currentSelectedFormTab == null) {
            KukuratusWindows.openInfoNotification("No form selected.");
            return;
        }
        String formName = currentSelectedFormTab.getCaption();

        String message = "<h3>Are you sure you want to delete Form: <b>" + formName + "</b>?</h3>";
        KukuratusWindows.openCancelDeleteWindow(this, message, "500px", null, new Runnable(){
            @Override
            public void run() {
                Utilities.removeFormFromSection(formName, curentSelectedSectionObject);

                LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);
                sectionsMap.put(curentSelectedSectionName, curentSelectedSectionObject);
                JSONArray rootArray = Utilities.formsRootFromSectionsMap(sectionsMap);
                String rootString = rootArray.toString(2);
                currentSelectedTags.form = rootString;
                try {
                    formsDAO.update(currentSelectedTags);
                } catch (SQLException e) {
                    KukuratusWindows.openErrorNotification(e.getMessage());
                }
                getUI().access(() -> {
                    currentSelectedFormsTabSheet.removeTab(currentSelectedFormTab);
                });
            }
        });

    }

    private void addNewSection() {
        LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);
        String message = "<h3>Add a new Section to Tag: <b>" + currentSelectedTags.name + "</b>. Enter a name for it!</h3>";
        KukuratusWindows.inputWindow(this, message, "500px", null, new TextRunnable(){
            @Override
            public void runOnText( String newSectionName ) {
                newSectionName = newSectionName.trim();

                JSONObject sectionObj = sectionsMap.get(newSectionName);
                if (sectionObj != null) {
                    getUI().access(() -> {
                        KukuratusWindows.openWarningNotification("A section with that name already exists.");
                    });
                    return;
                }

                Section newSection = new Section(newSectionName);

                JSONObject sectionJson = new JSONObject(newSection.toString());
                sectionsMap.put(newSectionName, sectionJson);

                JSONArray rootArray = Utilities.formsRootFromSectionsMap(sectionsMap);
                String rootString = rootArray.toString(2);
                currentSelectedTags.form = rootString;
                try {
                    formsDAO.update(currentSelectedTags);
                    String _newSectionName = newSectionName;
                    getUI().access(() -> {
                        sectionsMenuItem.addItemBefore(_newSectionName, VaadinIcons.FOLDER_OPEN, i -> {
                            curentSelectedSectionName = _newSectionName;
                            curentSelectedSectionObject = sectionJson;
                            loadSection();
                        }, sectionsSeparatorMenuItem);
                    });
                } catch (SQLException e) {
                    KukuratusWindows.openErrorNotification(e.getMessage());
                    e.printStackTrace();
                }

            }

        });

    }
    private void removeSection() {
        if (curentSelectedSectionName == null) {
            return;
        }
        LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);
        String message = new KLabel.Builder()
                .text("Are you sure you want to delete the Section: <b>" + curentSelectedSectionName + "</b>")
                .fontSizeInPixels(18).color("#AF0000").buildLabel();
        KukuratusWindows.openCancelDeleteWindow(this, message, "500px", null, new Runnable(){
            public void run() {
                JSONObject sectionObj = sectionsMap.remove(curentSelectedSectionName);
                if (sectionObj == null) {
                    getUI().access(() -> {
                        KukuratusWindows.openWarningNotification("The selected section doesn't exist.");
                    });
                    return;
                }

                JSONArray rootArray = Utilities.formsRootFromSectionsMap(sectionsMap);
                String rootString = rootArray.toString(2);
                currentSelectedTags.form = rootString;
                try {
                    formsDAO.update(currentSelectedTags);
                    getUI().access(() -> {
                        selectForm();
                    });
                } catch (SQLException e) {
                    KukuratusWindows.openErrorNotification(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    public void saveCurrentTag() {
        LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(currentSelectedTags.form);
        if (curentSelectedSectionObject != null) {
            sectionsMap.put(curentSelectedSectionName, curentSelectedSectionObject);
        }
        JSONArray rootArray = Utilities.formsRootFromSectionsMap(sectionsMap);
        String rootString = rootArray.toString(2);
        currentSelectedTags.form = rootString;
        try {
            formsDAO.update(currentSelectedTags);
        } catch (SQLException e) {
            KukuratusWindows.openErrorNotification(e.getMessage());
            e.printStackTrace();
        }
    }

    public void reloadFormTab() {
        Layout tab = (Layout) currentSelectedFormsTabSheet.getSelectedTab();
        currentSelectedFormTab = currentSelectedFormsTabSheet.getTab(tab);
        String formName = currentSelectedFormTab.getCaption();
        JSONObject formJson = Utilities.getForm4Name(formName, curentSelectedSectionObject);
        tab.removeAllComponents();

        JSONArray formItems = Utilities.getFormItems(formJson);
        for( int i = 0; i < formItems.length(); i++ ) {
            JSONObject jsonObject = formItems.getJSONObject(i);
            if (jsonObject.has(Utilities.TAG_TYPE)) {
                String type = jsonObject.getString(Utilities.TAG_TYPE).trim();

                String key = null;
                if (jsonObject.has(Utilities.TAG_KEY)) {
                    key = jsonObject.getString(Utilities.TAG_KEY).trim();
                }
                String label = null;
                if (jsonObject.has(Utilities.TAG_LABEL)) {
                    label = jsonObject.get(Utilities.TAG_LABEL).toString().trim();
                }
                if (label == null && key != null) {
                    label = key;
                }
                String defaultValue = null;
                if (jsonObject.has(Utilities.TAG_VALUE)) {
                    defaultValue = jsonObject.get(Utilities.TAG_VALUE).toString().trim();
                }
                if (defaultValue == null) {
                    defaultValue = "";
                }
                KLabel mainLabel = new KLabel.Builder().text(label).color("#5d9d76").build();
                tab.addComponent(mainLabel);
                switch( type ) {
                case ItemLabel.TYPE:
                    int size = 20;
                    if (jsonObject.has(Utilities.TAG_SIZE)) {
                        size = Integer.parseInt(jsonObject.get(Utilities.TAG_SIZE).toString().trim());
                    }
                    String value = new KLabel.Builder().text(defaultValue).fontSizeInPixels(size).color("#5d9d76").buildLabel();
                    mainLabel.setValue(value);
                    break;
                case ItemLabel.TYPE_WITHLINE:
                    size = 20;
                    if (jsonObject.has(Utilities.TAG_SIZE)) {
                        size = Integer.parseInt(jsonObject.get(Utilities.TAG_SIZE).toString().trim());
                    }
                    String value1 = new KLabel.Builder().text(defaultValue).underline().fontSizeInPixels(size).color("#5d9d76")
                            .buildLabel();
                    mainLabel.setValue(value1);
                    break;
                case ItemBoolean.TYPE:
                    CheckBox checkBox = new CheckBox();
                    if (defaultValue != null && defaultValue.equals("true")) {
                        checkBox.setValue(true);
                    }
                    tab.addComponent(checkBox);
                    break;
                case ItemCombo.TYPE:
                    String[] values = new String[0];
                    if (jsonObject.has(Utilities.TAG_VALUES)) {
                        JSONObject valuesObject = jsonObject.getJSONObject(Utilities.TAG_VALUES);
                        if (valuesObject.has(Utilities.TAG_ITEMS)) {
                            JSONArray valuesArray = valuesObject.getJSONArray(Utilities.TAG_ITEMS);
                            values = new String[valuesArray.length()];
                            for( int j = 0; j < valuesArray.length(); j++ ) {
                                JSONObject itemObj = valuesArray.getJSONObject(j);
                                values[j] = itemObj.getString(Utilities.TAG_ITEM);
                            }
                        }
                    }

                    ComboBox<String> comboBox = new ComboBox<>();
                    comboBox.setItems(values);
                    if (defaultValue != null) {
                        comboBox.setSelectedItem(defaultValue);
                    }
                    tab.addComponent(comboBox);
                    break;
                case ItemCombo.MULTI_TYPE:
                    String[] multiValues = new String[0];
                    if (jsonObject.has(Utilities.TAG_VALUES)) {
                        JSONObject valuesObject = jsonObject.getJSONObject(Utilities.TAG_VALUES);
                        if (valuesObject.has(Utilities.TAG_ITEMS)) {
                            JSONArray valuesArray = valuesObject.getJSONArray(Utilities.TAG_ITEMS);
                            multiValues = new String[valuesArray.length()];
                            for( int j = 0; j < valuesArray.length(); j++ ) {
                                JSONObject itemObj = valuesArray.getJSONObject(j);
                                multiValues[j] = itemObj.getString(Utilities.TAG_ITEM);
                            }
                        }
                    }

                    ComboBox<String> multiComboBox = new ComboBox<>();
                    multiComboBox.setItems(multiValues);
                    if (defaultValue != null) {
                        multiComboBox.setSelectedItem(defaultValue);
                    }
                    tab.addComponent(multiComboBox);
                    break;
                case ItemDate.TYPE:
                    DateField date = new DateField();
                    date.setDateFormat("yyyy-MM-dd");
                    if (defaultValue.length() == 0) {
                        date.setValue(LocalDate.now());
                    } else {
                        try {
                            if (defaultValue.trim().length() > 0) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                date.setValue(LocalDate.parse(defaultValue, formatter));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    tab.addComponent(date);
                    break;
                case ItemTime.TYPE:
                    TextField time = new TextField();
                    time.setPlaceholder("HH:mm:ss");
                    try {
                        if (defaultValue.trim().length() > 0) {
                            time.setValue(defaultValue);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    tab.addComponent(time);
                    break;
                case ItemInteger.TYPE:
                    TextField integerField = new TextField();
                    integerField.setValue(defaultValue);
                    tab.addComponent(integerField);
                    break;
                case ItemDouble.TYPE:
                    TextField doubleField = new TextField();
                    doubleField.setValue(defaultValue);
                    tab.addComponent(doubleField);
                    break;
                case ItemDynamicText.TYPE:
                    String[] split = defaultValue.split(";");
                    for( String string : split ) {
                        TextField dynamicField = new TextField();
                        dynamicField.setValue(string.trim());
                        tab.addComponent(dynamicField);
                    }
                    Button addButton = new Button(VaadinIcons.PLUS_CIRCLE);
                    addButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                    tab.addComponent(addButton);
                    break;
                case ItemPicture.TYPE:
                    Label pictureImage = new Label();
                    pictureImage.setContentMode(ContentMode.HTML);
                    pictureImage.setValue(VaadinIcons.PICTURE.getHtml());
                    pictureImage.addStyleName("big-icon");
                    tab.addComponent(pictureImage);
                    break;
                case ItemSketch.TYPE:
                    Label sketchImage = new Label();
                    sketchImage.setContentMode(ContentMode.HTML);
                    sketchImage.setValue(VaadinIcons.PAINTBRUSH.getHtml());
                    sketchImage.addStyleName("big-icon");
                    tab.addComponent(sketchImage);
                    break;
                case ItemMap.TYPE:
                    Label mapImage = new Label();
                    mapImage.setContentMode(ContentMode.HTML);
                    mapImage.setValue(VaadinIcons.MAP_MARKER.getHtml());
                    mapImage.addStyleName("big-icon");
                    tab.addComponent(mapImage);
                    break;
                case ItemText.TYPE:
                    TextField textField = new TextField();
                    textField.setValue(defaultValue);
                    tab.addComponent(textField);
                    break;
                case ItemConnectedCombo.TYPE:
                    tab.addComponent(new Label(ItemConnectedCombo.TYPE));
                    break;
                case ItemOneToManyConnectedCombo.TYPE:
                    tab.addComponent(new Label(ItemOneToManyConnectedCombo.TYPE));
                    break;
                default:
                    break;
                }

            }
        }
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.FORM;
    }

    @Override
    public String getLabel() {
        return "Form Builder";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends View> Class<T> getNavigationViewClass() {
        return (Class<T>) this.getClass();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public boolean onlyAdmin() {
        return false;
    }
}
