/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.cn1.libs;

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
        return false; // to be changed with Database.isCustomPathSupported() when it is ready
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
    
    public String toNativePath(String path){
        return fsStorage.toNativePath(path);
    }

}
