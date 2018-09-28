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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

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
import com.hydrologis.gss.server.utils.KukuratusWindows;
import com.hydrologis.gss.server.utils.TextRunnable;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.DbProvider;
import com.hydrologis.kukuratus.libs.spi.DefaultPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class FormsView extends VerticalLayout implements View, DefaultPage {

    private String authenticatedUsername;

    private Dao<Forms, ? > formsDAO;

    private Forms currentSelectedTags = null;

    private VerticalLayout formAndMenubarAreaLayout;
    private VerticalLayout formAreaLayout;

    private TabSheet currentSelectedSectionTabSheet;
    private Tab currentSelectedTab;

    private ComboBox<Forms> tagsCombo;

    private JSONObject curentSelectedSectionObject;

    private String curentSelectedSectionName;

    private MenuItem sectionsMenuItem;

    private MenuItem sectionsSeparatorMenuItem;

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

            Button addTag = new Button("Add");
            addTag.setStyleName(ValoTheme.BUTTON_PRIMARY);
            addTag.addClickListener(e -> addTag());
            Button deleteTag = new Button("Remove");
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
        String message = "<font color=\"red\"><h3>Are you sure you want to delete tags: <b>" + currentSelectedTags.name
                + "</b></h3></font>";
        KukuratusWindows.openCancelDeleteWindow(this, message, "500px", null, new Runnable(){
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
        String message = "<h3>Add a new tag definition. Enter a name for it!</h3>";
        KukuratusWindows.inputWindow(this, message, "500px", null, new TextRunnable(){
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
                curentSelectedSectionName = sectionName;
                curentSelectedSectionObject = sectionsMap.get(sectionName);
                loadSection(curentSelectedSectionObject);
            });
        });
        sectionsSeparatorMenuItem = sectionsMenuItem.addSeparator();
        sectionsMenuItem.addItem("add new", VaadinIcons.PLUS, i -> {
            addNewSection();
        });
        sectionsMenuItem.addItem("remove selected", VaadinIcons.MINUS, i -> {
            removeSection();
        });

        MenuItem tabsItem = menuBar.addItem("Tabs", VaadinIcons.TABS, null);
        tabsItem.addItem("add new", VaadinIcons.PLUS, i -> {
            addNewTab();
        });
        tabsItem.addItem("remove", VaadinIcons.MINUS, i -> {
            removeTab();
        });

        MenuItem widgetsItem = menuBar.addItem("Widgets", VaadinIcons.INPUT, null);
        widgetsItem.addItem("add new", VaadinIcons.PLUS, i -> {
            addNewWidget();
        });
        widgetsItem.addItem("remove", VaadinIcons.MINUS, i -> {
            removeWidget();
        });

        formAndMenubarAreaLayout.addComponent(menuBar);
        formAreaLayout = new VerticalLayout();
        formAndMenubarAreaLayout.addComponentsAndExpand(formAreaLayout);

        Panel placeHolder = new Panel();
        placeHolder.addStyleName(ValoTheme.PANEL_WELL);
        placeHolder.setSizeFull();
        formAreaLayout.addComponentsAndExpand(placeHolder);
    }

    private void loadSection( JSONObject sectionObject ) {
        if (formAreaLayout == null) {
            throw new RuntimeException();
        }
        formAreaLayout.removeAllComponents();

        List<String> formNames4Section = Utilities.getFormNames4Section(sectionObject);

        currentSelectedSectionTabSheet = new TabSheet();
        currentSelectedSectionTabSheet.setHeight(100.0f, Unit.PERCENTAGE);
        currentSelectedSectionTabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        currentSelectedSectionTabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        currentSelectedSectionTabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener(){
            public void selectedTabChange( SelectedTabChangeEvent event ) {
                currentSelectedSectionTabSheet = event.getTabSheet();
                Layout tab = (Layout) currentSelectedSectionTabSheet.getSelectedTab();

                currentSelectedTab = currentSelectedSectionTabSheet.getTab(tab);
                String formName = currentSelectedTab.getCaption();
                JSONObject formJson = Utilities.getForm4Name(formName, sectionObject);
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
                        Label mainLabel = new Label("<font color=\"#5d9d76\">" + label + "</font>", ContentMode.HTML);
                        tab.addComponent(mainLabel);
                        switch( type ) {
                        case ItemLabel.TYPE:
                            String size = "20";
                            if (jsonObject.has(Utilities.TAG_SIZE)) {
                                size = jsonObject.get(Utilities.TAG_SIZE).toString().trim();
                            }
                            mainLabel.setValue("<font color=\"#5d9d76\" size=\"" + size + "\">" + defaultValue + "</font>");
                            break;
                        case ItemLabel.TYPE_WITHLINE:
                            size = "20";
                            if (jsonObject.has(Utilities.TAG_SIZE)) {
                                size = jsonObject.get(Utilities.TAG_SIZE).toString().trim();
                            }
                            mainLabel
                                    .setValue("<u><font color=\"#5d9d76\" size=\"" + size + "\">" + defaultValue + "</font></u>");
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
                            DateTimeField date = new DateTimeField();
                            if (defaultValue.length() == 0) {
                                date.setValue(LocalDateTime.now());
                            } else {
                                try {
                                    if (defaultValue.trim().length() > 0) {
                                        DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
                                        DateTimeFormatter formatter = b.appendPattern("yyyy-MM-dd").toFormatter();
                                        date.setValue(LocalDateTime.parse(defaultValue, formatter));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            tab.addComponent(date);
                            break;
                        case ItemTime.TYPE:
                            DateTimeField time = new DateTimeField();
                            if (defaultValue.length() == 0) {
                                time.setValue(LocalDateTime.now());
                                time.setDateFormat("HH:mm:ss");
                            } else {
                                try {
                                    if (defaultValue.trim().length() > 0) {
                                        DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
                                        DateTimeFormatter formatter = b.appendPattern("HH:mm:ss").toFormatter();
                                        time.setValue(LocalDateTime.parse(defaultValue, formatter));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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

//                        if (!jsonObject.has(Utilities.TAG_KEY)) {
//                            continue;
//                        }
//                        String key = jsonObject.getString(Utilities.TAG_KEY).trim();
//
//                        String value = null;
//                        if (jsonObject.has(Utilities.TAG_VALUE)) {
//                            value = jsonObject.get(Utilities.TAG_VALUE).toString().trim();
//                        }

                    }
                }

//                tab.addComponent(new Image(null,
//                    new ThemeResource("img/planets/"+formName+".jpg")));
            }
        });

        for( String name : formNames4Section ) {
            final VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            currentSelectedSectionTabSheet.addTab(layout, name);
        }

        formAreaLayout.addComponentsAndExpand(currentSelectedSectionTabSheet);
    }

    private void addNewWidget() {
        // TODO Auto-generated method stub

    }
    private void removeWidget() {
        // TODO Auto-generated method stub

    }

    private void addNewTab() {
        // TODO Auto-generated method stub

    }
    private void removeTab() {
        // TODO Auto-generated method stub

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
                            loadSection(curentSelectedSectionObject);
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
        String message = "<font color=\"red\"><h3>Are you sure you want to delete the Section: <b>" + curentSelectedSectionName
                + "</b></h3></font>";
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
