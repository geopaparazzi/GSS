package com.hydrologis.gss.users;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.stage.libs.registry.Group;
import eu.hydrologis.stage.libs.registry.User;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

public class NewUserDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private Text userNameText;

    private final String title;
    private Group group;

    private Text uniqueUserText;

    private Text emailText;

    private Text pwdText;

    public NewUserDialog( Shell parent, Group group ) {
        super(parent);
        this.group = group;
        this.title = "Add a new User";
    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
        shell.setMinimumSize(500, 200);
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        Label userNameLabel = new Label(composite, SWT.NONE);
        userNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        userNameLabel.setText("Full name of the new user");
        userNameText = new Text(composite, SWT.BORDER);
        userNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        userNameText.setFocus();

        Label uniqueUserLabel = new Label(composite, SWT.NONE);
        uniqueUserLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        uniqueUserLabel.setText("Unique username");
        uniqueUserText = new Text(composite, SWT.BORDER);
        uniqueUserText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label emailLabel = new Label(composite, SWT.NONE);
        emailLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        emailLabel.setText("Email");
        emailText = new Text(composite, SWT.BORDER);
        emailText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label pwdLabel = new Label(composite, SWT.NONE);
        pwdLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pwdLabel.setText("Password");
        pwdText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        pwdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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
                String nameStr = userNameText.getText();
                String uniqueStr = uniqueUserText.getText();
                String emailStr = emailText.getText();
                String pwdStr = pwdText.getText();

                if (nameStr.trim().length() > 0 && uniqueStr.trim().length() > 0 && pwdStr.trim().length() > 0) {
                    // check uniquename
                    User user = RegistryHandler.INSTANCE.getUserByUniqueName(uniqueStr);
                    if (user == null) {
                        User newUser = new User(nameStr, uniqueStr, emailStr, pwdStr, group);

                        RegistryHandler.INSTANCE.addUser(newUser);

                        setReturnCode(OK);
                        close();
                    } else {
                        MessageDialogUtil.openWarning(getParentShell(), null, "A user with that name already exists.", null);
                        return;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.buttonPressed(buttonId);
    }
}
