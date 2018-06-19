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
