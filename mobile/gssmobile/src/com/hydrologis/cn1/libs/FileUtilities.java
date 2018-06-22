/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    private FileUtilities() {
        fsStorage = FileSystemStorage.getInstance();
    }

    private boolean isCustomPathSupported() {
        return Database.isCustomPathSupported();
    }

    public String getSdcardFile(String file) {
        String sdcard = getSdcard();
        String sep = getFileSeparator(sdcard);
        String f = sdcard + sep + file;
        if (!new File(f).exists()) {
            return file;
        } else {
            return f;
        }
    }

    public String getSdcard() {
        List<String> rootTypes = getRootTypes();
        int indexOfSdcard = rootTypes.indexOf(SDCARD);
        if (indexOfSdcard == -1) {
            indexOfSdcard = 0;
        }
        String path = getRoots().get(indexOfSdcard);
        return FILE_PREFIX + fsStorage.toNativePath(path);
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
        if (isCustomPathSupported()) {
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
        return files;
    }

}
