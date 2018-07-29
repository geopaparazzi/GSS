package com.hydrologis.gss.server.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.control.LLayers;

import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.maps.EOnlineTileSources;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class MapChooserView extends VerticalLayout implements View {
    private static final String TMP_MAP = "TmpMap";
    private static final long serialVersionUID = 1L;
    private List<String> unselectedMapNames;
    private List<String> selectedMapNames;
    private LMap leafletMap;
    private LTileLayer tmpLayer;
    private volatile boolean isAdding = false;

    @Override
    public void enter( ViewChangeEvent event ) {
        HorizontalLayout hLayout = new HorizontalLayout();

        unselectedMapNames = RegistryHandler.INSTANCE.getAllTileSourcesNames();
        selectedMapNames = RegistryHandler.INSTANCE.getSelectedTileSourcesNames(AuthService.INSTANCE.getAuthenticatedUsername());
        unselectedMapNames.removeAll(selectedMapNames);

        ListSelect<String> unselectedList = new ListSelect<>("Available maps", sort(unselectedMapNames));
        unselectedList.setRows(15);
        unselectedList.setWidth(100.0f, Unit.PERCENTAGE);

        VerticalLayout btnLayout = new VerticalLayout();
        Label d1 = new Label();
        Label d2 = new Label();
        btnLayout.addComponent(d1);
        Button add = new Button(VaadinIcons.CHEVRON_RIGHT);
        Button remove = new Button(VaadinIcons.CHEVRON_LEFT);
        CssLayout btnCssLayout = new CssLayout(remove, add);
        btnCssLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        btnLayout.addComponent(btnCssLayout);
        btnLayout.addComponent(d2);

        btnLayout.setExpandRatio(d1, 4);
        btnLayout.setExpandRatio(btnCssLayout, 2);
        btnLayout.setExpandRatio(d2, 4);

        ListSelect<String> selectedList = new ListSelect<>("Selected map", sort(selectedMapNames));
        selectedList.setRows(15);
        selectedList.setWidth(100.0f, Unit.PERCENTAGE);

        unselectedList.addValueChangeListener(e -> {
            if (isAdding) {
                return;
            }
            Set<String> items = unselectedList.getSelectedItems();
            String[] names = items.toArray(new String[0]);
            String name = names[names.length - 1];

            leafletMap.removeLayer(tmpLayer);
            if (name.equals(RegistryHandler.MAPSFORGE)) {
                tmpLayer = new LTileLayer();
                tmpLayer.setUrl("./mapsforge?z={z}&x={x}&y={y}");
                tmpLayer.setAttributionString("Mapsforge");
                tmpLayer.setMaxZoom(22);
            } else {
                EOnlineTileSources source = EOnlineTileSources.getByName(name);
                tmpLayer = new LTileLayer();
                tmpLayer.setUrl(source.getUrl());
                tmpLayer.setAttributionString(source.getAttribution());
                tmpLayer.setMaxZoom(Integer.parseInt(source.getMaxZoom()));
                tmpLayer.setDetectRetina(true);
            }
            leafletMap.addBaseLayer(tmpLayer, TMP_MAP);
        });

        add.addClickListener(e -> {
            isAdding = true;
            Set<String> items = unselectedList.getSelectedItems();
            if (!items.isEmpty()) {
                unselectedMapNames.removeAll(items);
                selectedMapNames.addAll(items);
                unselectedList.setItems(sort(unselectedMapNames));
                selectedList.setItems(sort(selectedMapNames));
                RegistryHandler.INSTANCE.putSelectedTileSourcesNames(selectedMapNames,
                        AuthService.INSTANCE.getAuthenticatedUsername());
            }
            isAdding = false;
        });
        remove.addClickListener(e -> {
            Set<String> items = selectedList.getSelectedItems();
            if (!items.isEmpty()) {
                unselectedMapNames.addAll(items);
                selectedMapNames.removeAll(items);
                unselectedList.setItems(sort(unselectedMapNames));
                selectedList.setItems(sort(selectedMapNames));
                RegistryHandler.INSTANCE.putSelectedTileSourcesNames(selectedMapNames,
                        AuthService.INSTANCE.getAuthenticatedUsername());
            }
        });

        hLayout.addComponents(unselectedList, btnLayout, selectedList);
//        selectedList.setSizeFull();
//        unselectedList.setSizeFull();
        hLayout.setExpandRatio(btnLayout, 1);
        hLayout.setExpandRatio(selectedList, 4);
        hLayout.setExpandRatio(unselectedList, 4);
        hLayout.setSizeFull();

        leafletMap = new LMap();
        leafletMap.setCenter(41.8919, 12.5113);
        leafletMap.setZoomLevel(15);
        LLayers layersControl = leafletMap.getLayersControl();
        leafletMap.removeControl(layersControl);
        leafletMap.setControls(new ArrayList<>());

        tmpLayer = new LTileLayer();
        tmpLayer.setUrl(EOnlineTileSources.Open_Street_Map_Standard.getUrl());
        tmpLayer.setAttributionString(EOnlineTileSources.Open_Street_Map_Standard.getAttribution());
        tmpLayer.setMaxZoom(Integer.parseInt(EOnlineTileSources.Open_Street_Map_Standard.getMaxZoom()));
        tmpLayer.setDetectRetina(true);
        leafletMap.addBaseLayer(tmpLayer, TMP_MAP);

        addComponent(hLayout);
        addComponent(leafletMap);
        leafletMap.setSizeFull();
//        setExpandRatio(leafletMap, 1);

        setSizeFull();
    }

    private List<String> sort( List<String> list ) {
        Collections.sort(list);
        return list;
    }
}
