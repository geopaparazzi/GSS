/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.entrypoints.settings;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.GssDbProvider;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.j256.ormlite.dao.Dao;

import eu.hydrologis.stage.libs.log.StageLogger;

@SuppressWarnings("serial")
public class DevicesUIHandler {
    private TableViewer usersTableViewer;
    private Composite usersComposite;
    private Shell parentShell;
    private DatabaseHandler databaseHandler;

    public void buildGui( Composite buttonsComposite, Composite parametersComposite ) {
        parentShell = buttonsComposite.getShell();

        GssDbProvider dbProvider = GssContext.instance().getDbProvider();
        databaseHandler = dbProvider.getDatabaseHandler();

        Composite mainUsersComposite = new Composite(parametersComposite, SWT.NONE);
        mainUsersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout mainUsersLayout = new GridLayout(1, false);
        mainUsersLayout.verticalSpacing = 10;
        mainUsersComposite.setLayout(mainUsersLayout);

        try {
            Label usersLabel = new Label(mainUsersComposite, SWT.NONE);
            usersLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            usersLabel.setText("<b><i>Surveyors</i></b>");
            usersLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

            usersComposite = new Composite(mainUsersComposite, SWT.NONE);
            TableColumnLayout usersColumnLayout = new TableColumnLayout();
            usersComposite.setLayout(usersColumnLayout);
            GridData usersGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            usersComposite.setLayoutData(usersGD);
            createUsersTableViewer();
        } catch (Exception e) {
            StageLogger.logError(this, e);
        }

        Button usersButton = new Button(buttonsComposite, SWT.PUSH);
        GridData groupsButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        groupsButtonGD.heightHint = 100;
        usersButton.setLayoutData(groupsButtonGD);
        usersButton.setBackground(buttonsComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        usersButton.setText("<b><i>Surveyors</i></b>");
        usersButton.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        usersButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                StackLayout parametersStackLayout = (StackLayout) parametersComposite.getLayout();
                parametersStackLayout.topControl = mainUsersComposite;
                parametersComposite.layout();
            }
        });
    }

    private void createUsersTableViewer() throws Exception {
        if (usersTableViewer != null)
            usersTableViewer.getControl().dispose();

        usersTableViewer = new TableViewer(usersComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        usersTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn column1 = createUserColumn(usersTableViewer, "Id", 0);
        TableViewerColumn column2 = createUserColumn(usersTableViewer, "Device id", 1);
        TableViewerColumn column3 = createUserColumn(usersTableViewer, "Name", 2);
        TableViewerColumn column4 = createUserColumn(usersTableViewer, "Contact", 3);

        AbstractColumnLayout layout = (AbstractColumnLayout) usersComposite.getLayout();
        layout.setColumnData(column1.getColumn(), new ColumnWeightData(5));
        layout.setColumnData(column2.getColumn(), new ColumnWeightData(25));
        layout.setColumnData(column3.getColumn(), new ColumnWeightData(30));
        layout.setColumnData(column4.getColumn(), new ColumnWeightData(10));

        usersTableViewer.getTable().setHeaderVisible(true);
        usersTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        usersTableViewer.getTable().setLayoutData(tableData);

        refreshTable();

        // add right click menu
        MenuManager manager = new MenuManager();
        usersTableViewer.getControl().setMenu(manager.createContextMenu(usersTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                if (usersTableViewer.getSelection() instanceof IStructuredSelection) {
                    final IStructuredSelection selection = (IStructuredSelection) usersTableViewer.getSelection();

                    // Action addNewUserAction = new Action("Add new User", null){
                    // @Override
                    // public void run() {
                    // NewMachineUserDialog newUserDialog = new NewMachineUserDialog(parentShell);
                    // int returnCode = newUserDialog.open();
                    // if (returnCode == Window.OK) {
                    // try {
                    // createUsersTableViewer();
                    // } catch (Exception e) {
                    // StageLogger.logError(DevicesUIHandler.this, e);
                    // }
                    // }
                    //
                    // }
                    // };
                    // manager.add(addNewUserAction);
                    if (selection.isEmpty()) {
                        return;
                    }
                    Object firstElement = selection.getFirstElement();
                    if (firstElement instanceof GpapUsers) {
                        GpapUsers selectedUser = (GpapUsers) firstElement;
                        manager.add(new Action("Edit user", null){
                            @Override
                            public void run() {
                                try {
                                    GpapUserEditDialog editor = new GpapUserEditDialog(parentShell, selectedUser);
                                    int open = editor.open();
                                    if (open == Dialog.OK) {
                                        refreshTable();
                                    }
                                } catch (Exception e) {
                                    StageLogger.logError(this, e);
                                }
                            }
                        });
                    }
                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        usersComposite.layout();
    }

    private void refreshTable() throws SQLException {
        Dao<GpapUsers, ? > usersDao = databaseHandler.getDao(GpapUsers.class);
        List<GpapUsers> users = usersDao.queryForAll();
        usersTableViewer.setInput(users);
    }

    private TableViewerColumn createUserColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new UserLabelProvider(dataIndex));
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        return result;
    }

    private class UserLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        private int dataIndex;
        public UserLabelProvider( int dataIndex ) {
            this.dataIndex = dataIndex;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof GpapUsers) {
                GpapUsers user = (GpapUsers) element;
                if (dataIndex == 0) {
                    return user.id + "";
                } else if (dataIndex == 1) {
                    return user.deviceId;
                } else if (dataIndex == 2) {
                    return user.name;
                } else if (dataIndex == 3) {
                    return user.contact;
                }
            }
            return "";
        }
    }
}
