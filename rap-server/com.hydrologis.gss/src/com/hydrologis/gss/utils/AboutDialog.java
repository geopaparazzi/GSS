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

import java.awt.image.BufferedImage;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {

    private static final String CANCEL = "Cancel";
    private final String title;
    private BufferedImage image;

    public AboutDialog( Shell parent, String title ) {
        super(parent);
        setShellStyle(SWT.NO_TRIM);
        this.title = title;
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
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 15;
        layout.marginWidth = 15;
        layout.verticalSpacing = 15;
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);

        Layout layout2 = parent.getLayout();
        if (layout2 instanceof GridLayout) {
            GridLayout gl2 = (GridLayout) layout2;
            gl2.marginBottom = 0;
            gl2.marginRight = 30;
            gl2.verticalSpacing = 0;
            gl2.horizontalSpacing = 0;
        }

        CLabel logoLabel = new CLabel(composite, SWT.WRAP);
        logoLabel.setImage(ImageCache.getInstance().getImage(parent.getDisplay(), ImageCache.LOGO_LOGIN));
        String gssText = "<big><b>Geopaparazzi Survey Server</b></big> is brought to you by <a href='http://www.hydrologis.com'>HydroloGIS</a>.";
        gssText += "<br/><br/>It is built with <a href='http://www.eclipse.org/rap'>Eclipse RAP</a>. The application is released under the "
                + "<a href='https://www.eclipse.org/legal/epl-v20.html'>EPL</a>. <br/>The source code is shipped with the application.";
        CLabel gssLabel = new CLabel(composite, SWT.WRAP);
        gssLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        gssLabel.setText(gssText);
        String gpapText = "Information about <big><b>Geopaparazzi</b></big> can be found <a href='http://www.geopaparazzi.eu'>here</a>.";
        CLabel gpapLabel = new CLabel(composite, SWT.WRAP);
        gpapLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        gpapLabel.setText(gpapText);

        Button closeButton = new Button(parent, SWT.FLAT);
        closeButton.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, false));
        closeButton.setImage(eu.hydrologis.stage.libs.utils.ImageCache.getInstance().getImage(parent.getDisplay(),
                eu.hydrologis.stage.libs.utils.ImageCache.CLOSE_VIEW));
        closeButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        closeButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                close();
            }
        });

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        // createButton(parent, IDialogConstants.CANCEL_ID, CANCEL, true);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        super.buttonPressed(buttonId);
    }

    @Override
    protected Point getInitialSize() {
        if (image != null) {
            return new Point(image.getWidth() + 20, image.getHeight() + 80);
        }
        return new Point(600, 500);
    }

    @Override
    public boolean close() {
        return super.close();
    }

}
