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
package com.hydrologis.gss.server.utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.hydrologis.gss.server.GssSession;

public class Messages {
    private static final TreeSet<String> baseNames = new TreeSet<>();
    static {
        Messages.addBundle("gss_messages"); //$NON-NLS-1$
    }

    private static final String NO_TRANSLATION_FOR = "No translation for: "; //$NON-NLS-1$

    public static void addBundle( String baseName ) {
        baseNames.add(baseName);
    }

    public static String getString( String key ) {
        return get(key);
    }

    public static String get( String key ) {
        Optional<String> findFirst = baseNames.stream()
                .map(baseName -> ResourceBundle.getBundle(baseName, GssSession.getLocale()))
                .filter(bundle -> bundle.containsKey(key)).map(bundle -> bundle.getString(key)).findFirst();
        return findFirst.orElse(NO_TRANSLATION_FOR + key);
    }

    public static Set<Locale> getAvailableLanguages( File directory ) throws IOException {
        return Files.list(directory.toPath()).map(path -> path.toFile().getName())
                .filter(name -> name.endsWith(".properties") && name.contains("_")) //$NON-NLS-1$ //$NON-NLS-2$
                .map(name -> new Locale(name.substring(name.lastIndexOf("_") + 1, name.lastIndexOf(".")))) //$NON-NLS-1$ //$NON-NLS-2$
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Locale::getLanguage))));
    }

}
