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
package com.hydrologis.cn1.libs;

import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;

/**
 *
 * @author hydrologis
 */
public class LogCommentDialog extends Dialog {

    private Command cancelCommand = new Command(HyDialogs.CANCEL);
    private Command okCommand = new Command(HyDialogs.OK);

    private final TextArea commentField;

    public LogCommentDialog() {

        super("Log comment");

        setLayout(BoxLayout.y());

        SpanLabel titleLabel = new SpanLabel("Please add commment and/or email to help the developers to understand the issue.");

        commentField = new TextArea();
        commentField.setRows(6);

        add(titleLabel);
        add(commentField);

        setEditOnShow(commentField);

        Label grayLabel = new Label();
        grayLabel.setShowEvenIfBlank(true);
        grayLabel.getUnselectedStyle().setBgColor(0xcccccc);
        grayLabel.getUnselectedStyle().setPadding(1, 1, 1, 1);
        grayLabel.getUnselectedStyle().setPaddingUnit(Style.UNIT_TYPE_PIXELS);
        add(grayLabel);

        Button ok = new Button(okCommand);
        ok.getAllStyles().setBorder(Border.createEmpty());
        ok.getAllStyles().setFgColor(0);

        Button cancel = new Button(cancelCommand);
        cancel.getAllStyles().setBorder(Border.createEmpty());
        cancel.getAllStyles().setFgColor(0);

        Container buttons = new Container(new GridLayout(2));
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons);
    }

    public String getLogComment() {
        return commentField.getText();
    }

    public boolean openAndWait() {
        Command lastPressedDialog = showDialog();
        return lastPressedDialog == okCommand;
    }
}
