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
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.layout.AbstractColumnLayout;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;
import org.hortonmachine.dbs.log.Message;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;

import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.registry.User;
import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class LogViewEntryPoint extends StageEntryPoint {
    public static final String ID = "com.hydrologis.gss.entrypoints.LogViewEntryPoint";

    private static final long serialVersionUID = 1L;

    private Display display;

    private Shell parentShell;
    private TableViewer logTableViewer;

    private Composite mainComposite;

    private Composite logComposite;

    public static String dateFormatterYYYYMMDDHHMMSS_string = "yyyy-MM-dd HH:mm:ss";
    public static DateTimeFormatter dateFormatterYYYYMMDDHHMMSS = DateTimeFormat.forPattern(dateFormatterYYYYMMDDHHMMSS_string);

    private Combo typeCombo;

    private Text limitText;

    private org.eclipse.swt.widgets.DateTime fromDateChooser;

    private org.eclipse.swt.widgets.DateTime toDateChooser;

    private Color errorColor;
    private Color warningColor;
    private Color accessColor;
    private Color debugColor;

    private Combo heightCombo;

    private HashMap<String, String> heightMap;

    @SuppressWarnings("serial")
    @Override
    protected void createPage( final Composite parent ) {
        // Login screen
        User loggedUser = GssLoginDialog.checkUserLogin(getShell());
        boolean isAdmin = RegistryHandler.INSTANCE.isAdmin(loggedUser);
        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(isAdmin);
        if (loggedUser == null || !permissionsHandler.isAllowed(redirectUrl)) {
            StageUtils.permissionDeniedPage(parent, "/");
            return;
        }

        parentShell = parent.getShell();
        display = parent.getDisplay();

        errorColor = new Color(display, 255, 0, 0);
        warningColor = new Color(display, 255, 175, 0);
        accessColor = new Color(display, 0, 153, 255);
        debugColor = new Color(display, 0, 160, 0);

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(1, true));

        final Composite toolbarComposite = new Composite(composite, SWT.NONE);
        toolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        toolbarComposite.setLayout(new GridLayout(4, false));
        Label iconLabel = new Label(toolbarComposite, parentShell.getStyle());
        iconLabel.setText("");
        iconLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
        Image logoImage = com.hydrologis.gss.utils.ImageCache.getInstance().getImage(display,
                com.hydrologis.gss.utils.ImageCache.LOGO);
        iconLabel.setImage(logoImage);

        Label spacerLabel = new Label(toolbarComposite, parentShell.getStyle());
        spacerLabel.setText("");
        spacerLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        GssGuiUtilities.addLogoutButton(toolbarComposite);

        mainComposite = new Composite(composite, SWT.None);
        GridLayout mapLayout = new GridLayout(6, true);
        mapLayout.marginWidth = 15;
        mapLayout.marginHeight = 15;
        mainComposite.setLayout(mapLayout);
        GridData mainGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        mainComposite.setLayoutData(mainGD);

        addTypeCombo();
        addFromChooser();
        addToChooser();
        addLimitText();
        addHeightCombo();

        Group refreshGroup = new Group(mainComposite, parentShell.getStyle());
        GridData refreshGD = new GridData(GridData.FILL, GridData.FILL, true, false);
        refreshGroup.setLayoutData(refreshGD);
        refreshGroup.setLayout(new FillLayout());
        refreshGroup.setText("REFRESH");

        final Button refreshButton = new Button(refreshGroup, SWT.FLAT);
        refreshButton.setText("reload");
        refreshButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                Logger logDb = StageWorkspace.getInstance().getLogDb();

                int selectionIndex = typeCombo.getSelectionIndex();
                String item = typeCombo.getItem(selectionIndex);
                EMessageType type = EMessageType.valueOf(item);

                long limit = 100;

                String limitString = limitText.getText();
                try {
                    limit = (long) Double.parseDouble(limitString);
                } catch (Exception e2) {
                    // ignore
                }

                // yyyy-MM-dd HH:mm:ss
                String fromDateStr = fromDateChooser.getYear() + "-" + (fromDateChooser.getMonth() + 1) + "-"
                        + fromDateChooser.getDay() + " 00:00:00";
                DateTime fromDate = dateFormatterYYYYMMDDHHMMSS.parseDateTime(fromDateStr);

                String toDateStr = toDateChooser.getYear() + "-" + (toDateChooser.getMonth() + 1) + "-" + toDateChooser.getDay()
                        + " 23:59:59";
                DateTime toDate = dateFormatterYYYYMMDDHHMMSS.parseDateTime(toDateStr);

                selectionIndex = heightCombo.getSelectionIndex();
                String heightItemDesc = heightCombo.getItem(selectionIndex);
                String heightItem = heightMap.get(heightItemDesc);

                Integer itemHeight = null;
                try {
                    itemHeight = new Integer(Integer.parseInt(heightItem));
                } catch (NumberFormatException ex) {
                    // ignore invalid item count
                }

                try {
                    List<Message> messagesList = logDb.getFilteredList(type, fromDate.getMillis(), toDate.getMillis(), limit);
                    createLogTableViewer(messagesList, itemHeight);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

        Group logGroup = new Group(mainComposite, parentShell.getStyle());
        GridData logGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        logGD.horizontalSpan = 6;
        logGroup.setLayoutData(logGD);
        logGroup.setLayout(new FillLayout());
        logGroup.setText("log view");
        try {
            logComposite = new Composite(logGroup, SWT.NONE);
            TableColumnLayout logColumnLayout = new TableColumnLayout();
            logComposite.setLayout(logColumnLayout);
            createLogTableViewer(new ArrayList<>(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }


        String name = "Anonymous User";
        if (loggedUser != null)
            name = loggedUser.getName();
        GssGuiUtilities.addFooter(name, composite);
    }

    @SuppressWarnings("serial")
    private void addHeightCombo() {
        Group heightGroup = new Group(mainComposite, parentShell.getStyle());
        GridData heightGD = new GridData(GridData.FILL, GridData.FILL, true, false);
        heightGroup.setLayoutData(heightGD);
        heightGroup.setLayout(new FillLayout());
        heightGroup.setText("ROW HEIGHT");

        heightCombo = new Combo(heightGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] values = {"30", "80", "300", "600"};
        String[] names = {"normal", "medium", "large", "huge"};

        heightMap = new HashMap<>();
        for( int i = 0; i < names.length; i++ ) {
            heightMap.put(names[i], values[i]);
        }

        heightCombo.setItems(names);
        heightCombo.select(0);
        heightCombo.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                int selectionIndex = heightCombo.getSelectionIndex();
                String heightItemDesc = heightCombo.getItem(selectionIndex);

                String heightItem = heightMap.get(heightItemDesc);
                Integer rowHeight = null;
                try {
                    rowHeight = new Integer(Integer.parseInt(heightItem));
                    if (logTableViewer != null) {
                        logTableViewer.getTable().setData(RWT.CUSTOM_ITEM_HEIGHT, rowHeight);
                        logTableViewer.getTable().redraw();
                    }
                } catch (NumberFormatException ex) {
                    // ignore invalid item count
                }
            }
        });

    }

    private void addLimitText() {
        Group limitGroup = new Group(mainComposite, parentShell.getStyle());
        GridData limitGD = new GridData(GridData.FILL, GridData.FILL, true, false);
        limitGroup.setLayoutData(limitGD);
        limitGroup.setLayout(new FillLayout());
        limitGroup.setText("LIMIT");

        limitText = new Text(limitGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        // limitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        limitText.setText("100");

    }

    private void addToChooser() {
        Group toGroup = new Group(mainComposite, parentShell.getStyle());
        GridData toGD = new GridData(GridData.FILL, GridData.FILL, true, false);
        toGroup.setLayoutData(toGD);
        toGroup.setLayout(new FillLayout());
        toGroup.setText("TO DATE");

        int style = toGroup.getStyle() | SWT.DATE | SWT.MEDIUM | SWT.BORDER;
        toDateChooser = new org.eclipse.swt.widgets.DateTime(toGroup, style);

    }

    private void addFromChooser() {
        Group fromGroup = new Group(mainComposite, parentShell.getStyle());
        GridData fromGD = new GridData(GridData.FILL, GridData.FILL, true, false);
        fromGroup.setLayoutData(fromGD);
        fromGroup.setLayout(new FillLayout());
        fromGroup.setText("FROM DATE");

        int style = fromGroup.getStyle() | SWT.DATE | SWT.MEDIUM | SWT.BORDER;
        fromDateChooser = new org.eclipse.swt.widgets.DateTime(fromGroup, style);

    }

    private void addTypeCombo() {
        Group typeGroup = new Group(mainComposite, parentShell.getStyle());
        GridData typeGD = new GridData(GridData.FILL, GridData.FILL, true, false);
        typeGroup.setLayoutData(typeGD);
        typeGroup.setLayout(new FillLayout());
        typeGroup.setText("TYPE");

        typeCombo = new Combo(typeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        // GridData typeComboGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        // typeCombo.setLayoutData(typeComboGD);
        EMessageType[] values = EMessageType.values();
        String[] names = new String[values.length];
        for( int i = 0; i < names.length; i++ ) {
            names[i] = values[i].name();
        }
        typeCombo.setItems(names);
        typeCombo.select(0);
    }

    private void createLogTableViewer( List<Message> messagesList, Integer rowHeight ) throws Exception {
        if (logTableViewer != null)
            logTableViewer.getControl().dispose();

        logTableViewer = new TableViewer(logComposite, SWT.BORDER | SWT.FULL_SELECTION);
        logTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        logTableViewer.getTable().setData(RWT.CUSTOM_ITEM_HEIGHT, rowHeight);

        TableViewerColumn column1 = createGroupColumn(logTableViewer, "id", 0);
        TableViewerColumn column2 = createGroupColumn(logTableViewer, "timestamp", 1);
        TableViewerColumn column3 = createGroupColumn(logTableViewer, "type", 2);
        TableViewerColumn column4 = createGroupColumn(logTableViewer, "tag", 3);
        TableViewerColumn column5 = createGroupColumn(logTableViewer, "message", 4);

        column5.setEditingSupport(new TextEditingSupport(logTableViewer, 4));

        ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(logTableViewer);
        FocusCellOwnerDrawHighlighter highlighter = new FocusCellOwnerDrawHighlighter(logTableViewer);
        TableViewerFocusCellManager focusManager = new TableViewerFocusCellManager(logTableViewer, highlighter);
        int feature = ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR;
        TableViewerEditor.create(logTableViewer, focusManager, activationStrategy, feature);

        AbstractColumnLayout layout = (AbstractColumnLayout) logComposite.getLayout();
        layout.setColumnData(column1.getColumn(), new ColumnWeightData(5));
        layout.setColumnData(column2.getColumn(), new ColumnWeightData(15));
        layout.setColumnData(column3.getColumn(), new ColumnWeightData(5));
        layout.setColumnData(column4.getColumn(), new ColumnWeightData(20));
        layout.setColumnData(column5.getColumn(), new ColumnWeightData(55));

        logTableViewer.getTable().setHeaderVisible(true);
        logTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        logTableViewer.getTable().setLayoutData(tableData);
        logTableViewer.getTable().setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        logTableViewer.setInput(messagesList);

        logComposite.layout();
    }

    private TableViewerColumn createGroupColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new MessageLabelProvider(dataIndex));
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        return result;
    }

    private class MessageLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        private int dataIndex;

        public MessageLabelProvider( int dataIndex ) {
            this.dataIndex = dataIndex;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof Message) {
                String fromIndex = getFromIndex(element, dataIndex);
                if (fromIndex == null) {
                    fromIndex = "";
                }
                return fromIndex;
            }
            return "";
        }

        @Override
        public Color getForeground( Object element ) {
            Message data = (Message) element;
            EMessageType type = EMessageType.fromCode(data.type);
            switch( type ) {
            case WARNING:
                return warningColor;
            case ERROR:
                return errorColor;
            case ACCESS:
                return accessColor;
            case DEBUG:
                return debugColor;
            default:
                break;
            }
            return super.getForeground(element);
        }
    }

    private static String getFromIndex( Object element, int index ) {
        Message data = (Message) element;
        if (index == 0) {
            return String.valueOf(data.id);
        } else if (index == 1) {
            DateTime ts = new DateTime(data.ts);
            return ts.toString(dateFormatterYYYYMMDDHHMMSS);
        } else if (index == 2) {
            return EMessageType.fromCode(data.type).name();
        } else if (index == 3) {
            return data.tag;
        } else {
            String msg = "";
            try {
                int cutFrom = 0;
                int indexOf = data.msg.indexOf("::");
                if (indexOf != -1) {
                    cutFrom = indexOf;
                    int indexOf2 = data.msg.indexOf("::", indexOf + 2);
                    if (indexOf2 != -1) {
                        cutFrom = indexOf2 + 2;
                    }
                }
                msg = data.msg.substring(cutFrom).trim();
                msg = msg.replaceAll("\n", "<br/>");
                msg = msg.replaceAll("<.*init>", "(init)");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return msg;
        }
    }

    private static final class TextEditingSupport extends EditingSupport {
        private static final long serialVersionUID = 1L;
        private final CellEditor editor;
        private int index;
        public TextEditingSupport( TableViewer viewer, int index ) {
            super(viewer);
            this.index = index;
            editor = new TextCellEditor(viewer.getTable());
        }

        @Override
        protected boolean canEdit( Object element ) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor( Object element ) {
            return editor;
        }

        @Override
        protected Object getValue( Object element ) {
            return getFromIndex(element, index);
        }

        @Override
        protected void setValue( Object element, Object value ) {
            // Person person = (Person) element;
            // person.firstName = (String) value;
            // getViewer().update(element, null);
        }
    }

}
