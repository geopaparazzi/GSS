/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package com.hydrologis.gss.entrypoints;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.GssDbProvider;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.j256.ormlite.dao.Dao;

import eu.hydrologis.stage.libs.charts.MultiBarChartBrowser;
import eu.hydrologis.stage.libs.charts.MultiBarChartData;
import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.html.HtmlFeatureChooser;
import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.IDoubleClickListener;
import eu.hydrologis.stage.libs.utils.StageUtils;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DashboardEntryPoint extends StageEntryPoint implements IDoubleClickListener {

    private static final String INTEGER_AXIS_FORMAT = "',f'";

    public static final String ID = "com.hydrologis.gss.entrypoints.DashboardEntryPoint";

    private static final long serialVersionUID = 1L;

    private Shell parentShell;

    // private BarChartBrowser leaksPerDayBrowser;
    //
    // private BarChartBrowser leaksPerMachineBrowser;
    //
    // private TimeSeriesChartBrowser cumulatesLeaksBrowser;
    //
    // private SashForm mainSash;
    //
    // private SashForm upperChartsSash;
    //
    // private SashForm lowerChartsSash;

    private Group workProgressGroup;

    // private Group leaksPerDayGroup;
    //
    // private Group cumulataGroup;
    //
    // private Group leaksPerMachineGroup;
    //
    // private Combo plantsCmbo;

    private MultiBarChartBrowser multiworkProgressBrowser;

    private GssDbProvider dbp;

    @Override
    protected void createPage( final Composite parent ) {
        // Login screen
        eu.hydrologis.stage.libs.registry.User loggedUser = GssLoginDialog.checkUserLogin(getShell());
        boolean isAdmin = RegistryHandler.INSTANCE.isAdmin(loggedUser);
        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(isAdmin);
        if (loggedUser == null || !permissionsHandler.isAllowed(redirectUrl)) {
            StageUtils.permissionDeniedPage(parent, redirectUrl);
            return;
        }

        String name = loggedUser.getName();
        String uniquename = loggedUser.getUniqueName();
        StageUtils.logAccessIp(redirectUrl, "user:" + uniquename);

        parentShell = parent.getShell();
        dbp = GssContext.instance().getDbProvider();

        final Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(2, false));

        final Composite horizontalToolbarComposite = new Composite(mainComposite, SWT.NONE);
        GridData horizToolbarGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        horizToolbarGD.horizontalSpan = 2;
        horizontalToolbarComposite.setLayoutData(horizToolbarGD);
        GridLayout toolbarLayout = new GridLayout(7, false);
        horizontalToolbarComposite.setLayout(toolbarLayout);

        final Composite vertToolbarComposite = new Composite(mainComposite, SWT.NONE);
        vertToolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout vertToolbarLayout = new GridLayout(1, false);
        vertToolbarComposite.setLayout(vertToolbarLayout);

        GssGuiUtilities.addLogo(horizontalToolbarComposite);
        GssGuiUtilities.addPagesLinks(vertToolbarComposite, this, isAdmin);
        // try {
        // createDevicesCombo(horizontalToolbarComposite);
        // } catch (Exception e) {
        // StageLogger.logError(this, e);
        // }
        GssGuiUtilities.addVerticalFiller(vertToolbarComposite);
        GssGuiUtilities.addHorizontalFiller(horizontalToolbarComposite);
        GssGuiUtilities.addAdminTools(vertToolbarComposite, this, isAdmin);
        GssGuiUtilities.addLogoutButton(horizontalToolbarComposite);

        // mainSash = new SashForm(mainComposite, SWT.VERTICAL);
        // GridData mainGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        // mainSash.setLayoutData(mainGD);
        //
        // upperChartsSash = new SashForm(mainSash, SWT.HORIZONTAL);
        // GridData upperChartsGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        // upperChartsSash.setLayoutData(upperChartsGD);
        //
        // lowerChartsSash = new SashForm(mainSash, SWT.HORIZONTAL);
        // GridData lowerChartsGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        // lowerChartsSash.setLayoutData(lowerChartsGD);

        try {
            // cumulataGroup = new Group(upperChartsSash, parentShell.getStyle());
            // GridData cumulataGroupGD = new GridData(GridData.FILL, GridData.FILL, true, true);
            // cumulataGroup.setLayoutData(cumulataGroupGD);
            // cumulataGroup.setText("Leaks count over time");
            // cumulataGroup.setLayout(new FillLayout());
            //
            // cumulatesLeaksBrowser = new TimeSeriesChartBrowser(cumulataGroup, SWT.BORDER);
            // String timeChartHtml = cumulatesLeaksBrowser.getChartHtml();
            // timeChartHtml = HtmlFeatureChooser.INSTANCE.addDebugging(timeChartHtml);
            // cumulatesLeaksBrowser.setText(timeChartHtml);
            // cumulatesLeaksBrowser.addDoubleClickListener(this);

            Composite marginsComposite = new Composite(mainComposite, SWT.NONE);
            marginsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout marginsLayout = new GridLayout(1, false);
            marginsLayout.marginTop = 10;
            marginsLayout.marginBottom = 40;
            marginsLayout.marginLeft = 20;
            marginsLayout.marginRight = 60;
            marginsComposite.setLayout(marginsLayout);
            
            
            workProgressGroup = new Group(marginsComposite, parentShell.getStyle());
            GridData workProgressGD = new GridData(GridData.FILL, GridData.FILL, true, true);
            workProgressGroup.setLayoutData(workProgressGD);
            FillLayout workProgressLayout = new FillLayout();
            workProgressGroup.setLayout(workProgressLayout);
            workProgressGroup.setText("Survey Info per Device");

            MultiBarChartData workProgressCd = multiworkProgressChartData();
            multiworkProgressBrowser = new MultiBarChartBrowser(workProgressGroup, SWT.BORDER);
            multiworkProgressBrowser.setChartData(workProgressCd);
            multiworkProgressBrowser.setColor("#88c58a");
            multiworkProgressBrowser.setyAxisFormat(INTEGER_AXIS_FORMAT);
            String multichartHtml = multiworkProgressBrowser.getChartHtml();
            multichartHtml = HtmlFeatureChooser.INSTANCE.addDebugging(multichartHtml);
            multiworkProgressBrowser.setText(multichartHtml);
            multiworkProgressBrowser.addDoubleClickListener(this);

            // leaksPerDayGroup = new Group(lowerChartsSash, parentShell.getStyle());
            // GridData leaksPerDayGD = new GridData(GridData.FILL, GridData.FILL, true, true);
            // leaksPerDayGroup.setLayoutData(leaksPerDayGD);
            // leaksPerDayGroup.setLayout(new FillLayout());
            // leaksPerDayGroup.setText("Current Leaks per day");
            //
            // leaksPerDayBrowser = new BarChartBrowser(leaksPerDayGroup, SWT.BORDER);
            // leaksPerDayBrowser.setBarColor("#a84a4a");
            // leaksPerDayBrowser.setyAxisFormat(INTEGER_AXIS_FORMAT);
            // String perDayChartHtml = leaksPerDayBrowser.getChartHtml();
            // perDayChartHtml = HtmlFeatureChooser.INSTANCE.addDebugging(perDayChartHtml);
            // leaksPerDayBrowser.setText(perDayChartHtml);
            // leaksPerDayBrowser.addDoubleClickListener(this);
            //
            // leaksPerMachineGroup = new Group(lowerChartsSash, parentShell.getStyle());
            // GridData leaksPerMachineGD = new GridData(GridData.FILL, GridData.FILL, true, true);
            // leaksPerMachineGroup.setLayoutData(leaksPerMachineGD);
            // leaksPerMachineGroup.setLayout(new FillLayout());
            // leaksPerMachineGroup.setText("Current Leaks per Device");
            //
            // leaksPerMachineBrowser = new BarChartBrowser(leaksPerMachineGroup, SWT.BORDER);
            // leaksPerMachineBrowser.setBarColor("#3e8ede");
            // leaksPerMachineBrowser.setyAxisFormat(INTEGER_AXIS_FORMAT);
            // leaksPerMachineBrowser.setText(perDayChartHtml);
            // leaksPerMachineBrowser.addDoubleClickListener(this);
            //
            // mainSash.setWeights(new int[]{5, 5});
            // upperChartsSash.setWeights(new int[]{5, 5});
            // lowerChartsSash.setWeights(new int[]{5, 5});
        } catch (Exception e1) {
            StageLogger.logError(this, e1);
        }

        GssGuiUtilities.addFooter(name, mainComposite);

    }

    // private BarChartData leakPerDayChartData( List<SensitLeaks> leaksList ) {
    // DateTimeFormatter dateTimeFormatterYYYYMMDD = DateTimeFormat.forPattern("MM/dd");
    // TreeMap<String, Integer> time2CountMap = new TreeMap<>();
    // for( SensitLeaks leak : leaksList ) {
    // String timestamp = new DateTime(leak.timestamp).toString(dateTimeFormatterYYYYMMDD);
    // Integer count = time2CountMap.get(timestamp);
    // if (count == null) {
    // count = 1;
    // } else {
    // count = count + 1;
    // }
    // time2CountMap.put(timestamp, count);
    // }
    //
    // BarChartData bd = new BarChartData();
    // for( Entry<String, Integer> entry : time2CountMap.entrySet() ) {
    // bd.categories.add(entry.getKey());
    // bd.values.add(entry.getValue());
    // }
    // return bd;
    // }

    // private BarChartData workProgressChartData() throws Exception {
    // LinkedHashMap<String, Integer> workStatsPerPlant =
    // SensitDatabaseUtilities.getWorkStatsPerPlant();
    //
    // BarChartData bd = new BarChartData();
    // for( Entry<String, Integer> entry : workStatsPerPlant.entrySet() ) {
    // bd.categories.add(entry.getKey());
    // bd.values.add(entry.getValue());
    // }
    // return bd;
    // }

    private MultiBarChartData multiworkProgressChartData() throws Exception {
        LinkedHashMap<Long, int[]> statsPerDevice = getStats();

        MultiBarChartData bd = new MultiBarChartData();
        bd.series.add("Notes");
        bd.series.add("Images");
        bd.series.add("Gps Logs");
        bd.categories.add(new ArrayList<String>());
        bd.categories.add(new ArrayList<String>());
        bd.categories.add(new ArrayList<String>());
        bd.values.add(new ArrayList<Number>());
        bd.values.add(new ArrayList<Number>());
        bd.values.add(new ArrayList<Number>());

        Map<Long, String> deviceIdMap = dbp.getDatabaseHandler().getDao(GpapUsers.class).queryForAll().stream()
                .collect(Collectors.toMap(gp -> gp.id, gp -> gp.deviceId));

        for( Entry<Long, int[]> entry : statsPerDevice.entrySet() ) {
            Long deviceIdInternal = entry.getKey();
            String deviceId = deviceIdMap.get(deviceIdInternal);
            int[] values = entry.getValue();

            for( int i = 0; i < 3; i++ ) {
                bd.categories.get(i).add(deviceId);
                bd.values.get(i).add(values[i]);
            }
        }
        return bd;
    }

    public LinkedHashMap<Long, int[]> getStats() throws Exception {
        String notesSql = "select g.id, count(n.GPAPUSERSID) from gpapusers g left join notes n on g.id=n.GPAPUSERSID group by g.id order by g.id";
        String imagesSql = "select g.id, count(n.GPAPUSERSID ) from gpapusers g left join images n on g.id=n.GPAPUSERSID group by g.id order by g.id";
        String logsSql = "select g.id, count(n.GPAPUSERSID ) from gpapusers g left join gpslogs n on g.id=n.GPAPUSERSID group by g.id order by g.id";

        DatabaseHandler dbHandler = dbp.getDatabaseHandler();

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

    // private TimeSeriesChartData leakCumulChartData( List<SensitLeaks> leaksList ) {
    // TreeMap<Long, Integer> time2CountMap = new TreeMap<>();
    // for( SensitLeaks leak : leaksList ) {
    // // String timestamp = new DateTime(leak.timestamp).toString(dateTimeFormatterYYYYMMDD);
    // Integer count = time2CountMap.get(leak.timestamp);
    // if (count == null) {
    // count = 1;
    // } else {
    // count = count + 1;
    // }
    // time2CountMap.put(leak.timestamp, count);
    // }
    //
    // TimeSeriesChartData bd = new TimeSeriesChartData();
    // bd.series.add("Cumulated");
    //
    // List<Long> tsList = new ArrayList<>();
    // List<Number> valuesList = new ArrayList<>();
    // bd.timestamps.add(tsList);
    // bd.values.add(valuesList);
    // int cum = 0;
    // for( Entry<Long, Integer> entry : time2CountMap.entrySet() ) {
    // tsList.add(entry.getKey());
    // Integer value = entry.getValue();
    // cum += value;
    // valuesList.add(cum);
    // }
    // return bd;
    // }

    // private BarChartData leakPerDeviceChartData( List<SensitLeaks> leaksList ) {
    // TreeMap<String, Integer> id2CountMap = new TreeMap<>();
    // for( SensitLeaks leak : leaksList ) {
    // String deviceId = leak.device;
    //
    // Integer count = id2CountMap.get(deviceId);
    // if (count == null) {
    // count = 1;
    // } else {
    // count = count + 1;
    // }
    // id2CountMap.put(deviceId, count);
    // }
    //
    // BarChartData bd = new BarChartData();
    // for( Entry<String, Integer> entry : id2CountMap.entrySet() ) {
    // bd.categories.add(entry.getKey());
    // bd.values.add(entry.getValue());
    // }
    // return bd;
    // }

    @Override
    public void onDoubleClick( Control caller ) {
        // if (isMaximized) {
        // mainSash.setMaximizedControl(null);
        // upperChartsSash.setMaximizedControl(null);
        // lowerChartsSash.setMaximizedControl(null);
        // isMaximized = false;
        // } else {
        // if (caller == multiworkProgressBrowser) {
        // mainSash.setMaximizedControl(upperChartsSash);
        // upperChartsSash.setMaximizedControl(workProgressGroup);
        // isMaximized = true;
        // } else if (caller == cumulatesLeaksBrowser) {
        // mainSash.setMaximizedControl(upperChartsSash);
        // upperChartsSash.setMaximizedControl(cumulataGroup);
        // isMaximized = true;
        // } else if (caller == leaksPerDayBrowser) {
        // mainSash.setMaximizedControl(lowerChartsSash);
        // lowerChartsSash.setMaximizedControl(leaksPerDayGroup);
        // isMaximized = true;
        // } else if (caller == leaksPerMachineBrowser) {
        // mainSash.setMaximizedControl(lowerChartsSash);
        // lowerChartsSash.setMaximizedControl(leaksPerMachineGroup);
        // isMaximized = true;
        // }
        // }
    }

    // private void createDevicesCombo( final Composite toolBarComposite ) throws Exception {
    //
    // Composite composite = new Composite(toolBarComposite, SWT.NONE);
    // composite.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
    // GridLayout layout = new GridLayout(2, false);
    // composite.setLayout(layout);
    // layout.horizontalSpacing = 15;
    // layout.marginLeft = 15;
    //
    // Label plantLabel = new Label(composite, SWT.NONE);
    // plantLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    // plantLabel.setText("Select Plant");
    //
    // plantsCmbo = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
    // GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
    // layoutData.heightHint = 45;
    // layoutData.widthHint = 300;
    // plantsCmbo.setLayoutData(layoutData);
    // List<String> plantNames = SensitDatabaseUtilities.getImpiantiCodesList();
    //
    // Collections.sort(plantNames);
    // plantNames.add(0, "");
    //
    // plantsCmbo.setItems(plantNames.toArray(new String[0]));
    // plantsCmbo.setToolTipText("Select Plant to visualize statistics.");
    //
    // plantsCmbo.addSelectionListener(new SelectionAdapter(){
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public void widgetSelected( SelectionEvent e ) {
    // final String plantCode = getSelectedPlantCode();
    // try {
    // List<SensitLeaks> leaksList = SensitDatabaseUtilities.getLeaksList(dbp, plantCode, null);
    // TimeSeriesChartData leakCumulChartData = leakCumulChartData(leaksList);
    // cumulatesLeaksBrowser.setChartData(leakCumulChartData);
    // cumulatesLeaksBrowser.refresh();
    // BarChartData perDayCd = leakPerDayChartData(leaksList);
    // leaksPerDayBrowser.setBarChartData(perDayCd);
    // leaksPerDayBrowser.refresh();
    // BarChartData perMachinesCD = leakPerDeviceChartData(leaksList);
    // leaksPerMachineBrowser.setBarChartData(perMachinesCD);
    // leaksPerMachineBrowser.refresh();
    // } catch (Exception e1) {
    // StageLogger.logError(this, e1);
    // }
    // }
    //
    // });
    // }

}
