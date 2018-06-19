package com.hydrologis.gss.entrypoints.settings;

import java.sql.SQLException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.j256.ormlite.dao.Dao;

import eu.hydrologis.stage.libs.log.StageLogger;

/**
 * GpapUsers edit dialog.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpapUserEditDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private GpapUsers user;
    private Text nameText;
    private Text contactText;

    public GpapUserEditDialog( Shell parent, GpapUsers user ) {
        super(parent);
        // setShellStyle(SWT.NO_TRIM);
        this.user = user;
    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        shell.setText("Edit Surveyor: " + user.deviceId);
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

        Label nameLabel = new Label(composite, SWT.NONE);
        GridData nameLabelGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        nameLabel.setLayoutData(nameLabelGD);
        nameLabel.setText("Name");
        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(user.name);

        final ControlDecoration dynDecoration = new ControlDecoration(nameText, SWT.NONE);
        FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
        Image icon = registry.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
        dynDecoration.setImage(icon);
        dynDecoration.setMarginWidth(3);
        dynDecoration.hide();
        nameText.addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent event ) {
                if (nameText.getText().length() > 0) {
                    dynDecoration.hide();
                } else {
                    dynDecoration.show();
                }
            }

        });
        dynDecoration.setShowHover(true);
        dynDecoration.setShowOnlyOnFocus(true);
        dynDecoration.setDescriptionText("The name of the surveyor is mandatory.");

        Label contactLabel = new Label(composite, SWT.NONE);
        GridData contactLabelGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        contactLabel.setLayoutData(contactLabelGD);
        contactLabel.setText("Contact");
        contactText = new Text(composite, SWT.BORDER);
        contactText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        String contact = user.contact;
        if (contact != null) {
            contactText.setText(contact);
        }

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.get().CANCEL_LABEL, false);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.get().OK_LABEL, true);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        if (buttonId == IDialogConstants.OK_ID) {
            String name = nameText.getText();
            String contact = contactText.getText();

            user.name = name;
            if (contact.trim().length() > 0) {
                user.contact = contact;
            }

            try {
                Dao<GpapUsers, ? > userDao = GssContext.instance().getDbProvider().getDatabaseHandler().getDao(GpapUsers.class);
                userDao.update(user);
            } catch (SQLException e) {
                StageLogger.logError(this, e);
            }

            setReturnCode(OK);
            close();
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 300);
    }
}
