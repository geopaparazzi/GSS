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
package com.hydrologis.kukuratus.workspace;

import java.io.File;
import java.io.IOException;

import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.log.Logger;

import com.hydrologis.kukuratus.KukuratusLibs;

/**
 * The main workspace class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)O
 *
 */
public class KukuratusWorkspace {
    private static final String REGISTRY_DB_NAME = "registry.sqlite"; //$NON-NLS-1$
    private static final String KUKURATUS_WORKSPACE = "KukuratusWorkspace"; //$NON-NLS-1$
    /**
     * Name of the default geopaparazzi project folder.
     */
    public static final String GEOPAPARAZZI_FOLDERNAME = "geopaparazzi"; //$NON-NLS-1$
    /**
     * Name of the default data folder.
     */
    public static final String DATA_FOLDERNAME = "DATA"; //$NON-NLS-1$
    /**
     * Name of the default workspace folder.
     */
    public static final String WORKSPACE_FOLDERNAME = "WORKSPACE"; //$NON-NLS-1$
    /**
     * Name of the workspace tmp folder.
     */
    public static final String TMP_FOLDERNAME = "tmp"; //$NON-NLS-1$
    /**
     * Name of the default scripts folder.
     */
    public static final String SCRIPTS_FOLDERNAME = "scripts"; //$NON-NLS-1$

    /**
     * The java -D commandline property that defines the workspace.
     */
    public static final String STAGE_GLOBALFOLDER_JAVA_PROPERTIES_KEY = "stage.globalfolder"; //$NON-NLS-1$

    private Logger logDb = Logger.INSTANCE;
    private static KukuratusWorkspace stageWorkspace;

    private File kukuratusMainFolder;
    private File dataFolder;
    private File workspaceFolder;
    private File tmpFolderFile;

    public static KukuratusWorkspace getInstance() {
        if (stageWorkspace == null) {
            try {
                KukuratusLibs.init();
                stageWorkspace = new KukuratusWorkspace();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return stageWorkspace;
    }

    public static void setWorkspaceFolderPath( String path ) {
        System.setProperty(STAGE_GLOBALFOLDER_JAVA_PROPERTIES_KEY, path);
    }

    private KukuratusWorkspace() throws Exception {
        /*
         * first check if there is a properties file available
         */
        String globalFolderPath = System.getProperty(STAGE_GLOBALFOLDER_JAVA_PROPERTIES_KEY);
        if (globalFolderPath != null) {
            logDb.insertInfo(KUKURATUS_WORKSPACE, "GlobalFolder supplied: " + globalFolderPath); //$NON-NLS-1$
            kukuratusMainFolder = new File(globalFolderPath);
            if (!kukuratusMainFolder.exists()) {
                logDb.insertInfo(KUKURATUS_WORKSPACE, "Supplied GlobalFolder doesn't exist. Trying to create it..."); //$NON-NLS-1$
                if (!kukuratusMainFolder.mkdirs()) {
                    throw new IOException("Unable to create global folder: " + globalFolderPath); //$NON-NLS-1$
                }
            } else {
                // check if it is writable
                if (!kukuratusMainFolder.canWrite()) {
                    throw new IOException("Unable to write in the supplied global folder: " + globalFolderPath); //$NON-NLS-1$
                }
            }
            workspaceFolder = new File(kukuratusMainFolder, WORKSPACE_FOLDERNAME);
            if (!workspaceFolder.exists() && !workspaceFolder.mkdir()) {
                throw new IOException("Unable to create workspace folder: " + workspaceFolder); //$NON-NLS-1$
            }
            dataFolder = new File(kukuratusMainFolder, DATA_FOLDERNAME);
            if (!dataFolder.exists() && !dataFolder.mkdir()) {
                throw new IOException("Unable to create data folder: " + dataFolder); //$NON-NLS-1$
            }

            // log db
            File logDbFile = new File(workspaceFolder, "log.sqlite"); //$NON-NLS-1$
            try {
                logDb.init(logDbFile.getAbsolutePath(), EDb.SQLITE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            tmpFolderFile = new File(workspaceFolder, TMP_FOLDERNAME);
            if (!tmpFolderFile.exists()) {
                if (!tmpFolderFile.mkdir()) {
                    throw new IOException("Can't create TMP folder: " + tmpFolderFile.getAbsolutePath()); //$NON-NLS-1$
                }
            }

        } else {
            throw new IOException("No global folder defined."); //$NON-NLS-1$
        }
    }

    public Logger getLogDb() {
        return logDb;
    }

    public File getKukuratusMainFolder() {
        return kukuratusMainFolder;
    }

    public File getWorkspaceFolder() {
        return workspaceFolder;
    }

    public File getTmpFolder() {
        return tmpFolderFile;
    }

    public File getRegistryDatabase() {
        File userRegistryDb = new File(workspaceFolder, REGISTRY_DB_NAME);
        if (userRegistryDb.exists()) {
            // might be good to backup
        }
        return userRegistryDb;
    }

    public File getDataFolder() {
        return dataFolder;
    }

//    public Optional<File> getUsersFolder() {
//        if (!usersFolderOpt.isPresent() && stageWorkspaceFolderOpt.isPresent()) {
//            File usersFolder = new File(stageWorkspaceFolderOpt.get(), "users");
//            if (!usersFolder.exists()) {
//                if (!usersFolder.mkdirs()) {
//                    throw new RuntimeException(COULD_NOT_CREATE_PROFILES_FOLDER);
//                }
//            }
//            usersFolderOpt = Optional.ofNullable(usersFolder);
//        }
//
//        return usersFolderOpt;
//    }
//
//    private Optional<File> getUserFolder( String user ) {
//        Optional<File> usersFolder = getUsersFolder();
//        if (usersFolder.isPresent()) {
//            File userFolder = new File(usersFolder.get(), user);
//            if (!userFolder.exists()) {
//                if (!userFolder.mkdirs()) {
//                    throw new RuntimeException(COULD_NOT_CREATE_USER_FOLDER);
//                }
//            }
//            return Optional.of(userFolder);
//        }
//        return Optional.empty();
//    }
//    public Optional<File> getDataFolder( String user ) {
//        if (customDataFolderOpt.isPresent()) {
//            return customDataFolderOpt;
//        }
//        Optional<File> userFolder = getUserFolder(user);
//        if (userFolder.isPresent()) {
//            File dataFolder = new File(userFolder.get(), DATA_FOLDERNAME);
//            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
//                throw new RuntimeException(COULD_NOT_CREATE_DATA_FOLDER);
//            }
//            return Optional.of(dataFolder);
//        }
//        return Optional.empty();
//    }

    public static String makeSafe( String path ) {
        path = path.replace('\\', '/');
        return path;
    }

}
