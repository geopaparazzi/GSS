package com.hydrologis.gss.server.views;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.registry.Authorization;
import com.hydrologis.kukuratus.libs.registry.Group;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.registry.User;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;

public class WebUsersView extends HorizontalLayout implements View {
    private static final long serialVersionUID = 1L;
    private Grid<Group> groupsGrid;
    private Grid<User> usersGrid;

    @Override
    public void enter( ViewChangeEvent event ) {
        addGroupsGrid();
        addUsersGrid();

        setSizeFull();
        refreshGroups();

    }

    private void addGroupsGrid() {
        Button addButton = new Button(VaadinIcons.PLUS);
        addButton.setDescription("Add a new Group");
        addButton.addClickListener(e -> {
            GroupFormWindow window = new GroupFormWindow("Add new Group", new Group());
            getUI().addWindow(window);
        });

        groupsGrid = new Grid<>(Group.class);
        groupsGrid.setSelectionMode(SelectionMode.SINGLE);

        // make editable
        groupsGrid.getEditor().setEnabled(true);
        Binder<Group> binder = groupsGrid.getEditor().getBinder();
        groupsGrid.getEditor().addSaveListener(e -> save(e.getBean()));

        ComboBox<Authorization> authCombo = new ComboBox<>();
        try {
            authCombo.setItems(RegistryHandler.INSTANCE.getAuthorizations());
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        groupsGrid.setColumns();
        groupsGrid.addColumn(Group::getId).setCaption("Group Id").setExpandRatio(1);
        groupsGrid.addColumn(Group::getDescription).setCaption("Description").setExpandRatio(5)
                .setEditorBinding(binder.forField(new TextField()).bind(Group::getDescription, Group::setDescription));
        groupsGrid.addColumn(Group::getAuthorization).setCaption("Authorization").setExpandRatio(4)
                .setEditorBinding(binder.forField(authCombo).bind(Group::getAuthorization, Group::setAuthorization));
        groupsGrid.addComponentColumn(group -> new Button(VaadinIcons.TRASH, e -> deleteGroupClicked(group))).setExpandRatio(1);

        groupsGrid.setColumnReorderingAllowed(true);

        groupsGrid.setWidth("100%");
        groupsGrid.setHeight("100%");

        groupsGrid.asSingleSelect().addValueChangeListener(e -> {
            refreshUsers();
        });

        VerticalLayout gridLayout = new VerticalLayout(addButton, groupsGrid);
        gridLayout.setSizeFull();
        gridLayout.setExpandRatio(groupsGrid, 1);
        addComponent(gridLayout);
        setExpandRatio(gridLayout, 1);
    }

    private void addUsersGrid() {
        Button addButton = new Button(VaadinIcons.PLUS);
        addButton.setDescription("Add a new User");
        addButton.addClickListener(e -> {
            UserFormWindow window = new UserFormWindow("Add new User", new User());
            getUI().addWindow(window);
        });

        usersGrid = new Grid<>(User.class);
        usersGrid.setSelectionMode(SelectionMode.SINGLE);

        // make editable
        usersGrid.getEditor().setEnabled(true);
        Binder<User> binder = usersGrid.getEditor().getBinder();
        usersGrid.getEditor().addSaveListener(e -> save(e.getBean()));

        ComboBox<Group> groupCombo = new ComboBox<>();
        try {
            groupCombo.setItems(RegistryHandler.INSTANCE.getGroupsWithAuthorizations());
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        usersGrid.setColumns();
        usersGrid.addColumn(User::getId).setCaption("User Id").setExpandRatio(1);
        usersGrid.addColumn(User::getUniqueName).setCaption("Unique Name").setExpandRatio(5)
                .setEditorBinding(binder.forField(new TextField()).bind(User::getUniqueName, User::setUniqueName));
        usersGrid.addColumn(User::getPwd).setCaption("Password").setExpandRatio(5)
                .setRenderer(user -> "**********", new TextRenderer())
                .setEditorBinding(binder.forField(new TextField()).bind(User::getPwd, User::setPwd));
        usersGrid.addColumn(User::getGroup).setCaption("Group").setExpandRatio(4)
                .setEditorBinding(binder.forField(groupCombo).bind(User::getGroup, User::setGroup));
        usersGrid.addColumn(User::getName).setCaption("Name").setExpandRatio(5)
                .setEditorBinding(binder.forField(new TextField()).bind(User::getName, User::setName));
        usersGrid.addColumn(User::getEmail).setCaption("Email").setExpandRatio(5)
                .setEditorBinding(binder.forField(new TextField()).bind(User::getEmail, User::setEmail));

        usersGrid.addComponentColumn(group -> new Button(VaadinIcons.TRASH, e -> deleteUserClicked(group))).setExpandRatio(1);

        usersGrid.setColumnReorderingAllowed(true);

        usersGrid.setWidth("100%");
        usersGrid.setHeight("100%");

        VerticalLayout gridLayout = new VerticalLayout(addButton, usersGrid);
        gridLayout.setExpandRatio(usersGrid, 1);
        gridLayout.setSizeFull();
        addComponent(gridLayout);
        setExpandRatio(gridLayout, 2);
    }

    private void deleteGroupClicked( Group group ) {
        try {
            List<User> usersOfGroup = RegistryHandler.INSTANCE.getUsersOfGroup(group);
            if (usersOfGroup.size() > 0) {
                Notification.show("Can't delete a group that contains users.", Type.WARNING_MESSAGE);
            } else {
                RegistryHandler.INSTANCE.deleteGroup(group);
                refreshGroups();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void deleteUserClicked( User user ) {
        try {
            String authenticatedUsername = AuthService.INSTANCE.getAuthenticatedUsername();
            if (authenticatedUsername.equals(user.getUniqueName())) {
                Notification.show("Can't delete the currently logged users.", Type.WARNING_MESSAGE);
            } else {
                RegistryHandler.INSTANCE.deleteUser(user);
            }

            refreshGroups();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save( Group group ) {
        try {
            RegistryHandler.INSTANCE.updateGroup(group);
            refreshGroups();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save( User user ) {
        try {
            RegistryHandler.INSTANCE.updateUser(user);
            refreshUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshGroups() {
        try {
            List<Group> groups = RegistryHandler.INSTANCE.getGroupsWithAuthorizations();
            groupsGrid.setItems(groups);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    private void refreshUsers() {
        try {
            Set<Group> groupSet = groupsGrid.getSelectedItems();
            if (groupSet.isEmpty()) {
                usersGrid.setItems(Collections.emptyList());
                return;
            }
            Group group = groupSet.iterator().next();
            List<User> users = RegistryHandler.INSTANCE.getUsersOfGroup(group);
            users.forEach(user -> user.setGroup(group));
            usersGrid.setItems(users);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class GroupFormWindow extends Window {

        private TextField description = new TextField("Description");
        private ComboBox<Authorization> authorization = null;

        private Button cancel = new Button("Cancel");
        private Button save = new Button("Save", VaadinIcons.CHECK);

        public GroupFormWindow( String caption, Group group ) {
            initLayout(caption);
            initBehavior(group);
        }

        private void initLayout( String caption ) {
            List<Authorization> authList = Collections.emptyList();
            try {
                authList = RegistryHandler.INSTANCE.getAuthorizations();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            authorization = new ComboBox<>("Authorization", authList);

            setCaption(caption);
            save.addStyleName(ValoTheme.BUTTON_PRIMARY);

            HorizontalLayout buttons = new HorizontalLayout(cancel, save);
            buttons.setSpacing(true);

            GridLayout formLayout = new GridLayout(2, 1, description, authorization);
            formLayout.setMargin(true);
            formLayout.setSpacing(true);

            VerticalLayout layout = new VerticalLayout(formLayout, buttons);
            layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);
            setContent(layout);
            setModal(true);
            center();
        }

        private void initBehavior( Group group ) {
            Binder<Group> binder = new Binder<>(Group.class);
            binder.bindInstanceFields(this);
            binder.readBean(group);

            cancel.addClickListener(e -> close());
            save.addClickListener(e -> {
                try {
                    binder.writeBean(group);

                    if (group.getAuthorization() == null) {
                        Notification.show("The authorization is mandatory.", Type.ERROR_MESSAGE);
                        return;
                    }
                    if (group.getDescription() != null && group.getDescription().trim().length() == 0) {
                        Notification.show("The group name is mandatory.", Type.ERROR_MESSAGE);
                        return;
                    }
                    boolean anyMatch = RegistryHandler.INSTANCE.getGroupsWithAuthorizations().stream()
                            .anyMatch(g -> g.getDescription().equals(group.getDescription()));
                    if (anyMatch) {
                        Notification.show("A group with the same name exists already.", Type.ERROR_MESSAGE);
                    } else {
                        RegistryHandler.INSTANCE.addGroup(group);
                        close();
                        refreshGroups();
                        Notification.show("Group saved");
                    }
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
                }
            });
        }
    }

    private class UserFormWindow extends Window {

        private TextField name = new TextField("Name");
        private TextField uniqueName = new TextField("Unique Name");
        private TextField email = new TextField("Email");
        private PasswordField pwd = new PasswordField("Password");

        private ComboBox<Group> group = null;

        private Button cancel = new Button("Cancel");
        private Button save = new Button("Save", VaadinIcons.CHECK);

        public UserFormWindow( String caption, User user ) {
            initLayout(caption);
            initBehavior(user);
        }

        private void initLayout( String caption ) {
            List<Group> groupsList = Collections.emptyList();
            try {
                groupsList = RegistryHandler.INSTANCE.getGroupsWithAuthorizations();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            group = new ComboBox<>("Group", groupsList);

            setCaption(caption);
            save.addStyleName(ValoTheme.BUTTON_PRIMARY);

            HorizontalLayout buttons = new HorizontalLayout(cancel, save);
            buttons.setSpacing(true);

            GridLayout formLayout = new GridLayout(3, 2, name, uniqueName, email, pwd, new Label(), group);
            formLayout.setMargin(true);
            formLayout.setSpacing(true);

            VerticalLayout layout = new VerticalLayout(formLayout, buttons);
            layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);
            setContent(layout);
            setModal(true);
            center();
        }

        private void initBehavior( User user ) {
            Binder<User> binder = new Binder<>(User.class);
            binder.bindInstanceFields(this);
            binder.readBean(user);

            cancel.addClickListener(e -> close());
            save.addClickListener(e -> {
                try {
                    binder.writeBean(user);

                    if (user.getName() != null && user.getName().trim().length() == 0) {
                        Notification.show("The user name is mandatory.", Type.ERROR_MESSAGE);
                        return;
                    }
                    if (user.getUniqueName() != null && user.getUniqueName().trim().length() == 0) {
                        Notification.show("The unique user name is mandatory.", Type.ERROR_MESSAGE);
                        return;
                    }
                    if (user.getPwd() != null && user.getPwd().trim().length() == 0) {
                        Notification.show("The password is mandatory.", Type.ERROR_MESSAGE);
                        return;
                    }
                    if (user.getGroup() == null ) {
                        Notification.show("The group is mandatory.", Type.ERROR_MESSAGE);
                        return;
                    }
                    User sameNameUser = RegistryHandler.INSTANCE.getUserByUniqueName(user.getUniqueName());
                    if (sameNameUser != null) {
                        Notification.show("A user with the same unique name exists already.", Type.ERROR_MESSAGE);
                    } else {
                        RegistryHandler.INSTANCE.addUser(user);
                        close();
                        refreshUsers();
                        Notification.show("User saved");
                    }
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
                }
            });
        }
    }
}
