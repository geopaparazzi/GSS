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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.byteowls.vaadin.chartjs.ChartJs;
import com.byteowls.vaadin.chartjs.config.BarChartConfig;
import com.byteowls.vaadin.chartjs.data.BarDataset;
import com.byteowls.vaadin.chartjs.data.Data;
import com.byteowls.vaadin.chartjs.options.Position;
import com.byteowls.vaadin.chartjs.options.scale.Axis;
import com.byteowls.vaadin.chartjs.options.scale.CategoryScale;
import com.byteowls.vaadin.chartjs.options.scale.LinearScale;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.j256.ormlite.dao.Dao;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class DashboardPage extends VerticalLayout implements View, com.hydrologis.kukuratus.libs.spi.DashboardPage {
    private static final long serialVersionUID = 1L;
    private DatabaseHandler dbHandler;

    @Override
    public void enter( ViewChangeEvent event ) {

        dbHandler = SpiHandler.INSTANCE.getDbProvider().getDatabaseHandler().get();
        try {
            long surveyorsCount = dbHandler.getDao(GpapUsers.class).countOf();
            if (surveyorsCount == 0) {

                VerticalLayout layout = new VerticalLayout();
                Label noSurveyorsLabel = new Label("<h1><b>No surveyor data available yet!</b></h1><br/>");
                noSurveyorsLabel.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);
                noSurveyorsLabel.setWidth(null);
                layout.addComponent(noSurveyorsLabel);
                Link appLink = new Link("You might want to get the Android app to upload some data!",
                        new ExternalResource("https://play.google.com/store/apps/details?id=com.hydrologis.gssmobile"));
                appLink.setDescription("Geopaparazzi Survey Server Sync");
                appLink.setWidth(null);
                layout.addComponent(appLink);
                layout.setWidth(null);
                layout.setComponentAlignment(noSurveyorsLabel, Alignment.MIDDLE_CENTER);
                layout.setComponentAlignment(appLink, Alignment.MIDDLE_CENTER);

                addComponent(layout);
                setSizeFull();
                setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
            } else {
                long notesCount = dbHandler.getDao(Notes.class).countOf();
                long logsCount = dbHandler.getDao(GpsLogs.class).countOf();
                long mediaCount = dbHandler.getDao(Images.class).countOf();

                Label surveyLabel = getLabel("Surveyors: ", surveyorsCount);
                Label logsLabel = getLabel("GPS Logs: ", logsCount);
                Label notesLabel = getLabel("Notes: ", notesCount);
                Label imageslabel = getLabel("Media: ", mediaCount);
                HorizontalLayout numbers = new HorizontalLayout(surveyLabel, logsLabel, notesLabel, imageslabel);

                ChartJs chart = createChart();

                addComponents(numbers, chart);
                setExpandRatio(numbers, 1);
                setExpandRatio(chart, 4);

                numbers.setSizeFull();
                chart.setSizeFull();
            }
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private Label getLabel( String string, long count ) {
        Label label = new Label("<h1>" + string + "<b>" + count + "</b></h1>");
        label.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);
        label.setSizeFull();
        return label;
    }

    private ChartJs createChart() throws Exception {

        LinkedHashMap<Long, int[]> stats = getStats();

        Map<Long, String> deviceIdMap = dbHandler.getDao(GpapUsers.class).queryForAll().stream()
                .collect(Collectors.toMap(gp -> gp.id, gp -> gp.name));

        BarChartConfig config = new BarChartConfig();
        Data<BarChartConfig> data = config.data();
        data.labels(deviceIdMap.values().toArray(new String[0]));
        data.addDataset(
                new BarDataset().type().label("Notes").backgroundColor("rgba(255,0,0,0.5)").borderColor("white").borderWidth(2));
        data.addDataset(
                new BarDataset().type().label("Images").backgroundColor("rgba(0,255,0,0.5)").borderColor("white").borderWidth(2));
        data.addDataset(new BarDataset().type().label("Gps Logs").backgroundColor("rgba(0,0,225,0.5)").borderColor("white")
                .borderWidth(2)).and();

        LinearScale linearScale = new LinearScale();
        linearScale.ticks().beginAtZero(true);
        config.options().responsive(true)//
                .title().display(true)//
                .position(Position.LEFT).text("Stats per Surveyor")//
                .and().scales().add(Axis.X, new CategoryScale().position(Position.BOTTOM))
                .add(Axis.Y, linearScale.position(Position.LEFT)).and().done();

        List<Double> notesList = new ArrayList<>();
        List<Double> imagesList = new ArrayList<>();
        List<Double> logsList = new ArrayList<>();
        for( Entry<Long, int[]> entry : stats.entrySet() ) {
            int[] values = entry.getValue();
            notesList.add((double) values[1]);
            imagesList.add((double) values[2]);
            logsList.add((double) values[3]);
        }

        BarDataset notesDs = (BarDataset) config.data().getDatasetAtIndex(0);
        notesDs.dataAsList(notesList);
        BarDataset imagesDs = (BarDataset) config.data().getDatasetAtIndex(1);
        imagesDs.dataAsList(imagesList);
        BarDataset logsDs = (BarDataset) config.data().getDatasetAtIndex(2);
        logsDs.dataAsList(logsList);

        ChartJs chart = new ChartJs(config);
        chart.setJsLoggingEnabled(true);

        return chart;
    }

    public LinkedHashMap<Long, int[]> getStats() throws Exception {
        String notesSql = "select g.id, count(n.GPAPUSERSID) from gpapusers g left join notes n on g.id=n.GPAPUSERSID group by g.id order by g.id";
        String imagesSql = "select g.id, count(n.GPAPUSERSID ) from gpapusers g left join images n on g.id=n.GPAPUSERSID group by g.id order by g.id";
        String logsSql = "select g.id, count(n.GPAPUSERSID ) from gpapusers g left join gpslogs n on g.id=n.GPAPUSERSID group by g.id order by g.id";

        LinkedHashMap<Long, int[]> devicesMap = new LinkedHashMap<>();
        Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);
        notesDao.queryRaw(notesSql, rs -> {
            long deviceId = rs.getLong(0);
            int count = rs.getInt(1);

            int[] counts = devicesMap.get(deviceId);
            if (counts == null) {
                counts = new int[4];
                devicesMap.put(deviceId, counts);
            }
            counts[0] = (int) deviceId;
            counts[1] = count;
            return new Object[]{deviceId, count};
        }).getResults();
        Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);
        imagesDao.queryRaw(imagesSql, rs -> {
            long deviceId = rs.getLong(0);
            int count = rs.getInt(1);
            int[] counts = devicesMap.get(deviceId);
            if (counts == null) {
                counts = new int[4];
                devicesMap.put(deviceId, counts);
            }
            counts[0] = (int) deviceId;
            counts[2] = count;
            return new Object[]{deviceId, count};
        }).getResults();
        Dao<GpsLogs, ? > gpsLogsDao = dbHandler.getDao(GpsLogs.class);
        gpsLogsDao.queryRaw(logsSql, rs -> {
            long deviceId = rs.getLong(0);
            int count = rs.getInt(1);
            int[] counts = devicesMap.get(deviceId);
            if (counts == null) {
                counts = new int[4];
                devicesMap.put(deviceId, counts);
            }
            counts[0] = (int) deviceId;
            counts[3] = count;
            return new Object[]{deviceId, count};
        }).getResults();

        return devicesMap;
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.DASHBOARD;
    }

    @Override
    public String getLabel() {
        return "DashBoard";
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
