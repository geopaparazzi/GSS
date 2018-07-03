/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.cn1.libs;

import com.codename1.system.NativeInterface;

/**
 *
 * @author hydrologis
 */
public interface HyNativeUtils extends NativeInterface {
    public String PERMISSION_WRITE_EXTERNAL_STORAGE = "WRITE_EXTERNAL_STORAGE";
    
    public boolean checkPemissions(String permissionName, String userMessage);

}
