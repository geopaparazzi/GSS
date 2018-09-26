package com.hydrologis.gss.server.utils;

import java.io.File;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.gears.utils.files.FileUtilities;

import com.hydrologis.gss.server.database.objects.Forms;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.j256.ormlite.dao.Dao;

public class FormDatabaseUtilis {

    public static void main( String[] args ) throws Exception {
//        insertForm("/home/hydrologis/TMP/TESTGSS/DATA/gss_database.mv.db",
//                "/home/hydrologis/TMP/TESTGSS/FW_TreeVisualAssessment.json");
        insertForm("/home/hydrologis/TMP/TESTGSS/DATA/gss_database.mv.db", "/home/hydrologis/TMP/TESTGSS/tags.json");
    }

    private static void insertForm( String dbPath, String formPath ) throws Exception {
        EDb edb = EDb.fromFileDesktop(new File(dbPath));
        try (ASpatialDb spatialDb = edb.getSpatialDb()) {
            spatialDb.open(dbPath);

            DatabaseHandler databaseHandler = DatabaseHandler.instance(spatialDb);

            if (!spatialDb.hasTable(DatabaseHandler.getTableName(Forms.class))) {
                databaseHandler.createTableIfNotExists(Forms.class);
            }
            Dao<Forms, ? > formsDao = databaseHandler.getDao(Forms.class);

            String formString = FileUtilities.readFile(formPath);
            String formName = FileUtilities.getNameWithoutExtention(new File(formPath));

            Forms form = new Forms(formName, formString, "god");
            formsDao.create(form);

        }
    }

}
