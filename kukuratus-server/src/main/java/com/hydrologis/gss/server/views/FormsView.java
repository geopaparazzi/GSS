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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemBoolean;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemConnectedCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDate;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDouble;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemDynamicText;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemInteger;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemLabel;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemOneToManyConnectedCombo;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemPicture;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemSketch;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemText;
import org.hortonmachine.gears.io.geopaparazzi.forms.items.ItemTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.database.objects.Forms;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class FormsView extends VerticalLayout implements View, DefaultPage {

    private String authenticatedUsername;

    private Dao<Forms, ? > formsDAO;

    private Forms currentSelectedTags = null;

    private VerticalLayout formAndMenubarAreaLayout;
    private VerticalLayout formAreaLayout;

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
            Button deleteTag = new Button("Remove");
            deleteTag.setStyleName(ValoTheme.BUTTON_DANGER);
            deleteTag.setVisible(false);

            ComboBox<Forms> tagsCombo = new ComboBox<>();
            tagsCombo.setItems(formsList);
            tagsCombo.setPlaceholder("No form selected");
            tagsCombo.setItemCaptionGenerator(Forms::getName);
            tagsCombo.setEmptySelectionAllowed(false);
            tagsCombo.addSelectionListener(new SingleSelectionListener<Forms>(){
                @Override
                public void selectionChange( SingleSelectionEvent<Forms> event ) {
                    Optional<Forms> selectedForm = event.getFirstSelectedItem();
                    selectedForm.ifPresent(form -> {
                        selectForm(form);
                        deleteTag.setVisible(true);
                    });
                }
            });

            HorizontalLayout tagsLayout = new HorizontalLayout(tagsCombo, addTag, deleteTag);
            addComponent(tagsLayout);

            formAndMenubarAreaLayout = new VerticalLayout();
            Panel placeHolder = new Panel();
            placeHolder.addStyleName(ValoTheme.PANEL_WELL);
            placeHolder.setSizeFull();
            formAndMenubarAreaLayout.addComponent(placeHolder);
            addComponentsAndExpand(formAndMenubarAreaLayout);

            setSizeFull();

        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    protected void selectForm( Forms form ) {
        currentSelectedTags = form;

        LinkedHashMap<String, JSONObject> sectionsMap = Utilities.getSectionsFromJsonString(form.form);

        formAndMenubarAreaLayout.removeAllComponents();

        MenuBar menuBar = new MenuBar();
        menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

        MenuItem sectionsItem = menuBar.addItem("Sections", VaadinIcons.TASKS, null);
        sectionsMap.keySet().stream().forEach(sectionName -> {
            sectionsItem.addItem(sectionName, VaadinIcons.FOLDER_OPEN, i -> {
                loadSection(sectionsMap.get(sectionName));
            });
        });
        sectionsItem.addSeparator();
        sectionsItem.addItem("add new", VaadinIcons.PLUS, i -> {
            removeTab();
        });
        sectionsItem.addItem("remove", VaadinIcons.MINUS, i -> {
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
    }

    private void loadSection( JSONObject sectionObject ) {
        if (formAreaLayout == null) {
            throw new RuntimeException();
        }
        formAreaLayout.removeAllComponents();

        List<String> formNames4Section = Utilities.getFormNames4Section(sectionObject);

        TabSheet sectionTabs = new TabSheet();
        sectionTabs.setHeight(100.0f, Unit.PERCENTAGE);
        sectionTabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        sectionTabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        sectionTabs.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener(){
            public void selectedTabChange( SelectedTabChangeEvent event ) {
                // Find the tabsheet
                TabSheet tabsheet = event.getTabSheet();

                // Find the tab (here we know it's a layout)
                Layout tab = (Layout) tabsheet.getSelectedTab();

                String formName = tabsheet.getTab(tab).getCaption();
                JSONObject formJson = Utilities.getForm4Name(formName, sectionObject);
                tab.removeAllComponents();

                JSONArray formItems = Utilities.getFormItems(formJson);
                for( int i = 0; i < formItems.length(); i++ ) {
                    JSONObject jsonObject = formItems.getJSONObject(i);
                    if (jsonObject.has(Utilities.TAG_TYPE)) {
                        String type = jsonObject.getString(Utilities.TAG_TYPE).trim();
                        switch( type ) {
                        case ItemBoolean.TYPE:
                            tab.addComponent(new Label(ItemBoolean.TYPE));
                            break;
                        case ItemCombo.TYPE:
                            tab.addComponent(new Label(ItemCombo.TYPE));
                            break;
                        case ItemConnectedCombo.TYPE:
                            tab.addComponent(new Label(ItemConnectedCombo.TYPE));
                            break;
                        case ItemDate.TYPE:
                            tab.addComponent(new Label(ItemDate.TYPE));
                            break;
                        case ItemDouble.TYPE:
                            tab.addComponent(new Label(ItemDouble.TYPE));
                            break;
                        case ItemDynamicText.TYPE:
                            tab.addComponent(new Label(ItemDynamicText.TYPE));
                            break;
                        case ItemInteger.TYPE:
                            tab.addComponent(new Label(ItemInteger.TYPE));
                            break;
                        case ItemLabel.TYPE:
                            tab.addComponent(new Label(ItemLabel.TYPE));
                            break;
                        case ItemOneToManyConnectedCombo.TYPE:
                            tab.addComponent(new Label(ItemOneToManyConnectedCombo.TYPE));
                            break;
                        case ItemPicture.TYPE:
                            tab.addComponent(new Label(ItemPicture.TYPE));
                            break;
                        case ItemSketch.TYPE:
                            tab.addComponent(new Label(ItemSketch.TYPE));
                            break;
                        case ItemText.TYPE:
                            tab.addComponent(new Label(ItemText.TYPE));
                            break;
                        case ItemTime.TYPE:
                            tab.addComponent(new Label(ItemTime.TYPE));
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
            sectionTabs.addTab(layout, name);
        }

        formAreaLayout.addComponentsAndExpand(sectionTabs);
    }

    private void removeWidget() {
        // TODO Auto-generated method stub

    }

    private void addNewWidget() {
        // TODO Auto-generated method stub

    }

    private void addNewTab() {
        // TODO Auto-generated method stub

    }

    private void removeSection() {
        // TODO Auto-generated method stub

    }

    private void removeTab() {
        // TODO Auto-generated method stub

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
