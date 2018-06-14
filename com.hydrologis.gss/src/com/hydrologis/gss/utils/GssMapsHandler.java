package com.hydrologis.gss.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.hydrologis.gss.map.GssMapBrowser;

import eu.hydrologis.stage.libs.map.EOnlineTileSources;
import eu.hydrologis.stage.libs.map.LeafletMapBrowser;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.registry.Settings;

public enum GssMapsHandler {
    INSTANCE;

    public static final String MAPSFORGE = "Mapsforge";
    public static String SETTINGS_KEY_MAPS = "GSS_SETTINGS_KEY_MAPS";

    private List<String> names;

    private GssMapsHandler() {
        names = new ArrayList<>();
        names.add(MAPSFORGE);
        for( EOnlineTileSources source : EOnlineTileSources.values() ) {
            names.add(source.getName());
        }
    }

    public List<String> getAllNames() {
        return names;
    }

    public List<String> getSelectedNames() throws SQLException {
        String colonSepMaps = RegistryHandler.INSTANCE.getSettingByKey(SETTINGS_KEY_MAPS,
                EOnlineTileSources.Open_Street_Map_Standard.getName());
        String[] split = colonSepMaps.split(";");
        return Arrays.asList(split);
    }

    public void putSelectedNames( List<String> names ) throws SQLException {
        String maps = names.stream().collect(Collectors.joining(";"));
        Settings newSetting = new Settings(SETTINGS_KEY_MAPS, maps);
        RegistryHandler.INSTANCE.insertOrUpdateSetting(newSetting);
    }

    public String getForMapBrowser( LeafletMapBrowser mapBrowser, String html, String mapName, boolean showMap ) {
        if (html == null)
            html = "";
        if (mapName.equals(MAPSFORGE)) {
            html += mapBrowser.getWorkspaceMapsforgeLayer(showMap);
        } else {
            EOnlineTileSources sources = EOnlineTileSources.getByName(mapName);
            html += sources.addToLeaflet(mapBrowser, showMap, false);
        }
        return html;
    }

    public String addSelectedMaps( GssMapBrowser mapBrowser ) throws SQLException {
        List<String> selectedNames = getSelectedNames();
        String html = "";

        if (selectedNames != null && selectedNames.size() > 0) {
            boolean isFirst = true;
            for( String name : selectedNames ) {
                html += getForMapBrowser(mapBrowser, "", name, isFirst);
                isFirst = false;
            }
        }
        return html;
    }

}
