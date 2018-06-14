package com.hydrologis.gss.entrypoints.settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.hydrologis.gss.utils.GssGuiUtilities;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.registry.Settings;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

@SuppressWarnings("serial")
public class OtherConfigsUIHandler {

    public void buildGui( Composite buttonsComposite, Composite parametersComposite,
            GssUserPermissionsHandler permissionsHandler ) {
        Shell parentShell = buttonsComposite.getShell();

        Composite otherConfigsComposite = new Composite(parametersComposite, SWT.NONE);
        otherConfigsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout groupsAndUsersLayout = new GridLayout(1, false);
        groupsAndUsersLayout.verticalSpacing = 10;
        otherConfigsComposite.setLayout(groupsAndUsersLayout);

        try {
            Group userPermissionsGroup = new Group(otherConfigsComposite, SWT.NONE);
            userPermissionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            userPermissionsGroup.setLayout(new GridLayout(1, false));
            userPermissionsGroup.setText("Pages allowed to normal users");

            List<String> allPagesNames = permissionsHandler.getAllPagesNames();
            List<String> allowedPagesNames = permissionsHandler.getAllowedPagesNames();
            for( String name : allPagesNames ) {
                final Button checkButton = new Button(userPermissionsGroup, SWT.CHECK);
                checkButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                checkButton.setText(name);
                if (allowedPagesNames.contains(name)) {
                    checkButton.setSelection(true);
                }
                checkButton.addSelectionListener(new SelectionAdapter(){
                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        String userPermissions = RegistryHandler.INSTANCE
                                .getSettingByKey(GssGuiUtilities.KEY_USER_VIEWS_PERMISSIONS, null);
                        List<String> permList = new ArrayList<>();
                        if (userPermissions != null) {
                            Collections.addAll(permList, userPermissions.split(GssGuiUtilities.DELIMITER));
                        }

                        boolean selection = checkButton.getSelection();
                        if (selection) {
                            // make sure it is there
                            if (!permList.contains(name)) {
                                permList.add(name);
                            }
                        } else {
                            // remove it
                            permList.removeIf(n -> n.equals(name));
                        }

                        String newUserPermissions = permList.stream().collect(Collectors.joining(GssGuiUtilities.DELIMITER));
                        try {
                            RegistryHandler.INSTANCE.insertOrUpdateSetting(
                                    new Settings(GssGuiUtilities.KEY_USER_VIEWS_PERMISSIONS, newUserPermissions));
                        } catch (SQLException e1) {
                            StageLogger.logError(this, e1);
                            MessageDialogUtil.openConfirm(parentShell, "ERROR", "An error occurred while storing the preference.",
                                    null);
                        }
                    }
                });

            }

        } catch (Exception e) {
            StageLogger.logError(this, e);
        }

        Button otherConfigsButton = new Button(buttonsComposite, SWT.PUSH);
        GridData otherConfigsButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        otherConfigsButtonGD.heightHint = 100;
        otherConfigsButton.setLayoutData(otherConfigsButtonGD);
        otherConfigsButton.setBackground(buttonsComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        otherConfigsButton.setText("<b><i>Other configurations</i></b>");
        otherConfigsButton.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        otherConfigsButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                StackLayout parametersStackLayout = (StackLayout) parametersComposite.getLayout();
                parametersStackLayout.topControl = otherConfigsComposite;
                parametersComposite.layout();
            }
        });
    }

}
