package com.hydrologis.gss.users;

import java.util.List;

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

import com.gasleaksensors.databases.v2.core.handlers.DatabaseHandler;
import com.gasleaksensors.databases.v2.core.objects.Devices;
import com.gasleaksensors.libsV2.IDbProvider;
import com.gasleaksensors.libsV2.SensitContextV2;
import com.j256.ormlite.dao.Dao;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

public class NewDeviceDialog extends Dialog {

    private static final long serialVersionUID = 1L;
    private Label uniqueIdLabel;
    private Text uniqueIdText;

    private final String title;

    public NewDeviceDialog( Shell parent ) {
        super(parent);
        this.title = "Add a new Device";
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

        uniqueIdLabel = new Label(composite, SWT.NONE);
        GridData groupNameData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        uniqueIdLabel.setLayoutData(groupNameData);
        uniqueIdLabel.setText("New Unique Id");

        uniqueIdText = new Text(composite, SWT.BORDER);
        uniqueIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        uniqueIdText.setFocus();

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
                String newUniqueIdText = uniqueIdText.getText();
                newUniqueIdText = newUniqueIdText.trim();
                if (newUniqueIdText.length() == 0) {
                    return;
                }

                IDbProvider dbp = GssContext.instance().getDbProviderForSession();
                DatabaseHandler databaseHandler = dbp.getDatabaseHandler();

                Dao<Devices, ? > devicesDao = databaseHandler.getDao(Devices.class);
                List<Devices> devicesList = devicesDao.queryForAll();

                boolean exists = false;
                for( Devices machine : devicesList ) {
                    if (machine.uniqueid.equals(newUniqueIdText)) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    MessageDialogUtil.openWarning(getParentShell(), "ID EXISTS",
                            "A device with this id already exists in the database: " + newUniqueIdText, null);
                    return;
                } else {
                    long id = 0;
                    try {
                        id = devicesDao.queryRawValue(
                                "select max(" + Devices.ID_FIELD_NAME + ") from " + DatabaseHandler.getTableName(Devices.class));
                        id++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Devices newD = new Devices(id, newUniqueIdText, 1);
                    devicesDao.create(newD);
                    setReturnCode(OK);
                    close();
                }
            } catch (Exception e) {
                StageLogger.logError(this, e);
            }

        }
        super.buttonPressed(buttonId);
    }
}
