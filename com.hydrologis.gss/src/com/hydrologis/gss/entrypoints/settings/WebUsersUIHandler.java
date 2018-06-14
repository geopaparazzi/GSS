package com.hydrologis.gss.entrypoints.settings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.AbstractColumnLayout;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.widgets.DialogCallback;
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

import com.hydrologis.gss.users.NewGroupDialog;
import com.hydrologis.gss.users.NewUserDialog;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.registry.User;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

@SuppressWarnings("serial")
public class WebUsersUIHandler {
    private TableViewer groupsTableViewer;
    private TableViewer usersTableViewer;
    private Composite groupsComposite;
    private Composite usersComposite;
    private Shell parentShell;
    private User loggedUser;

    public void buildGui( Composite buttonsComposite, Composite parametersComposite, User loggedUser ) {
        this.loggedUser = loggedUser;
        parentShell = buttonsComposite.getShell();

        // GROUPS AND USERS
        Composite groupsAndUsersComposite = new Composite(parametersComposite, SWT.NONE);
        groupsAndUsersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout groupsAndUsersLayout = new GridLayout(1, false);
        groupsAndUsersLayout.verticalSpacing = 10;
        groupsAndUsersComposite.setLayout(groupsAndUsersLayout);

        try {
            Label groupsLabel = new Label(groupsAndUsersComposite, SWT.NONE);
            groupsLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            groupsLabel.setText("<b><i>Available groups (select group to view users)</i></b>");
            groupsLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

            groupsComposite = new Composite(groupsAndUsersComposite, SWT.NONE);
            TableColumnLayout groupsColumnLayout = new TableColumnLayout();
            groupsComposite.setLayout(groupsColumnLayout);
            groupsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            createGroupsTableViewer();

            Label usersLabel = new Label(groupsAndUsersComposite, SWT.NONE);
            usersLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            usersLabel.setText("<b><i>Available users</i></b>");
            usersLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

            usersComposite = new Composite(groupsAndUsersComposite, SWT.NONE);
            TableColumnLayout usersColumnLayout = new TableColumnLayout();
            usersComposite.setLayout(usersColumnLayout);
            GridData usersGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            usersComposite.setLayoutData(usersGD);
            createUsersTableViewer();
        } catch (Exception e) {
            StageLogger.logError(this, e);
        }

        Button groupsButton = new Button(buttonsComposite, SWT.PUSH);
        GridData groupsButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        groupsButtonGD.heightHint = 100;
        groupsButton.setLayoutData(groupsButtonGD);
        groupsButton.setBackground(buttonsComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        groupsButton.setText("<b><i>Web Application Groups and Users</i></b>");
        groupsButton.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        groupsButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                StackLayout parametersStackLayout = (StackLayout) parametersComposite.getLayout();
                parametersStackLayout.topControl = groupsAndUsersComposite;
                parametersComposite.layout();
            }
        });
    }

    private void createGroupsTableViewer() throws Exception {
        if (groupsTableViewer != null)
            groupsTableViewer.getControl().dispose();

        groupsTableViewer = new TableViewer(groupsComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        groupsTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn column1 = createGroupColumn(groupsTableViewer, "Name", 0);
        TableViewerColumn column2 = createGroupColumn(groupsTableViewer, "Permission", 1);

        AbstractColumnLayout layout = (AbstractColumnLayout) groupsComposite.getLayout();
        layout.setColumnData(column1.getColumn(), new ColumnWeightData(50));
        layout.setColumnData(column2.getColumn(), new ColumnWeightData(50));

        groupsTableViewer.getTable().setHeaderVisible(true);
        groupsTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupsTableViewer.getTable().setLayoutData(tableData);

        List<eu.hydrologis.stage.libs.registry.Group> groups = RegistryHandler.INSTANCE.getGroupsWithAuthorizations();
        groupsTableViewer.setInput(groups);

        groupsTableViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                try {
                    createUsersTableViewer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // add right click menu
        MenuManager manager = new MenuManager();
        groupsTableViewer.getControl().setMenu(manager.createContextMenu(groupsTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                if (groupsTableViewer.getSelection() instanceof IStructuredSelection) {
                    final IStructuredSelection selection = (IStructuredSelection) groupsTableViewer.getSelection();

                    Action addNewGroupAction = new Action("Add new group", null){
                        @Override
                        public void run() {
                            NewGroupDialog newGroupDialog = new NewGroupDialog(parentShell);
                            int returnCode = newGroupDialog.open();
                            if (returnCode == Window.OK) {
                                try {
                                    createGroupsTableViewer();
                                } catch (Exception e) {
                                    StageLogger.logError(WebUsersUIHandler.this, e);
                                }
                            }
                        }
                    };
                    if (selection.isEmpty()) {
                        manager.add(addNewGroupAction);
                        return;
                    }
                    Object firstElement = selection.getFirstElement();
                    if (firstElement instanceof eu.hydrologis.stage.libs.registry.Group) {
                        eu.hydrologis.stage.libs.registry.Group selectedGroup = (eu.hydrologis.stage.libs.registry.Group) firstElement;

                        manager.add(addNewGroupAction);
                        if (loggedUser.getGroup().getId() != selectedGroup.getId()) {
                            manager.add(new Action("Delete Group", null){
                                @Override
                                public void run() {
                                    MessageDialogUtil.openQuestion(parentShell, "Delete Group",
                                            "Are you sure you want to delete the group and all users belonging to it?",
                                            new DialogCallback(){
                                                @Override
                                                public void dialogClosed( int returnCode ) {
                                                    if (returnCode == Window.OK) {
                                                        try {
                                                            RegistryHandler.INSTANCE.removeGroup(selectedGroup);
                                                            createGroupsTableViewer();
                                                        } catch (Exception e) {
                                                            StageLogger.logError(WebUsersUIHandler.this, e);
                                                        }
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        groupsComposite.layout();
    }

    private void createUsersTableViewer() throws Exception {
        eu.hydrologis.stage.libs.registry.Group group = getSelectedGroup();
        if (usersTableViewer != null)
            usersTableViewer.getControl().dispose();

        List<User> users = new ArrayList<>();
        if (group != null)
            users = RegistryHandler.INSTANCE.getUsersOfGroup(group);

        usersTableViewer = new TableViewer(usersComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        usersTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn column1 = createUserColumn(usersTableViewer, "Name", 0);
        TableViewerColumn column2 = createUserColumn(usersTableViewer, "Username", 1);
        TableViewerColumn column3 = createUserColumn(usersTableViewer, "Email", 2);

        AbstractColumnLayout layout = (AbstractColumnLayout) usersComposite.getLayout();
        layout.setColumnData(column1.getColumn(), new ColumnWeightData(33));
        layout.setColumnData(column2.getColumn(), new ColumnWeightData(33));
        layout.setColumnData(column3.getColumn(), new ColumnWeightData(33));

        usersTableViewer.getTable().setHeaderVisible(true);
        usersTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        usersTableViewer.getTable().setLayoutData(tableData);

        usersTableViewer.setInput(users);

        // add right click menu
        MenuManager manager = new MenuManager();
        usersTableViewer.getControl().setMenu(manager.createContextMenu(usersTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                if (usersTableViewer.getSelection() instanceof IStructuredSelection) {
                    final IStructuredSelection selection = (IStructuredSelection) usersTableViewer.getSelection();

                    Action addNewUserAction = new Action("Add new user", null){
                        @Override
                        public void run() {
                            eu.hydrologis.stage.libs.registry.Group selectedGroup = getSelectedGroup();
                            if (selectedGroup == null) {
                                MessageDialogUtil.openInformation(parentShell, "INFO",
                                        "Please select the group to add the user to.", null);
                                return;
                            }

                            NewUserDialog newUserDialog = new NewUserDialog(parentShell, selectedGroup);
                            int returnCode = newUserDialog.open();
                            if (returnCode == Window.OK) {
                                try {
                                    createUsersTableViewer();
                                } catch (Exception e) {
                                    StageLogger.logError(WebUsersUIHandler.this, e);
                                }
                            }

                        }
                    };
                    if (selection.isEmpty()) {
                        manager.add(addNewUserAction);
                        return;
                    }
                    Object firstElement = selection.getFirstElement();
                    if (firstElement instanceof User) {
                        User selectedUser = (User) firstElement;
                        manager.add(addNewUserAction);

                        if (loggedUser.getId() != selectedUser.getId()) {
                            manager.add(new Action("Delete User", null){
                                @Override
                                public void run() {
                                    MessageDialogUtil.openQuestion(parentShell, "Delete User",
                                            "Are you sure you want to delete the user?", new DialogCallback(){
                                                @Override
                                                public void dialogClosed( int returnCode ) {
                                                    if (returnCode == Window.OK) {
                                                        try {
                                                            RegistryHandler.INSTANCE.removeUser(selectedUser);
                                                            createUsersTableViewer();
                                                        } catch (Exception e) {
                                                            StageLogger.logError(WebUsersUIHandler.this, e);
                                                        }
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        usersComposite.layout();
    }

    private eu.hydrologis.stage.libs.registry.Group getSelectedGroup() {
        final IStructuredSelection groupSelection = (IStructuredSelection) groupsTableViewer.getSelection();
        Object firstElement = groupSelection.getFirstElement();
        eu.hydrologis.stage.libs.registry.Group group = null;
        if (firstElement instanceof eu.hydrologis.stage.libs.registry.Group) {
            group = (eu.hydrologis.stage.libs.registry.Group) firstElement;
        }
        return group;
    }

    private TableViewerColumn createGroupColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new GroupLabelProvider(dataIndex));
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        return result;
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

    private class GroupLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        private int dataIndex;
        public GroupLabelProvider( int dataIndex ) {
            this.dataIndex = dataIndex;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof eu.hydrologis.stage.libs.registry.Group) {
                eu.hydrologis.stage.libs.registry.Group data = (eu.hydrologis.stage.libs.registry.Group) element;
                if (dataIndex == 0) {
                    return data.getDescription();
                } else {
                    return data.getAuthorization().getName();
                }
            }
            return "";
        }
    }

    private class UserLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        private int dataIndex;
        public UserLabelProvider( int dataIndex ) {
            this.dataIndex = dataIndex;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof User) {
                User user = (User) element;
                if (dataIndex == 0) {
                    return user.getName();
                } else if (dataIndex == 1) {
                    return user.getUniqueName();
                } else {
                    return user.getEmail();
                }
            }
            return "";
        }
    }

}
