/** *****************************************************************************
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
 * ****************************************************************************
 */
package com.hydrologis.cn1.libs;

import com.codename1.db.Database;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public enum FileUtilities {
    INSTANCE;

    public static final String SDCARD = "SDCARD";
    public static final String FILE_PREFIX = "file://";

    final FileSystemStorage fsStorage;

    private String sdcardPath = null;

    private FileUtilities() {
        fsStorage = FileSystemStorage.getInstance();
    }

    private boolean isCustomPathSupported() {
        return Database.isCustomPathSupported();
    }

    public String getSdcardFile(String file) throws IOException {
        String sdcard = getSdcard();
        String sep = getFileSeparator(sdcard);
        String f = sdcard + sep + file;
        if (!new File(f).exists()) {
            return file;
        } else {
            return f;
        }
    }

    public String getSdcard() throws IOException {
        if (sdcardPath == null) {
            HyLog.d("Looking for sdcard to use");

            List<String> rootTypes = getRootTypes();
            HyLog.d("Root types found:");
            for (String rootType : rootTypes) {
                HyLog.d("->" + rootType);
            }
            int indexOfSdcard = rootTypes.indexOf(SDCARD);
            if (indexOfSdcard == -1) {
                indexOfSdcard = 0;
            }
            HyLog.d("Roots found:");
            List<String> roots = getRoots();
            for (String root : roots) {
                HyLog.d("->" + root);
            }

            int rootsSize = roots.size();
            if (indexOfSdcard > rootsSize) {
                HyLog.d("Unable to get sdcard. (" + indexOfSdcard + " vs. " + rootsSize + ")");
                throw new IOException();
            }

            String path = roots.get(indexOfSdcard);
            HyLog.d("Using root: " + path);
            sdcardPath = FILE_PREFIX + fsStorage.toNativePath(path);
            HyLog.d("As native path: " + sdcardPath);

            final boolean customPathSupported = isCustomPathSupported();
            HyLog.d("Is database custom path supported: " + customPathSupported);
        }
        return sdcardPath;
    }

    public List<String> getRoots() {
        String[] roots = fsStorage.getRoots();
        return Arrays.asList(roots);
    }

    public List<String> getRootsNative() {
        List<String> roots = getRoots();
        List<String> nativeRoots = new ArrayList<>();
        for (String root : roots) {
            nativeRoots.add(fsStorage.toNativePath(root));
        }
        return nativeRoots;
    }

    public List<String> getRootTypes() {
        List<String> roots = getRoots();
        List<String> rootTypes = new ArrayList<>();
        for (String root : roots) {
            int rootType = fsStorage.getRootType(root);
            String rt = rootType == 1 ? "MAINSTORAGE" : rootType == 2 ? SDCARD : "UNKNOWN";
            rootTypes.add(rt);
        }
        return rootTypes;
    }

    public String[] listFiles(String parent) throws IOException {
        String sep = getFileSeparator(parent);

        final String[] listFiles = fsStorage.listFiles(parent);
        if (listFiles == null) {
            HyLog.d("Found 0 files in parent: " + parent);
        }
        final boolean customPathSupported = isCustomPathSupported();
        if (customPathSupported && listFiles != null) {
            String[] newList = new String[listFiles.length];
            for (int i = 0; i < newList.length; i++) {
                String newFile = parent + sep + listFiles[i];
                newList[i] = newFile;
            }
            return newList;
        } else {
            return listFiles;
        }
    }

    private String getFileSeparator(String parent) {
        String sep = File.separator;
        if (parent.endsWith(File.separator)) {
            sep = "";
        }
        return sep;
    }

    public String toNativePath(String path) {
        return fsStorage.toNativePath(path);
    }

    /**
     * Find the files by a given extension.
     *
     * <p>
     * <b>NOTE: this right now goes down only one level</b></p>
     *
     * @param parent the parent path.
     * @param ext the extension.
     * @return the list of files found.
     * @throws IOException
     */
    public List<String> findFilesByExtension(String parent, String ext) throws IOException {
        List<String> files = new ArrayList<>();
        if (parent == null) {
            parent = getSdcard();
        }
        String[] listFiles = listFiles(parent);
        if (listFiles != null) {
            for (String file : listFiles) {
                if (new File(file).isDirectory()) {
                    String[] filesInFolders = listFiles(file);
                    for (String fileInFolder : filesInFolders) {
                        if (!new File(fileInFolder).isDirectory() && fileInFolder.endsWith(ext)) {
                            files.add(fileInFolder);
                        }
                    }
                } else {
                    if (file.endsWith(ext)) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    public static String stripFileProtocol(String path) {
        if (path.startsWith(FILE_PREFIX)) {
            path = path.substring(FILE_PREFIX.length());
        }
        return path;
    }

}
