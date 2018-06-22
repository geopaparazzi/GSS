package com.hydrologis.gss;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.hortonmachine.dbs.compat.EDb;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

public class GssContext {

    private static final String DB_PREFIX = "gss_database";

    private List<String> entrypointNamesList = null;

    private static GssContext context;

    private GssDbProvider dbProvider;

    private GssContext() {
    }

    public static GssContext instance() {
        if (context == null) {
            context = new GssContext();
        }
        return context;
    }

    public List<String> getAvailableEntryPointNames() {
        if (entrypointNamesList == null) {
            entrypointNamesList = new ArrayList<>();
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint point = registry.getExtensionPoint("eu.hydrologis.stage.entrypoint");
            if (point != null) {
                IExtension[] extensions = point.getExtensions();
                for( IExtension extension : extensions ) {
                    IConfigurationElement[] configurationElements = extension.getConfigurationElements();
                    for( IConfigurationElement element : configurationElements ) {
                        String clazz = element.getAttribute("class");
                        entrypointNamesList.add(clazz);
                    }
                }
            }
        }
        return entrypointNamesList;
    }

    public synchronized GssDbProvider getDbProvider() {
        if (dbProvider == null) {
            dbProvider = new GssDbProvider();
        }
        return dbProvider;
    }

    public File getDatabaseFile() throws IOException {
        Optional<File> globalDataFolder = StageWorkspace.getInstance().getGlobalDataFolder();
        if (globalDataFolder.isPresent()) {
            File dataFolderFile = globalDataFolder.get();
            File[] databaseFiles = dataFolderFile.listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return name.startsWith(DB_PREFIX)
                            && (name.endsWith(EDb.SPATIALITE.getExtension()) || name.endsWith(EDb.H2GIS.getExtension()));
                }
            });
            if (databaseFiles != null && databaseFiles.length == 1) {
                return databaseFiles[0];
            }
            
            // create a new database
            StageLogger.logInfo(this, "No database present in folder, creating one.");
            File dbFile = new File(dataFolderFile, "gss_database.mv.db");
            return dbFile;
        }
        throw new IOException("Can't find main database file in: " + globalDataFolder.get().getAbsolutePath());
    }

}
