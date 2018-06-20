/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.cn1.libs;

import com.codename1.l10n.DateFormat;
import com.codename1.l10n.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author hydrologis
 */
public class TimeUtilities {
    public static final SimpleDateFormat readableFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static String toYYYYMMDDHHMMSS(long ts){
        Date d = new Date(ts);
        return readableFormat.format(d);
    }
    
}
