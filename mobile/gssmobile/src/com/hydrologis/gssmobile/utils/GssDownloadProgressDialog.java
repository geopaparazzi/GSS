/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.utils;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.io.ConnectionRequest;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.util.Resources;
import com.hydrologis.cn1.libs.HyDialogs;

/**
 *
 * @author hydrologis
 */
public class GssDownloadProgressDialog extends InfiniteProgress {

    public void showInfiniteBlockingWithTitle(String title, Resources theme, ConnectionRequest req) {
//        Form f = Display.getInstance().getCurrent();
//        if (f == null) {
//            f = new Form();
//            f.show();
//        }
//        if (f.getClientProperty("isInfiniteProgress") == null) {
//            f.setTintColor(hyTintColor);
//        }
        if (theme != null) {
            Image spinnerImage = theme.getImage("gss_spinner_128.png");
            setAnimation(spinnerImage);
        }
        Dialog d = new Dialog();

        //Command cancelCommand = new Command(HyDialogs.CANCEL);
        d.putClientProperty("isInfiniteProgress", true);
        d.setTintColor(0xF);
        d.setDialogUIID("HyInfiniteContainer");
        d.setLayout(new BorderLayout());
        d.addComponent(BorderLayout.NORTH, new SpanLabel(title, "hy_infinitedialoglabel"));
        d.addComponent(BorderLayout.CENTER, this);
        if (req != null) {
            Button cancel = new Button(HyDialogs.CANCEL);
            cancel.getAllStyles().setBorder(Border.createEmpty());
            cancel.getAllStyles().setFgColor(0);
            cancel.addActionListener(l -> {
                req.kill();
                d.dispose();
            });
            d.addComponent(BorderLayout.SOUTH, cancel);
        }
        d.setTransitionInAnimator(CommonTransitions.createEmpty());
        d.setTransitionOutAnimator(CommonTransitions.createEmpty());
        d.showPacked(BorderLayout.CENTER, false);
        if (req != null) {
            req.setDisposeOnCompletion(d);
        }
    }

}
