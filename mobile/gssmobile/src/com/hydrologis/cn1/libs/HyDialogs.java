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

import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;

/**
 *
 * @author hydrologis
 */
public class HyDialogs {

    public static final String OK = "OK";

    public static FontImage ERR_IMAGE = FontImage.createMaterial(FontImage.MATERIAL_ERROR, "Error", 4);
    public static FontImage INFO_IMAGE = FontImage.createMaterial(FontImage.MATERIAL_INFO, "Info", 4);
    public static FontImage WARNING_IMAGE = FontImage.createMaterial(FontImage.MATERIAL_WARNING, "Warning", 4);

    public static void showErrorDialog(String msg) {
        Dialog.show("ERROR", msg, Dialog.TYPE_ERROR, ERR_IMAGE, OK, null);
    }

    public static void showInfoDialog(String msg) {
        showInfoDialog("INFO", msg);
    }

    public static void showInfoDialog(String title, String msg) {
        Dialog.show(title, msg, Dialog.TYPE_INFO, INFO_IMAGE, OK, null);
    }

    public static void showWarningDialog(String msg) {
        showInfoDialog("WARNING", msg);
    }

    public static void showWarningDialog(String title, String msg) {
        Dialog.show(title, msg, Dialog.TYPE_WARNING, WARNING_IMAGE, OK, null);
    }

}
