package com.hydrologis.gss;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utils.FileUtilities;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

public class GssActivator implements BundleActivator {
    public static final String PLUGIN_ID = "eu.hydrologis.gss";

    @Override
    public void start( BundleContext context ) throws Exception {

        StageWorkspace workspace = StageWorkspace.getInstance();

        Bundle bundle = context.getBundle();
        URL notesUrl = bundle.getResource("defaulticons/notes.png");
        URL imagesUrl = bundle.getResource("defaulticons/images.png");
        try {
            String notesPath = FileLocator.toFileURL(notesUrl).getPath();
            String imagesPath = FileLocator.toFileURL(imagesUrl).getPath();
            File notesFile = new File(notesPath);
            File imagesFile = new File(imagesPath);

            File dataFolder = workspace.getDataFolder(null).get();
            File notesOutFile = new File(dataFolder, "notes.png");
            File imagesOutFile = new File(dataFolder, "images.png");
            FileUtilities.copyFile(notesFile, notesOutFile);
            FileUtilities.copyFile(imagesFile, imagesOutFile);

        } catch (IOException e) {
            StageLogger.logError(this, e);
        }

    }

    @Override
    public void stop( BundleContext context ) throws Exception {
        // TODO Auto-generated method stub

    }

}
