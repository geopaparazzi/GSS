package com.hydrologis.gss.users;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.stage.libs.registry.Authorization;
import eu.hydrologis.stage.libs.registry.Group;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

public class NewGroupDialog extends Dialog {

    private static final long serialVersionUID = 1L;
    private Label groupNameLabel;
    private Text groupNameText;

    private final String title;
    private Combo authorizationCombo;

    public NewGroupDialog( Shell parent ) {
        super(parent);
        this.title = "Add a new Group";
    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        groupNameLabel = new Label(composite, SWT.NONE);
        GridData groupNameData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        groupNameLabel.setLayoutData(groupNameData);
        groupNameLabel.setText("New group name");

        groupNameText = new Text(composite, SWT.BORDER);
        groupNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        groupNameText.setFocus();

        Label groupAuthorizationLabel = new Label(composite, SWT.NONE);
        groupAuthorizationLabel.setText("New group authorization");

        authorizationCombo = new Combo(composite, parent.getStyle());

        try {
            List<Authorization> authorizations = RegistryHandler.INSTANCE.getAuthorizations();
            String[] authNames = new String[authorizations.size()];
            for( int i = 0; i < authNames.length; i++ ) {
                authNames[i] = authorizations.get(i).getName();
            }
            authorizationCombo.setItems(authNames);
            authorizationCombo.select(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
        createButton(parent, IDialogConstants.OK_ID, "Ok", true);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        if (buttonId == IDialogConstants.OK_ID) {
            // add group
            try {
                String newGroupText = groupNameText.getText();
                int selectionIndex = authorizationCombo.getSelectionIndex();
                String authName = authorizationCombo.getItem(selectionIndex);

                Group group = RegistryHandler.INSTANCE.getGroupByName(newGroupText);
                Authorization auth = RegistryHandler.INSTANCE.getAuthorizationByName(authName);
                if (group == null) {
                    Group newGroup = new Group(newGroupText, auth);
                    RegistryHandler.INSTANCE.addGroup(newGroup);

                    setReturnCode(OK);
                    close();
                } else {
                    MessageDialogUtil.openWarning(getParentShell(), null, "A group with that name already exists.", null);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        super.buttonPressed(buttonId);
    }
}
