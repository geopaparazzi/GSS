package com.hydrologis.gss.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ClientFileLoader;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ResourceManager;

import eu.hydrologis.stage.libs.log.StageLogger;

public class JsResources {

    private static final String[][] javascriptFiles = new String[][]{//
            // {"libs/leaflet.easyPrint.js", "leaflet.easyPrint.js"},
    };

    private static List<String> toRequireList = new ArrayList<String>();
    private static String toRequireLeaksMap;
    private static String toRequireGpspointsMap;

    private static final ResourceLoader resourceLoader = new ResourceLoader(){
        public InputStream getResourceAsStream( String resourceName ) throws IOException {
            return JsResources.class.getClassLoader().getResourceAsStream(resourceName);
        }
    };

    public static void ensureJavaScriptResources() {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();

        for( String[] jsFile : javascriptFiles ) {
            try {
                if (!resourceManager.isRegistered(jsFile[0])) {
                    InputStream resourceAsStream = resourceLoader.getResourceAsStream(jsFile[1]);
                    if (resourceAsStream == null) {
                        StageLogger.logError("JsResources", "Could not load resource: " + jsFile[1], new IOException());
                        continue;
                    }
                    String registered = register(resourceManager, jsFile[0], resourceAsStream);
                    if (!toRequireList.contains(registered))
                        toRequireList.add(registered);
                }
            } catch (IOException e) {
                StageLogger.logError("JsResources", e);
            }
        }
        ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
        for( String toRequireStr : toRequireList ) {
            loader.requireJs(toRequireStr);
        }
    }

    public static String ensureLeaksMapHtmlResource() {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();
        try {
            String fileName = "leaksmap.html";
            if (!resourceManager.isRegistered(fileName)) {
                InputStream resourceAsStream = resourceLoader.getResourceAsStream(fileName);
                if (resourceAsStream == null) {
                    StageLogger.logError("JsResources", "Could not load resource: " + fileName, new IOException());
                    return null;
                }
                String registered = register(resourceManager, fileName, resourceAsStream);
                if (toRequireLeaksMap == null)
                    toRequireLeaksMap = registered;
            }
        } catch (IOException e) {
            StageLogger.logError("JsResources", e);
        }
        if (toRequireLeaksMap != null) {
            ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
            loader.requireJs(toRequireLeaksMap);
        }
        return toRequireLeaksMap;
    }

    public static String ensureGpspointsMapHtmlResource() {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();
        try {
            String fileName = "gpspoints.html";
            if (!resourceManager.isRegistered(fileName)) {
                InputStream resourceAsStream = resourceLoader.getResourceAsStream(fileName);
                if (resourceAsStream == null) {
                    StageLogger.logError("JsResources", "Could not load resource: " + fileName, new IOException());
                    return null;
                }
                String registered = register(resourceManager, fileName, resourceAsStream);
                if (toRequireGpspointsMap == null)
                    toRequireGpspointsMap = registered;
            }
        } catch (IOException e) {
            StageLogger.logError("JsResources", e);
        }
        if (toRequireGpspointsMap != null) {
            ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
            loader.requireJs(toRequireGpspointsMap);
        }
        return toRequireGpspointsMap;
    }


    public static String registerIfMissing( String resource ) {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();
        try {
            // load html
            String location;
            if (!resourceManager.isRegistered(resource)) {
                InputStream resourceAsStream = resourceLoader.getResourceAsStream(resource);
                if (resourceAsStream == null) {
                    StageLogger.logError("JsResources", "Could not load resource: " + resource, new IOException());
                    return null;
                }
                location = register(resourceManager, resource, resourceAsStream);
            } else {
                location = resourceManager.getLocation(resource);
            }
            ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
            loader.requireJs(resource);
            return location;
        } catch (IOException e) {
            StageLogger.logError("JsResources", e);
        }
        return null;
    }

    private static String register( ResourceManager resourceManager, String registerPath, InputStream inputStream )
            throws IOException {
        String location;
        try {
            resourceManager.register(registerPath, inputStream);
            location = resourceManager.getLocation(registerPath);
        } finally {
            inputStream.close();
        }
        return location;
    }

}
