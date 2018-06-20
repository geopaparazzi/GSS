/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.utils;

import com.hydrologis.gssmobile.database.Notes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public class SyncData {
    public static final String MEDIA = "Media";
    public static final String GPS_LOGS = "Gps Logs";
    public static final String NOTES = "Notes";

    public final List<Object> rootsList = Arrays.asList(NOTES, GPS_LOGS, MEDIA);

    public final HashMap<String, List<?>> type2ListMap = new HashMap<>();
    
}
