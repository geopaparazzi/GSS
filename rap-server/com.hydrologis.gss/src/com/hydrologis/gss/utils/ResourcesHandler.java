/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;

public class ResourcesHandler {
    public static final String BASE = "hydroserver";

    public static String registerFileByName( File file ) throws Exception {
        ResourceManager resourceManager = RWT.getResourceManager();
        String name = file.getName();
        String resourceName = BASE + "/" + name;
        if (!resourceManager.isRegistered(resourceName)) {
            try(InputStream inputStream = new FileInputStream(file)){
                resourceManager.register(resourceName, inputStream);
            }
        }
        
        return resourceName;
    }

    public static String getResourceUrlByName(String fileName){
        String url = RWT.getResourceManager().getLocation(BASE + "/" + fileName);
        return url;
    }

    public static String getResourceUrlByResourceName(String resourceName){
        String url = RWT.getResourceManager().getLocation(resourceName);
        return url;
    }
    
}
