/*******************************************************************************
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
 ******************************************************************************/
package com.hydrologis.cn1.libs;

import com.codename1.components.InfiniteProgress;
import com.codename1.io.JSONParser;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkEvent;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.util.regex.StringReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 *
 * @author hydrologis
 */
public class HyUploadProgressForm extends Form {

    private boolean canCancel;
    private InfiniteProgress infiniteProgress;

    public HyUploadProgressForm(Form previous, String title, boolean canCancel) {
        this.canCancel = canCancel;
        init(title, previous);
    }

    private void init(String title, Form previous) {
        setLayout(BoxLayout.y());
        Toolbar tb = getToolbar();
        tb.setBackCommand("Back", (e) -> {
            if (canCancel) {
                onCancel();
                previous.showBack();
            }
        });
        setTitle(title);

        infiniteProgress = new InfiniteProgress();
        infiniteProgress.setUIID("uploadInfiniteProgress");

        add(BorderLayout.centerCenter(infiniteProgress));
    }

    public void upload(String processName, MultipartRequest mpr) {
        Label progressLabel = new Label("", "uploadProgressLabel");
        add(new Label("Started upload: " + processName, "uploadProgressLabel"));
        add(progressLabel);
        NetworkManager.getInstance().addProgressListener(new ActionListener() {
            private int currentLength = -1;
            private boolean finished = false;

            @Override
            public void actionPerformed(ActionEvent evt) {
                NetworkEvent e = (NetworkEvent) evt;
                if (currentLength == -1) {
                    currentLength = e.getLength();
                }
                switch (e.getProgressType()) {
                    case NetworkEvent.PROGRESS_TYPE_COMPLETED:
                        NetworkManager.getInstance().removeProgressListener(this);
                        break;
                    case NetworkEvent.PROGRESS_TYPE_INITIALIZING:
                        break;
                    case NetworkEvent.PROGRESS_TYPE_INPUT:
                    case NetworkEvent.PROGRESS_TYPE_OUTPUT:
                        if (!finished) {
                            int sentReceived = e.getSentReceived();
                            if (currentLength != -1) {
                                progressLabel.setText("Sent " + sentReceived + " of " + currentLength + " bytes");
                            } else {
                                progressLabel.setText("Sent " + sentReceived + " bytes");
                            }
                            revalidate();
                            if (currentLength != -1 && sentReceived == currentLength) {
                                finished = true;
                            }
                        }
                        break;
                }
            }
        });
        NetworkManager.getInstance().addToQueue(mpr);
    }

    public void onCancel() {

    }

    public void dismiss() {
        infiniteProgress.remove();
        canCancel = true;
    }

}
