package com.hydrologis.gss;

import java.io.File;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.hydrologis.stage.libs.utils.FileUtilities;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

public class GssActivator implements BundleActivator {
    public static final String PLUGIN_ID = "eu.hydrologis.gss";

    @Override
    public void start( BundleContext context ) throws Exception {
        Bundle bundle = context.getBundle();
        File bundleFile = FileLocator.getBundleFile(bundle);

        StageWorkspace workspace = StageWorkspace.getInstance();

        File resFolder = new File(bundleFile, "resources");

        File notesFile = new File(resFolder, "notes.png");
        File imagesFile = new File(resFolder, "images.png");

        File dataFolder = workspace.getDataFolder(null).get();
        File notesOutFile = new File(dataFolder, "notes.png");
        File imagesOutFile = new File(dataFolder, "images.png");
        FileUtilities.copyFile(notesFile, notesOutFile);
        FileUtilities.copyFile(imagesFile, imagesOutFile);
    }

    @Override
    public void stop( BundleContext context ) throws Exception {
        // TODO Auto-generated method stub

    }

}
