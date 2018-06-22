/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.cn1.libs;

import com.codename1.io.Preferences;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;

/**
 *
 * @author hydrologis
 */
public class HyUtilities {

    public static FontImage ERR_IMAGE = FontImage.createMaterial(FontImage.MATERIAL_ERROR, "Error", 4);
    public static FontImage INFO_IMAGE = FontImage.createMaterial(FontImage.MATERIAL_INFO, "Info", 4);
    
    public static final String OK = "OK";

    public static String CUSTOM_UDID = "GSS_CUSTOM_UDID";
    public static String MIMETYPE_BYTEARRAY = "application/octet-stream";

    public static String getUdid() {
        String udid = Display.getInstance().getUdid();
        if (udid == null) {
            udid = Preferences.get(CUSTOM_UDID, null);
            if (udid == null || udid.length() == 0) {
                return null;
            }
        }
        return udid;
    }

    public static void showErrorDialog(String msg) {
        Dialog.show("ERROR", msg, Dialog.TYPE_ERROR, ERR_IMAGE, OK, null);
    }
    public static void showInfoDialog(String msg) {
        Dialog.show("INFO", msg, Dialog.TYPE_INFO, INFO_IMAGE, OK, null);
    }

}
