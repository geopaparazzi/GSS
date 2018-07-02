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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A singleton cache for images.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class ImageCache {
    public static final String LOGO = "logo.png";
    public static final String LOGO_LOGIN = "logo_login.png";
    public static final String GROUP = "group.gif";
    public static final String DASHBOARD = "home.gif";
    public static final String USER = "user.gif";
    public static final String WEBUSERS = "webusers.gif";
    public static final String EVENTI = "eventi.gif";
    public static final String GPSPOINTS = "gpspoints.gif";
    public static final String WORKSTATS = "work_stats.gif";
    public static final String REPORTS = "reports.gif";
    public static final String SAVE_SPACE = "save_edit_space.gif";
    public static final String FILLER = "filler.png";

    private static ImageCache imageCache;

    private HashMap<String, Image> imageMap = new HashMap<String, Image>();

    private ImageCache() {
    }

    public static ImageCache getInstance() {
        if (imageCache == null) {
            imageCache = new ImageCache();
        }
        return imageCache;
    }

    /**
     * Get an image for a certain key.
     * 
     * <p>
     * <b>The only keys to be used are the static strings in this class!!</b>
     * </p>
     * 
     * @param key
     *            a file key, as for example {@link ImageCache#DATABASE_VIEW}.
     * @return the image.
     */
    public Image getImage( Display display, String key ) {
        Image image = imageMap.get(key);
        if (image == null) {
            image = createImage(display, key);
            imageMap.put(key, image);
        }
        return image;
    }

    private Image createImage( Display display, String key ) {
        Image image = getImageFromResource(display, key);
        return image;
    }

    /**
     * Disposes the images and clears the internal map.
     */
    public void dispose() {
        Set<Entry<String, Image>> entrySet = imageMap.entrySet();
        for( Entry<String, Image> entry : entrySet ) {
            entry.getValue().dispose();
        }
        imageMap.clear();
    }

    public static Image getImageFromResource( Display display, String path ) {
        ClassLoader classLoader = ImageCache.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("resources/" + path);
        Image result = null;
        if (inputStream != null) {
            try {
                result = new Image(display, inputStream);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return result;
    }

}
