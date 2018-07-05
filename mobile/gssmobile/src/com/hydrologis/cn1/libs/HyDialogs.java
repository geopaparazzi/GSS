/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
