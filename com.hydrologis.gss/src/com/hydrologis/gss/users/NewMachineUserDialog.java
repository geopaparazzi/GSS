package com.hydrologis.gss.users;

import java.util.List;
import java.util.stream.Collectors;

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

import com.gasleaksensors.databases.v2.core.handlers.DatabaseHandler;
import com.gasleaksensors.databases.v2.core.objects.UserLevels;
import com.gasleaksensors.databases.v2.core.objects.Users;
import com.gasleaksensors.libsV2.IDbProvider;
import com.gasleaksensors.libsV2.SensitContextV2;
import com.hydrologis.gss.utils.SensitDatabaseUtilities;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

public class NewMachineUserDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private Text userNameText;

    private final String title;

    private Text nameText;

    private Text pwdText;

    private Combo userLevelCombo;

    private IDbProvider dbp;

    public NewMachineUserDialog( Shell parent ) {
        super(parent);
        this.title = "Add a new User";

        dbp = GssContext.instance().getDbProviderForSession();
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
        GridLayout mainLayout = new GridLayout(2, false);
        composite.setLayout(mainLayout);

        Label userNameLabel = new Label(composite, SWT.NONE);
        userNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        userNameLabel.setText("Unique username");
        userNameText = new Text(composite, SWT.BORDER);
        userNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        userNameText.setFocus();

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameLabel.setText("Full name");
        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label pwdLabel = new Label(composite, SWT.NONE);
        pwdLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pwdLabel.setText("Password");
        pwdText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        pwdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label userLevelLabel = new Label(composite, SWT.NONE);
        userLevelLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        userLevelLabel.setText("User level");
        userLevelCombo = new Combo(composite, parent.getStyle());
        try {
            List<String> levelNamesList = dbp.getDatabaseHandler().getDao(UserLevels.class).queryForAll().stream()
                    .map(ul -> ul.description).collect(Collectors.toList());
            userLevelCombo.setItems(levelNamesList.toArray(new String[0]));
            userLevelCombo.select(0);
        } catch (Exception e) {
            StageLogger.logError(this, e);
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
                String uniqueStr = userNameText.getText().trim();
                String nameStr = nameText.getText().trim();
                String pwdStr = pwdText.getText().trim();
                String userLevelDescription = userLevelCombo.getText();
                DatabaseHandler databaseHandler = dbp.getDatabaseHandler();
                Dao<Users, ? > uDao = databaseHandler.getDao(Users.class);
                Dao<UserLevels, ? > ulDao = databaseHandler.getDao(UserLevels.class);
                QueryBuilder<Users, ? > uQB = uDao.queryBuilder();
                QueryBuilder<UserLevels, ? > ulQB = ulDao.queryBuilder();
                if (uniqueStr.length() > 0) {
                    Users tmpU = SensitDatabaseUtilities.getUserByName(uQB, uniqueStr);
                    if (tmpU == null) {
                        uQB.reset();
                        long maxId = 0;
                        try {
                            maxId = uDao.queryRawValue(
                                    "select max(" + Users.ID_FIELD_NAME + ") from " + DatabaseHandler.getTableName(Users.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Users newU = new Users(maxId + 1);
                        newU.username = uniqueStr;
                        newU.name = nameStr;
                        if (pwdStr.length() == 0)
                            pwdStr = nameStr;
                        newU.password = pwdStr;

                        UserLevels defaultUserLevel = SensitDatabaseUtilities.getUserLevelByName(ulQB, userLevelDescription);
                        newU.userLevelsId = defaultUserLevel;
                        uDao.create(newU);

                        setReturnCode(OK);
                        close();
                    } else {
                        MessageDialogUtil.openWarning(getParentShell(), "USER EXISTS",
                                "A user by that name already exists in the database: " + uniqueStr, null);
                    }
                } else {
                    return;
                }

            } catch (Exception e) {
                StageLogger.logError(this, e);
            }
        }
        super.buttonPressed(buttonId);
    }
}
