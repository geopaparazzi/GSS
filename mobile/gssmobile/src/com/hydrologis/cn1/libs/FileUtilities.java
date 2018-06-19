/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.cn1.libs;

import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public class FileUtilities {

    public static final String SDCARD = "SDCARD";
    public static final String FILE_PREFIX = "file://";

    final FileSystemStorage fsStorage;

    public FileUtilities() {
        fsStorage = FileSystemStorage.getInstance();
    }

    public String getSdcardFile(String file) {
        String sdcard = fsStorage.toNativePath(getSdcard());
        File f = new File(FILE_PREFIX + sdcard + File.separator + file);
        if (!f.exists()) {
            return file;
        } else {
            return f.getAbsolutePath();
        }
    }

    public String getSdcard() {
        List<String> rootTypes = getRootTypes();
        int indexOfSdcard = rootTypes.indexOf(SDCARD);
        if (indexOfSdcard != -1) {
            return getRoots().get(indexOfSdcard);
        } else {
            return getRoots().get(0);
        }
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

}
