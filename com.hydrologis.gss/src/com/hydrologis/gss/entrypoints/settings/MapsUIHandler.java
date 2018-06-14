package com.hydrologis.gss.entrypoints.settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.AbstractColumnLayout;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import com.hydrologis.gss.map.GssMapBrowser;
import com.hydrologis.gss.utils.GssMapsHandler;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.map.EOnlineTileSources;

@SuppressWarnings({"serial", "unchecked"})
public class MapsUIHandler {

    private TableViewer allMapsTableViewer;
    private TableViewer selectedMapsTableViewer;
    private GssMapBrowser mapBrowser;

    public void buildGui( Composite buttonsComposite, Composite parametersComposite ) {
        // MAPS
        Composite mapsComposite = new Composite(parametersComposite, SWT.NONE);
        mapsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mapsComposite.setLayout(new GridLayout(3, true));

        Composite leftComposite = new Composite(mapsComposite, SWT.NONE);
        leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        leftComposite.setLayout(new TableColumnLayout());

        Composite middleComposite = new Composite(mapsComposite, SWT.NONE);
        middleComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout middleLayout = new GridLayout(1, false);
        middleLayout.verticalSpacing = 10;
        middleComposite.setLayout(middleLayout);

        Composite rightComposite = new Composite(mapsComposite, SWT.NONE);
        rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        rightComposite.setLayout(new TableColumnLayout());

        allMapsTableViewer = new TableViewer(leftComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        allMapsTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn columnAll = createMapColumn(allMapsTableViewer, "Available Maps List", 0);

        AbstractColumnLayout layoutAll = (AbstractColumnLayout) leftComposite.getLayout();
        layoutAll.setColumnData(columnAll.getColumn(), new ColumnWeightData(100));

        allMapsTableViewer.getTable().setHeaderVisible(true);
        allMapsTableViewer.getTable().setLinesVisible(true);
        GridData tableDataAll = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableDataAll.verticalSpan = 3;
        allMapsTableViewer.getTable().setLayoutData(tableDataAll);

        List<String> allMapNames = GssMapsHandler.INSTANCE.getAllNames();
        allMapsTableViewer.setInput(allMapNames);
        allMapsTableViewer.addSelectionChangedListener(e -> {
            final IStructuredSelection mapSelection = (IStructuredSelection) allMapsTableViewer.getSelection();
            Object firstElement = mapSelection.getFirstElement();
            if (firstElement instanceof String) {
                String mapName = (String) firstElement;
                refreshMap(mapName);
            }
        });

        Button addButton = new Button(middleComposite, SWT.PUSH);

        selectedMapsTableViewer = new TableViewer(rightComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        selectedMapsTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn columnSelected = createMapColumn(selectedMapsTableViewer, "Selected Maps List", 0);
        AbstractColumnLayout layoutSelected = (AbstractColumnLayout) rightComposite.getLayout();
        layoutSelected.setColumnData(columnSelected.getColumn(), new ColumnWeightData(100));

        selectedMapsTableViewer.getTable().setHeaderVisible(true);
        selectedMapsTableViewer.getTable().setLinesVisible(true);
        GridData tableDataSelected = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableDataSelected.verticalSpan = 3;
        selectedMapsTableViewer.getTable().setLayoutData(tableDataSelected);

        List<String> selectedMapNames = new ArrayList<>();
        try {
            selectedMapNames = GssMapsHandler.INSTANCE.getSelectedNames();
        } catch (SQLException e2) {
            StageLogger.logError(this, e2);
        }
        selectedMapsTableViewer.setInput(selectedMapNames);

        // MIDDLE PART

        mapBrowser = new GssMapBrowser(middleComposite, SWT.BORDER);

        Button removeButton = new Button(middleComposite, SWT.PUSH);

        GridData mapBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        mapBrowserGD.heightHint = 400;
        mapBrowser.setLayoutData(mapBrowserGD);
        refreshMap(EOnlineTileSources.Open_Street_Map_Standard.getName());

        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addButton.setText("add selected to 'Selected Maps List'");
        addButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                final IStructuredSelection mapSelection = (IStructuredSelection) allMapsTableViewer.getSelection();
                Object firstElement = mapSelection.getFirstElement();
                if (firstElement instanceof String) {
                    String mapName = (String) firstElement;
                    List<String> input = (List<String>) selectedMapsTableViewer.getInput();
                    List<String> newList = new ArrayList<>();
                    newList.addAll(input);
                    newList.add(mapName);
                    selectedMapsTableViewer.setInput(newList);

                    try {
                        GssMapsHandler.INSTANCE.putSelectedNames(newList);
                    } catch (SQLException e1) {
                        StageLogger.logError(MapsUIHandler.this, e1);
                    }
                }
            }
        });

        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        removeButton.setText("remove selected from 'Selected Maps List'");
        removeButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                final IStructuredSelection mapSelection = (IStructuredSelection) selectedMapsTableViewer.getSelection();
                Object firstElement = mapSelection.getFirstElement();
                if (firstElement instanceof String) {
                    String mapName = (String) firstElement;
                    List<String> input = (List<String>) selectedMapsTableViewer.getInput();
                    List<String> newList = new ArrayList<>();
                    newList.addAll(input);
                    newList.removeIf(name -> name.equals(mapName));
                    selectedMapsTableViewer.setInput(newList);

                    try {
                        GssMapsHandler.INSTANCE.putSelectedNames(newList);
                    } catch (SQLException e1) {
                        StageLogger.logError(MapsUIHandler.this, e1);
                    }
                }
            }
        });

        Button mapsButton = new Button(buttonsComposite, SWT.PUSH);
        GridData usersButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        usersButtonGD.heightHint = 100;
        mapsButton.setLayoutData(usersButtonGD);
        mapsButton.setBackground(buttonsComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        mapsButton.setText("<b><i>Background Maps</i></b>");
        mapsButton.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        mapsButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                StackLayout parametersStackLayout = (StackLayout) parametersComposite.getLayout();
                parametersStackLayout.topControl = mapsComposite;
                parametersComposite.layout();
            }
        });
    }

    private void refreshMap( String selectedMap ) {
        String mapHtml = mapBrowser.getMapHtml();
        String replacement = GssMapsHandler.INSTANCE.getForMapBrowser(mapBrowser, "", selectedMap, true);
        mapHtml = mapBrowser.addLayersInHtml(mapHtml, replacement);
        mapBrowser.setText(mapHtml);
    }

    private TableViewerColumn createMapColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new MapLabelProvider());
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        return result;
    }

    private class MapLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        @Override
        public String getText( Object element ) {
            return element.toString();
        }
    }
}
