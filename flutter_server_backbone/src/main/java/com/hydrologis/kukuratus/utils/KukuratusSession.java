/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.kukuratus.utils;

import java.sql.SQLException;
import java.util.Locale;

import com.hydrologis.kukuratus.registry.RegistryHandler;
import com.hydrologis.kukuratus.registry.Settings;

public class KukuratusSession {
    public static final String KEY_AUTOMATIC_REGISTRATION = "GSS_KEY_AUTOMATIC_REGISTRATION"; //$NON-NLS-1$
    public static final int timerSeconds = 30;

    public static final String KEY_LOCALE = "KUKURATUS_KEY_LOCALE"; //$NON-NLS-1$

    private static Locale applicationLocale = null;

    public static Locale getLocale() {
        if (applicationLocale == null) {
            String localeString = RegistryHandler.INSTANCE.getGlobalSettingByKey(KEY_LOCALE, Locale.ENGLISH.toString());
            applicationLocale = new Locale(localeString);
        }
        return applicationLocale;
    }

    public static void setLocale( Locale locale ) {
        try {
            applicationLocale = locale;
            Settings newSetting = new Settings(KEY_LOCALE, locale.toString(), null);
            RegistryHandler.INSTANCE.insertOrUpdateGlobalSetting(newSetting);
        } catch (SQLException e) {
            KukuratusLogger.logError("KukuratusSession", e); //$NON-NLS-1$
        }
    }

    public static void setLocaleFromString( String localeStr ) {
        setLocale(new Locale(localeStr));
    }
}