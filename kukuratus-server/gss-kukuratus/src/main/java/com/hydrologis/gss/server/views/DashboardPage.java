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
import com.hydrologis.gss.server.GssDbProvider;
import com.hydrologis.gss.server.database.GssDatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.j256.ormlite.dao.Dao;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class DashboardPage extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;
    private GssDatabaseHandler dbHandler;

    @Override
    public void enter( ViewChangeEvent event ) {

        dbHandler = GssDbProvider.INSTANCE.getDatabaseHandler().get();
        try {
            long surveyorsCount = dbHandler.getDao(GpapUsers.class).countOf();
            long notesCount = dbHandler.getDao(Notes.class).countOf();
            long logsCount = dbHandler.getDao(GpsLogs.class).countOf();
            long mediaCount = dbHandler.getDao(Images.class).countOf();

            Label surveyLabel = getLabel("Surveyors: ", surveyorsCount);
            Label logsLabel = getLabel("GPS Logs: ", logsCount);
            Label notesLabel = getLabel("Notes: ", notesCount);
            Label imageslabel = getLabel("Media: ", mediaCount);
            HorizontalLayout numbers = new HorizontalLayout(surveyLabel, logsLabel, notesLabel, imageslabel);

            ChartJs chart = createChart();
//            chart.setStyleName("dashboard-labels-border", true);

            addComponents(numbers, chart);
            setExpandRatio(numbers, 1);
            setExpandRatio(chart, 4);

            numbers.setSizeFull();
            chart.setSizeFull();
//            setSizeFull();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Label getLabel( String string, long count ) {
        Label label = new Label("<h1>" + string + "<b>" + count + "</b></h1>");
        label.setContentMode(com.vaadin.shared.ui.ContentMode.HTML);
//        label.setStyleName("dashboard-labels-border", true);
        label.setSizeFull();
        return label;
    }

//    private ChartJs createChart() {
//        BarChartConfig config = new BarChartConfig();
//        config.data().labels("January", "February", "March", "April", "May", "June", "July")
//                .addDataset(new BarDataset().type().label("Dataset 1").backgroundColor("rgba(151,187,205,0.5)")
//                        .borderColor("white").borderWidth(2))
//                .addDataset(new LineDataset().type().label("Dataset 2").backgroundColor("rgba(151,187,205,0.5)")
//                        .borderColor("white").borderWidth(2))
//                .addDataset(new BarDataset().type().label("Dataset 3").backgroundColor("rgba(220,220,220,0.5)")).and();
//
//        config.options().responsive(true).title().display(true).position(Position.LEFT).text("Chart.js Combo Bar Line Chart")
//                .and().done();
//
//        List<String> labels = config.data().getLabels();
//        for( Dataset< ? , ? > ds : config.data().getDatasets() ) {
//            List<Double> data = new ArrayList<>();
//            for( int i = 0; i < labels.size(); i++ ) {
//                data.add((double) (Math.random() > 0.5 ? 1.0 : -1.0) * Math.round(Math.random() * 100));
//            }
//
//            if (ds instanceof BarDataset) {
//                BarDataset bds = (BarDataset) ds;
//                bds.dataAsList(data);
//            }
//
//            if (ds instanceof LineDataset) {
//                LineDataset lds = (LineDataset) ds;
//                lds.dataAsList(data);
//            }
//        }
//
//        ChartJs chart = new ChartJs(config);
//        chart.setJsLoggingEnabled(true);
//
//        return chart;
//    }
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

        config.options().responsive(true)//
                .title().display(true)//
                .position(Position.LEFT).text("Stats per Surveyor")//
                .and()
                .done();

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

//    private MultiBarChartData multiworkProgressChartData() throws Exception {
//        LinkedHashMap<Long, int[]> statsPerDevice = getStats();
//
//        MultiBarChartData bd = new MultiBarChartData();
//        bd.series.add("Notes");
//        bd.series.add("Images");
//        bd.series.add("Gps Logs");
//        bd.categories.add(new ArrayList<String>());
//        bd.categories.add(new ArrayList<String>());
//        bd.categories.add(new ArrayList<String>());
//        bd.values.add(new ArrayList<Number>());
//        bd.values.add(new ArrayList<Number>());
//        bd.values.add(new ArrayList<Number>());
//
//        Map<Long, String> deviceIdMap = dbp.getDatabaseHandler().getDao(GpapUsers.class).queryForAll().stream()
//                .collect(Collectors.toMap(gp -> gp.id, gp -> gp.name));
//
//        for( Entry<Long, int[]> entry : statsPerDevice.entrySet() ) {
//            Long deviceIdInternal = entry.getKey();
//            String deviceId = deviceIdMap.get(deviceIdInternal);
//            int[] values = entry.getValue();
//
//            for( int i = 1; i < 4; i++ ) {
//                bd.categories.get(i - 1).add(deviceId);
//                bd.values.get(i - 1).add(values[i]);
//            }
//        }
//        return bd;
//    }

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
}
