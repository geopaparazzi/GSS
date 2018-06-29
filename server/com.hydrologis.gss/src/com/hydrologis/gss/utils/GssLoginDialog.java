/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.utils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.hydrologis.gss.GssSession;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.registry.User;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;
import eu.hydrologis.stage.libs.workspace.LoginChecker;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * Login dialog.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GssLoginDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private static final String LOGINMESSAGE = "Please sign in with your username and password:";
    private static final String LOGIN = "Login";
    private static final String CANCEL = "Cancel";
    private static final String PASSWORD = "Password:";
    private static final String USERNAME = "Username:";

    private static final int LOGIN_ID = IDialogConstants.CLIENT_ID + 1;
    private Text userText;
    private Text passText;
    private Label mesgLabel;
    private final String title;
    private final String message;
    private String username;
    private String password;

    private Label errLabel;

    public GssLoginDialog( Shell parent, String title, String message ) {
        super(parent);
        setShellStyle(SWT.NO_TRIM);
        this.title = title;
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername( String username ) {
        this.username = username;
    }

    public String getUsername() {
        return username;
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
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 40;
        layout.marginWidth = 40;
        layout.verticalSpacing = 20;
        layout.horizontalSpacing = 30;
        composite.setLayout(layout);

        Label iconLabel = new Label(composite, SWT.NONE);
        GridData iconLabelGD = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        iconLabelGD.horizontalSpan = 2;
        iconLabel.setLayoutData(iconLabelGD);
        Image logoImage = com.hydrologis.gss.utils.ImageCache.getInstance().getImage(parent.getDisplay(),
                com.hydrologis.gss.utils.ImageCache.LOGO_LOGIN);
        iconLabel.setImage(logoImage);

        mesgLabel = new Label(composite, SWT.NONE);
        GridData messageData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        messageData.horizontalSpan = 2;
        mesgLabel.setLayoutData(messageData);
        Label userLabel = new Label(composite, SWT.NONE);
        userLabel.setText(USERNAME);
        userLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        userText = new Text(composite, SWT.BORDER);
        userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Label passLabel = new Label(composite, SWT.NONE);
        passLabel.setText(PASSWORD);
        passLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        passText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        passText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        errLabel = new Label(composite, SWT.NONE);
        GridData errorData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        errorData.horizontalSpan = 2;
        errLabel.setLayoutData(errorData);
        errLabel.setText("");
        errLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        initilizeDialogArea();
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton(parent, IDialogConstants.CANCEL_ID, CANCEL, false);
        createButton(parent, LOGIN_ID, LOGIN, true);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        if (buttonId == LOGIN_ID) {
            username = userText.getText();
            password = passText.getText();

            User user = null;
            try {
                user = LoginChecker.isLoginOk(username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (user == null) {
                errLabel.setText("<b><i style=\"color:red;\">Wrong username or password!</i></b>");
                return;
            }

            GssSession.setUniqueUserName(user.getUniqueName());

            setReturnCode(OK);
            close();
        } else {
            password = null;
        }
        super.buttonPressed(buttonId);
    }

    private void initilizeDialogArea() {
        if (message != null) {
            mesgLabel.setText(message);
        }
        if (username != null) {
            userText.setText(username);
        }
        userText.setFocus();
    }

    /**
     * Show login screen and check pwd.
     * 
     * @param shell
     * @return the {@link User} or <code>null</code>.
     * @throws Exception 
     */
    public static User checkUserLogin( Shell shell ) {
        if (GssSession.getUniqueUserName().isPresent()) {
            String name = GssSession.getUniqueUserName().get();
            User userUniqueName = null;
            try {
                userUniqueName = RegistryHandler.INSTANCE.getUserByUniqueName(name);
            } catch (Exception e) {
                StageLogger.logError("LoginDialog", "Error loggin user: " + name, e);
            }
            if (userUniqueName != null)
                return userUniqueName;
        }
        String message = LOGINMESSAGE;
        final GssLoginDialog loginDialog = new GssLoginDialog(shell, LOGIN, message);
        loginDialog.setUsername("");
        int returnCode = loginDialog.open();
        if (returnCode == Window.OK) {
            String username = loginDialog.getUsername();
            String password = loginDialog.getPassword();

            GssSession.setUniqueUserName(username);
            try {
                StageWorkspace.getInstance().getDataFolder(username);
                StageWorkspace.getInstance().getScriptsFolder(username);
                StageWorkspace.getInstance().getGeopaparazziFolder(username);
            } catch (Exception e) {
                StageLogger.logError("LoginDialog", e);
                MessageDialogUtil.openError(shell, "Error", "An error occurred while trying to access the workspace.", null);
            }
            try {
                return LoginChecker.isLoginOk(username, password);
            } catch (Exception e) {
                StageLogger.logError("SensitLoginDialog", e);
            }
        }
        return null;
    }

}
