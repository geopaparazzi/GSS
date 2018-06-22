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
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.Validator;
import com.hydrologis.cn1.libs.HyUtilities;

/**
 *
 * @author hydrologis
 */
public class UdidDialog extends Dialog {

    private TextField udidField;

    public UdidDialog() {
        super("Device UDID not supported");

        setLayout(BoxLayout.y());

        SpanLabel titleLabel = new SpanLabel("Please insert a user selected unique id");

        String udid = Preferences.get(HyUtilities.CUSTOM_UDID, "");
        udidField = new TextField(udid, "Insert UDID of at least 10 chars");

        Validator val = new Validator();
        val.addConstraint(udidField, new LengthConstraint(10));

        add(titleLabel);
        add(udidField);

        setEditOnShow(udidField);

        Label grayLabel = new Label();
        grayLabel.setShowEvenIfBlank(true);
        grayLabel.getUnselectedStyle().setBgColor(0xcccccc);
        grayLabel.getUnselectedStyle().setPadding(1, 1, 1, 1);
        grayLabel.getUnselectedStyle().setPaddingUnit(Style.UNIT_TYPE_PIXELS);
        add(grayLabel);

        Button ok = new Button(new Command(HyUtilities.OK));
        ok.getAllStyles().setBorder(Border.createEmpty());
        ok.getAllStyles().setFgColor(0);
        add(ok);
        
        val.addSubmitButtons(ok);

        setDisposeWhenPointerOutOfBounds(true);

    }

    public String getUdid() {
        return udidField.getText();
    }

}
