/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.utils;

import com.codename1.components.SpanLabel;
import com.codename1.io.Preferences;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.Constraint;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.Validator;
import com.hydrologis.cn1.libs.HyDialogs;
import com.hydrologis.cn1.libs.HyUtilities;

/**
 *
 * @author hydrologis
 */
public class ServerUrlDialog extends Dialog {

    private final TextField serverUrlField;

    public ServerUrlDialog() {

        super("Server URL");

        setLayout(BoxLayout.y());

        SpanLabel titleLabel = new SpanLabel("Insert the server to connect to");

        String serverUrl = Preferences.get(GssUtilities.SERVER_URL, "http://localhost:8080/upload");

        serverUrlField = new TextField(serverUrl, "Complete server URL");

        Validator val = new Validator();
        val.addConstraint(serverUrlField, new UrlConstraint());

        add(titleLabel);
        add(serverUrlField);

        setEditOnShow(serverUrlField);

        Label grayLabel = new Label();
        grayLabel.setShowEvenIfBlank(true);
        grayLabel.getUnselectedStyle().setBgColor(0xcccccc);
        grayLabel.getUnselectedStyle().setPadding(1, 1, 1, 1);
        grayLabel.getUnselectedStyle().setPaddingUnit(Style.UNIT_TYPE_PIXELS);
        add(grayLabel);

        Button ok = new Button(new Command(HyDialogs.OK));
        ok.getAllStyles().setBorder(Border.createEmpty());
        ok.getAllStyles().setFgColor(0);
        add(ok);
        
        val.addSubmitButtons(ok);

        setDisposeWhenPointerOutOfBounds(true);

    }

    public String getServerUrl() {
        return serverUrlField.getText();
    }

}
