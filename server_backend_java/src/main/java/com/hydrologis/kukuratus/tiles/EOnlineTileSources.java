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
package com.hydrologis.kukuratus.tiles;

/**
 * Collection of online tile sources and configurations.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public enum EOnlineTileSources {
    Google_Maps("Google Maps", "", "", "Google", "https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    Google_Satellite("Google Satellite", "", "", "Google", "https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    Google_Terrain("Google Terrain", "", "", "Google", "https://mt1.google.com/vt/lyrs=t&x={x}&y={y}&z={z}", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    Google_Terrain_Hybrid("Google Terrain Hybrid", "", "", "Google", "https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$
    Google_Satellite_Hybrid("Google Satellite Hybrid", "", "", "Google", "https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$
    Stamen_Terrain("Stamen Terrain", "", "", "Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tile.stamen.com/terrain/{z}/{x}/{y}.png", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Stamen_Toner("Stamen Toner", "", "", "Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tile.stamen.com/toner/{z}/{x}/{y}.png", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Stamen_Toner_Light("Stamen Toner Light", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL", //$NON-NLS-1$
            "http://tile.stamen.com/toner-lite/{z}/{x}/{y}.png", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Stamen_Watercolor("Stamen Watercolor", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL", //$NON-NLS-1$
            "http://tile.stamen.com/watercolor/{z}/{x}/{y}.jpg", "", "18", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Wikimedia_Map("Wikimedia Map", "", "", "OpenStreetMap contributors, under ODbL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png", "", "20", "1"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Wikimedia_Hike_Bike_Map("Wikimedia Hike Bike Map", "", "", "OpenStreetMap contributors, under ODbL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png", "", "17", "1"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Esri_Boundaries_Places("Esri Boundaries Places", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}", //$NON-NLS-1$
            "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Esri_Gray_dark("Esri Gray (dark)", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}", "", //$NON-NLS-1$ //$NON-NLS-2$
            "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$
    Esri_Gray_light("Esri Gray (light)", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}", "", //$NON-NLS-1$ //$NON-NLS-2$
            "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$
    Esri_National_Geographic("Esri National Geographic", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Esri_Ocean("Esri Ocean", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://services.arcgisonline.com/ArcGIS/rest/services/Ocean/World_Ocean_Base/MapServer/tile/{z}/{y}/{x}", "", "20", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "0"), // //$NON-NLS-1$
    Esri_Satellite("Esri Satellite", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Esri_Standard("Esri Standard", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Esri_Terrain("Esri Terrain", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Esri_Transportation("Esri Transportation", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Transportation/MapServer/tile/{z}/{y}/{x}", "", //$NON-NLS-1$ //$NON-NLS-2$
            "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$
    Esri_World_Imagery("Esri World Imagery", "", "https://wiki.openstreetmap.org/wiki/Esri", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", "", "22", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Esri_Topo_World("Esri Topo World", "", "", "Esri", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Open_Street_Map_Standard("Open Street Map", "", "", "OpenStreetMap contributors, CC-BY-SA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Open_Street_Map_HOT("Open Street Map H.O.T.", "", "", "OpenStreetMap contributors, CC-BY-SA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tile.openstreetmap.fr/hot/{z}/{x}/{y}.png", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Open_Street_Map_Monochrome("Open Street Map Monochrome", "", "", "OpenStreetMap contributors, CC-BY-SA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Strava_All("Strava All", "", "", "OpenStreetMap contributors, CC-BY-SA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://heatmap-external-b.strava.com/tiles/all/bluered/{z}/{x}/{y}.png", "", "15", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Strava_Run("Strava Run", "", "", "OpenStreetMap contributors, CC-BY-SA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "https://heatmap-external-b.strava.com/tiles/run/bluered/{z}/{x}/{y}.png?v=19", "", "15", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Open_Weather_Map_Temperature("Open Weather Map Temperature", "", "", "Map tiles by OpenWeatherMap, under CC BY-SA 4.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tile.openweathermap.org/map/temp_new/{z}/{x}/{y}.png?APPID=1c3e4ef8e25596946ee1f3846b53218a", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Open_Weather_Map_Clouds("Open Weather Map Clouds", "", "", "Map tiles by OpenWeatherMap, under CC BY-SA 4.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tile.openweathermap.org/map/clouds_new/{z}/{x}/{y}.png?APPID=ef3c5137f6c31db50c4c6f1ce4e7e9dd", "", "19", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "0"), // //$NON-NLS-1$
    Open_Weather_Map_Wind_Speed("Open Weather Map Wind Speed", "", "", "Map tiles by OpenWeatherMap, under CC BY-SA 4.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://tile.openweathermap.org/map/wind_new/{z}/{x}/{y}.png?APPID=f9d0069aa69438d52276ae25c1ee9893", "", "19", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    CartoDb_Dark_Matter("CartoDb Dark Matter", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "Map tiles by CartoDB, under CC BY 3.0. Data by OpenStreetMap, under ODbL.", //$NON-NLS-1$
            "http://basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png", "", "20", "0"), // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    CartoDb_Positron("CartoDb Positron", "", "", " Map tiles by CartoDB, under CC BY 3.0. Data by OpenStreetMap, under ODbL.", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "http://basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png", "", "20", "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    private String _name;
    private String _attribution;
    private String _url;
    private String _maxZoom;

    private EOnlineTileSources( String name, String arg1, String arg2, String attribution, String url, String arg3,
            String maxZoom, String minZoom ) {
        _name = name;
        _attribution = attribution;
        _url = url;
        _maxZoom = maxZoom;
    }

    public String getName() {
        return _name;
    }

    public String getUrl() {
        return _url;
    }

    public String getAttribution() {
        return _attribution;
    }

    public String getMaxZoom() {
        return _maxZoom;
    }

    /**
     * Get a source by its name.
     * 
     * @param name the source name.
     * @return the source or OSM if it doesn't exist.
     */
    public static EOnlineTileSources getByName( String name ) {
        for( EOnlineTileSources source : values() ) {
            if (name.equals(source.getName())) {
                return source;
            }
        }
        return Open_Street_Map_Standard;
    }
}
