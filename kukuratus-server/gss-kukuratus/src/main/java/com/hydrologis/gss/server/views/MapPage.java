package com.hydrologis.gss.server.views;

import java.util.ArrayList;
import java.util.Arrays;

import org.vaadin.addon.leaflet.LCircle;
import org.vaadin.addon.leaflet.LCircleMarker;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.LRectangle;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletBaseLayerChangeEvent;
import org.vaadin.addon.leaflet.LeafletBaseLayerChangeListener;
import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.LeafletClickListener;
import org.vaadin.addon.leaflet.LeafletMoveEndEvent;
import org.vaadin.addon.leaflet.LeafletMoveEndListener;
import org.vaadin.addon.leaflet.LeafletOverlayAddEvent;
import org.vaadin.addon.leaflet.LeafletOverlayAddListener;
import org.vaadin.addon.leaflet.LeafletOverlayRemoveEvent;
import org.vaadin.addon.leaflet.LeafletOverlayRemoveListener;
import org.vaadin.addon.leaflet.shared.Bounds;
import org.vaadin.addon.leaflet.shared.Control;
import org.vaadin.addon.leaflet.shared.Point;

import com.hydrologis.kukuratus.libs.maps.MapComponent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ClassResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;

public class MapPage extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    private LMap leafletMap;
    private LMarker leafletMarker;
    private LeafletClickListener listener = new LeafletClickListener(){

        @Override
        public void onClick( LeafletClickEvent event ) {
            if (event.getPoint() != null) {
                Notification.show(String.format("Clicked %s @ %.4f,%.4f", event.getConnector().getClass().getSimpleName(),
                        event.getPoint().getLat(), event.getPoint().getLon()));

            } else {
                Notification.show(String.format("Clicked %s", event.getConnector().getClass().getSimpleName()));
            }
        }
    };

    @Override
    public void enter( ViewChangeEvent event ) {
        leafletMap = new LMap();
        leafletMap.setCenter(41.8919, 12.5113);
        leafletMap.setZoomLevel(15);

        leafletMap.setControls(new ArrayList<Control>(Arrays.asList(Control.values())));

        LPolyline leafletPolyline = new LPolyline(new Point(60.45, 22.295), new Point(60.4555, 22.301), new Point(60.45, 22.307));
        leafletPolyline.setColor("#FF00FF");
        leafletPolyline.setFill(true);
        leafletPolyline.setFillColor("#00FF00");
        leafletPolyline.setClickable(false);
        leafletPolyline.setWeight(8);
        leafletPolyline.setOpacity(0.5);
        leafletPolyline.setDashArray("15, 10, 5, 10, 15");
        leafletPolyline.addClickListener(listener);
        leafletMap.addComponent(leafletPolyline);

        LPolygon leafletPolygon = new LPolygon(new Point(60.455, 22.300), new Point(60.456, 22.302), new Point(60.50, 22.308));
        leafletPolygon.setColor("#FF00FF");
        leafletPolygon.setFill(true);
        leafletPolygon.setFillColor("#00FF00");
        leafletPolygon.addClickListener(listener);
        leafletMap.addComponent(leafletPolygon);

        Bounds bounds = new Bounds(new Point(60.45, 22.295), new Point(60.4509, 22.300));
        LRectangle rect = new LRectangle(bounds);
        rect.addClickListener(listener);
        rect.setFillColor("yellow");
        leafletMap.addLayer(rect);

        LCircle leafletCircle = new LCircle(60.4525, 22.301, 300);
        leafletCircle.setColor("#00FFFF");
        // leafletCircle.addClickListener(listener);
        leafletMap.addComponent(leafletCircle);
        leafletCircle.addClickListener(listener);

        LCircleMarker leafletCircleMarker = new LCircleMarker(60.4525, 22.301, 5);
        leafletCircleMarker.setColor("#FFFF00");
        leafletMap.addComponent(leafletCircleMarker);
        leafletCircleMarker.addClickListener(listener);

        leafletMarker = new LMarker(60.4525, 22.301);
        leafletMarker.addClickListener(listener);
        leafletMarker.setTitle("this is marker two!");
//      leafletMarker
//              .setDivIcon("this is a <h1>fabulous</h1> <span style=\"color:red\">icon</span>");
        leafletMarker.setPopup("Hello <b>world</b>");
        leafletMap.addComponent(leafletMarker);

        leafletMarker = new LMarker(60.4525, 22.301);
        leafletMarker.setIcon(new ClassResource("testicon.png"));
        leafletMarker.setIconSize(new Point(57, 52));
        leafletMarker.setIconAnchor(new Point(57, 26));
        leafletMarker.addClickListener(listener);
        leafletMarker.setTitle("this is marker one!");
        leafletMarker.setPopup("Hello <b>Vaadin World</b>!");
        leafletMarker.setPopupAnchor(new Point(-28, -26));

        leafletMap.addComponent(leafletMarker);
        leafletMap.setAttributionPrefix("Powered by Leaflet with v-leaflet");

        LPolyline roadOverlay = new LPolyline(new Point(61.42, 21.245), new Point(60.4655, 22.321), new Point(60.45, 22.307));
        leafletMap.addOverlay(roadOverlay, "Road overlay");

        LMarker point1 = new LMarker(61.441, 21.2442);
        LMarker point2 = new LMarker(61.445, 21.2441);
        LMarker point3 = new LMarker(61.447, 21.2445);
        LLayerGroup layerGroup = new LLayerGroup(point1, point2, point3);
        leafletMap.addOverlay(layerGroup, "Points overlay");

        leafletMap.addBaseLayer(new LOpenStreetMapLayer(), "CloudMade");

        // This will make everything sharper on "retina devices", but also text
        // quite small
        // baselayer.setDetectRetina(true);

        LTileLayer pk = new LTileLayer();
        pk.setUrl("./mapsforge?z={z}&x={x}&y={y}");
        pk.setAttributionString("Mapsforge");
        pk.setMaxZoom(22);
//        pk.setSubDomains("tile2");
        pk.setDetectRetina(true);

        leafletMap.addBaseLayer(pk, "Mapsforge");

        leafletMap.addBaseLayerChangeListener(new LeafletBaseLayerChangeListener(){
            @Override
            public void onBaseLayerChange( LeafletBaseLayerChangeEvent event ) {
                Notification.show(event.getName() + " base layer was activated!");
            }
        });

        leafletMap.addOverlayAddListener(new LeafletOverlayAddListener(){
            @Override
            public void onOverlayAdd( LeafletOverlayAddEvent event ) {
                Notification.show(event.getName() + " overlay was added!");
            }
        });

        leafletMap.addOverlayRemoveListener(new LeafletOverlayRemoveListener(){
            @Override
            public void onOverlayRemove( LeafletOverlayRemoveEvent event ) {
                Notification.show(event.getName() + " overlay was removed!");
            }
        });

        leafletMap.addClickListener(listener);

        leafletMap.addMoveEndListener(new LeafletMoveEndListener(){
            @Override
            public void onMoveEnd( LeafletMoveEndEvent event ) {
                Bounds b = event.getBounds();
                Notification.show(String.format("New viewport (%.4f,%.4f ; %.4f,%.4f)", b.getSouthWestLat(), b.getSouthWestLon(),
                        b.getNorthEastLat(), b.getNorthEastLon()), Type.TRAY_NOTIFICATION);
            }
        });

        addComponent(leafletMap);
        leafletMap.setSizeFull();
        setExpandRatio(leafletMap, 1);

        setSizeFull();
    }
}
