/** *****************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Author: Antonello Andrea (http://www.hydrologis.com)
 * ****************************************************************************
 */
package com.hydrologis.cn1.libs.kukuratus;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.io.ConnectionRequest;
import com.codename1.ui.Button;
import com.codename1.ui.Dialog;
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
public class KukuratusInfiniteDownloadProgressDialog extends InfiniteProgress {

    public void showInfiniteBlockingWithTitle(String title, Resources theme, ConnectionRequest req) {
        if (theme != null) {
            Image spinnerImage = theme.getImage("infinite_spinner.png");
            setAnimation(spinnerImage);
        }
        Dialog d = new Dialog();
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
