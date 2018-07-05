/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.cn1.libs;

import com.codename1.io.Preferences;
import com.codename1.ui.Display;

/**
 *
 * @author hydrologis
 */
public class HyUtilities {

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


}
